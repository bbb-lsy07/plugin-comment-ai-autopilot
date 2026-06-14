package top.nxxy335.commentaiautopilot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import top.nxxy335.commentaiautopilot.extension.AiCommentReply;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiReplyOrchestrator {

    private final ContextExtractor contextExtractor;
    private final PromptBuilder promptBuilder;
    private final AiReplyService aiReplyService;
    private final SentimentService sentimentService;
    private final ReviewService reviewService;
    private final CommentReplyPublisher commentReplyPublisher;
    private final FilterService filterService;
    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    // In-memory dedup: tracks which comment/reply is currently being processed
    // Prevents duplicate replies when Reconciler fires multiple times
    private final ConcurrentHashMap<String, Boolean> processingLocks = new ConcurrentHashMap<>();

    /**
     * Process a new comment or reply.
     *
     * @param commentName     the parent Comment name
     * @param replyName       the Reply name that triggered this (null for top-level comments)
     * @param isAiConversation true when someone replied to AI's reply (conversation continuation)
     */
    public Mono<Void> processComment(String commentName, String replyName, boolean isAiConversation) {
        String lockKey = isAiConversation ? commentName + ":conv:" + replyName : commentName + ":top";

        // In-memory dedup: if already processing, skip immediately
        if (processingLocks.putIfAbsent(lockKey, Boolean.TRUE) != null) {
            log.info("[Orchestrator] Already processing: {}, skipping duplicate", lockKey);
            return Mono.empty();
        }

        log.info("[Orchestrator] Start processing: comment={}, replyName={}, isAiConversation={}",
            commentName, replyName, isAiConversation);

        return isAutoReplyEnabled()
            .flatMap(enabled -> {
                if (!enabled) {
                    log.info("[Orchestrator] Auto reply disabled, skipping: {}", commentName);
                    return Mono.empty();
                }
                return filterService.shouldProcess(commentName)
                    .flatMap(shouldProcess -> {
                        if (!shouldProcess) {
                            log.info("[Orchestrator] Filtered out by rules: {}", commentName);
                            return Mono.empty();
                        }
                        // For top-level comments: skip if we already have ANY reply record
                        // For AI conversation: skip if we already replied to THIS specific reply
                        if (!isAiConversation) {
                            return hasExistingReply(commentName)
                                .flatMap(hasReply -> {
                                    if (hasReply) {
                                        log.info("[Orchestrator] Already have reply record for: {}, skipping", commentName);
                                        return Mono.empty();
                                    }
                                    return doProcess(commentName, replyName, isAiConversation);
                                });
                        }
                        return hasExistingConversationReply(replyName)
                            .flatMap(hasReply -> {
                                if (hasReply) {
                                    log.info("[Orchestrator] Already replied to reply: {}, skipping", replyName);
                                    return Mono.empty();
                                }
                                return doProcess(commentName, replyName, isAiConversation);
                            });
                    });
            })
            .doOnError(e -> log.error("[Orchestrator] Error processing comment {}: {}", commentName, e.getMessage(), e))
            .doFinally(signal -> {
                // Always release the lock when processing completes
                processingLocks.remove(lockKey);
                log.debug("[Orchestrator] Released processing lock for: {}", lockKey);
            })
            .then();
    }

    private Mono<Void> doProcess(String commentName, String replyName, boolean isAiConversation) {
        return getModelName().flatMap(modelName ->
            contextExtractor.extract(commentName, replyName, isAiConversation)
                .flatMap(context -> sentimentService.analyzeSentiment(context.commentContent(), modelName)
                    .flatMap(sentimentResult -> {
                        log.info("[Orchestrator] Sentiment for {}: {} (confidence: {})",
                            commentName, sentimentResult.sentiment(), sentimentResult.confidence());
                        return promptBuilder.buildPrompt(context, sentimentResult.sentiment())
                            .flatMap(prompt -> createAiCommentReply(context, sentimentResult.sentiment())
                                .flatMap(replyRecord -> generateAndPublish(prompt, context, replyRecord, modelName))
                            );
                    })
                )
        );
    }

    /**
     * Check if there's already ANY AiCommentReply record for this top-level comment.
     * Checks for ANY record (not just published) to prevent race conditions.
     */
    private Mono<Boolean> hasExistingReply(String commentName) {
        return client.list(AiCommentReply.class,
                record -> commentName.equals(record.getSpec().getCommentId())
                    && !Boolean.TRUE.equals(record.getSpec().getIsAiConversation()),
                null)
            .hasElements()
            .defaultIfEmpty(false)
            .onErrorResume(e -> {
                log.debug("[Orchestrator] Failed to check existing replies: {}", e.getMessage());
                return Mono.just(false);
            });
    }

    /**
     * Check if we already have ANY AiCommentReply record for this specific reply (conversation).
     * Checks for ANY record (not just published) to prevent race conditions.
     */
    private Mono<Boolean> hasExistingConversationReply(String replyName) {
        if (replyName == null || replyName.isBlank()) {
            return Mono.just(false);
        }
        return client.list(AiCommentReply.class,
                record -> replyName.equals(record.getSpec().getReplyTo())
                    && Boolean.TRUE.equals(record.getSpec().getIsAiConversation()),
                null)
            .hasElements()
            .defaultIfEmpty(false)
            .onErrorResume(e -> {
                log.debug("[Orchestrator] Failed to check existing conversation replies: {}", e.getMessage());
                return Mono.just(false);
            });
    }

    /**
     * Generate AI reply, optionally review it, then publish.
     */
    private Mono<Void> generateAndPublish(String prompt, ContextExtractor.CommentContext context,
                                           AiCommentReply replyRecord, String modelName) {
        return aiReplyService.generateReply(prompt, modelName)
            .defaultIfEmpty("")
            .flatMap(aiReply -> {
                if (aiReply.isBlank()) {
                    log.warn("[Orchestrator] AI generated empty reply for: {}", context.commentId());
                    return updateRecord(replyRecord, "", 0, "FAIL", false).then();
                }

                log.info("[Orchestrator] AI generated reply for {}: {} chars",
                    context.commentId(), aiReply.length());

                return reviewService.review(context.postContent(), context.commentContent(), aiReply, modelName)
                    .flatMap(reviewResult -> {
                        log.info("[Orchestrator] Review for {}: score={}, status={}, reason={}",
                            context.commentId(), reviewResult.score(), reviewResult.status(), reviewResult.reason());
                        if ("FAIL".equals(reviewResult.status())) {
                            log.warn("[Orchestrator] Content safety review FAILED for: {}, not publishing",
                                context.commentId());
                            return updateRecord(replyRecord, aiReply, 0, "FAIL", false).then();
                        }
                        return publishReply(context, aiReply, replyRecord, reviewResult.score());
                    })
                    .switchIfEmpty(
                        publishReply(context, aiReply, replyRecord, 100)
                    )
                    .onErrorResume(e -> {
                        log.warn("[Orchestrator] Review error, auto-passing: {}", e.getMessage());
                        return publishReply(context, aiReply, replyRecord, 100);
                    });
            });
    }

    /**
     * Publish the reply and update the record to PASS + published=true.
     */
    private Mono<Void> publishReply(ContextExtractor.CommentContext context, String aiReply,
                                     AiCommentReply replyRecord, int score) {
        return isAutoPublishEnabled()
            .flatMap(autoPublish -> {
                return commentReplyPublisher.publishReply(
                        context.commentId(), aiReply, context.postId(), context.replyTo(), autoPublish)
                    .flatMap(publishedReply -> {
                        log.info("[Orchestrator] Reply {} for: {}", autoPublish ? "published" : "saved as draft", context.commentId());
                        return updateRecord(replyRecord, aiReply, score, "PASS", autoPublish);
                    })
                    .flatMap(updated -> Mono.empty());
            })
            .then();
    }

    private Mono<String> getModelName() {
        return settingFetcher.getSettingValue("model")
            .map(node -> {
                var nameNode = node.get("modelName");
                if (nameNode != null && !nameNode.asText().isBlank()) {
                    return nameNode.asText();
                }
                return "";
            })
            .onErrorResume(e -> {
                log.debug("[Orchestrator] Failed to fetch model setting: {}", e.getMessage());
                return Mono.just("");
            })
            .defaultIfEmpty("");
    }

    private Mono<Boolean> isAutoReplyEnabled() {
        return settingFetcher.getSettingValue("basic")
            .map(node -> !node.has("autoReply") || node.get("autoReply").asBoolean(true))
            .onErrorResume(e -> {
                log.debug("[Orchestrator] Failed to fetch autoReply setting: {}", e.getMessage());
                return Mono.just(true);
            })
            .defaultIfEmpty(true);
    }

    private Mono<Boolean> isAutoPublishEnabled() {
        return settingFetcher.getSettingValue("basic")
            .map(node -> !node.has("autoPublish") || node.get("autoPublish").asBoolean(true))
            .onErrorResume(e -> {
                log.debug("[Orchestrator] Failed to fetch autoPublish setting: {}", e.getMessage());
                return Mono.just(true);
            })
            .defaultIfEmpty(true);
    }

    private Mono<AiCommentReply> createAiCommentReply(ContextExtractor.CommentContext context, String sentiment) {
        AiCommentReply record = new AiCommentReply();
        record.setMetadata(new Metadata());
        record.getMetadata().setName("ai-reply-" + UUID.randomUUID().toString().substring(0, 8));
        record.setSpec(new AiCommentReply.Spec());
        record.getSpec().setCommentId(context.commentId());
        record.getSpec().setPostId(context.postId());
        record.getSpec().setPostSlug(context.postSlug());
        record.getSpec().setReply("");
        record.getSpec().setScore(0);
        record.getSpec().setStatus("PENDING");
        record.getSpec().setRetryCount(0);
        record.getSpec().setReplyTo(context.replyTo());
        record.getSpec().setIsAiConversation(context.isAiConversation());
        record.getSpec().setPublished(false);
        record.getSpec().setSentiment(sentiment);
        return client.create(record)
            .doOnSuccess(created -> log.info("[Orchestrator] Created AiCommentReply record: {}",
                created.getMetadata().getName()));
    }

    private Mono<AiCommentReply> updateRecord(AiCommentReply record, String reply,
                                               int score, String status, boolean published) {
        log.debug("[Orchestrator] Updating record {}: status={}, score={}, published={}",
            record.getMetadata().getName(), status, score, published);
        return client.fetch(AiCommentReply.class, record.getMetadata().getName())
            .flatMap(latest -> {
                latest.getSpec().setReply(reply);
                latest.getSpec().setScore(score);
                latest.getSpec().setStatus(status);
                latest.getSpec().setPublished(published);
                return client.update(latest);
            })
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                .filter(e -> e instanceof OptimisticLockingFailureException)
                .doBeforeRetry(signal -> log.debug("[Orchestrator] Retrying update for {} due to optimistic lock",
                    record.getMetadata().getName()))
            )
            .doOnSuccess(updated -> log.debug("[Orchestrator] Record {} updated: status={}, score={}, published={}",
                record.getMetadata().getName(), status, score, published));
    }
}
