package top.nxxy335.commentaiautopilot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import top.nxxy335.commentaiautopilot.extension.AiPersona;
import top.nxxy335.commentaiautopilot.util.GravatarUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CommentReplyPublisher {

    private final ReactiveExtensionClient client;

    public CommentReplyPublisher(ReactiveExtensionClient client) {
        this.client = client;
    }

    private static final String AI_PERSONA_OWNER_PREFIX = "ai-persona-";

    /**
     * Publish a reply to a comment automatically using AI Persona identity.
     * Includes a final dedup check: if an AI reply already exists for this comment,
     * skip publishing to prevent duplicate replies.
     *
     * @param personaName the persona name to use (null for default persona)
     */
    public Mono<Reply> publishReply(String parentCommentName, String replyContent,
                                     String postName, String quoteReplyName, boolean autoPublish,
                                     String personaName) {
        return checkExistingAiReply(parentCommentName, quoteReplyName)
            .flatMap(exists -> {
                if (exists) {
                    log.info("[Publisher] AI reply already exists for comment: {}, skipping duplicate publish",
                        parentCommentName);
                    return Mono.empty();
                }
                return doPublish(parentCommentName, replyContent, postName, quoteReplyName, autoPublish, personaName);
            });
    }

    private Mono<Boolean> checkExistingAiReply(String parentCommentName, String quoteReplyName) {
        return client.list(Reply.class,
                reply -> {
                    if (!parentCommentName.equals(reply.getSpec().getCommentName())) {
                        return false;
                    }
                    var owner = reply.getSpec().getOwner();
                    if (owner == null || owner.getName() == null) {
                        return false;
                    }
                    boolean isAiOwner = owner.getName().startsWith(AI_PERSONA_OWNER_PREFIX);
                    boolean hasAiAnnotation = owner.getAnnotations() != null
                        && "true".equals(owner.getAnnotations().get("comment-ai-autopilot.nxxy335.top/is-ai"));
                    boolean isAiReply = isAiOwner || hasAiAnnotation;

                    if (!isAiReply) {
                        return false;
                    }

                    if (quoteReplyName != null && !quoteReplyName.isBlank()) {
                        return quoteReplyName.equals(reply.getSpec().getQuoteReply());
                    }
                    return true;
                },
                null)
            .hasElements()
            .defaultIfEmpty(false);
    }

    private Mono<Reply> doPublish(String parentCommentName, String replyContent,
                                   String postName, String quoteReplyName, boolean autoPublish,
                                   String personaName) {

        // 1. 获取被回复的对象信息，构建 Markdown 引用
        Mono<String> quoteMarkdownMono = Mono.empty();

        if (quoteReplyName != null && !quoteReplyName.isBlank()) {
            // 如果是回复另一条回复
            quoteMarkdownMono = client.fetch(Reply.class, quoteReplyName)
                .map(r -> buildQuoteMarkdown(
                    r.getSpec().getOwner() != null ? r.getSpec().getOwner().getDisplayName() : "匿名用户",
                    r.getSpec().getRaw() != null ? r.getSpec().getRaw() : r.getSpec().getContent()
                ));
        } else if (parentCommentName != null && !parentCommentName.isBlank()) {
            // 如果是直接回复顶级评论
            quoteMarkdownMono = client.fetch(Comment.class, parentCommentName)
                .map(c -> buildQuoteMarkdown(
                    c.getSpec().getOwner() != null ? c.getSpec().getOwner().getDisplayName() : "匿名用户",
                    c.getSpec().getRaw() != null ? c.getSpec().getRaw() : c.getSpec().getContent()
                ));
        }

        // 2. 解析 AI 角色并合并最终文本进行发布
        return Mono.zip(resolvePersona(personaName), quoteMarkdownMono.defaultIfEmpty(""))
            .flatMap(tuple -> {
                ResolvedPersona persona = tuple.getT1();
                String quoteMarkdown = tuple.getT2();

                String displayName = persona.displayName();
                String email = persona.email();

                // 将引用文本拼接到 AI 回复内容的最前面
                String finalContent = quoteMarkdown.isBlank() ? replyContent : quoteMarkdown + "\n\n" + replyContent;

                Reply reply = new Reply();
                reply.setMetadata(new Metadata());
                reply.getMetadata().setName(generateReplyName());
                reply.setSpec(new Reply.ReplySpec());

                var spec = reply.getSpec();
                spec.setCommentName(parentCommentName);
                spec.setRaw(finalContent);     // 保存带有引用的完整 Markdown
                spec.setContent(finalContent); // 同上
                spec.setApproved(autoPublish);
                if (autoPublish) {
                    spec.setApprovedTime(Instant.now());
                }
                spec.setPriority(0);
                spec.setTop(false);
                spec.setAllowNotification(false);
                spec.setHidden(false);

                if (quoteReplyName != null && !quoteReplyName.isBlank()) {
                    spec.setQuoteReply(quoteReplyName);
                }

                var owner = new Comment.CommentOwner();
                owner.setKind(Comment.CommentOwner.KIND_EMAIL);
                if (email != null && !email.isBlank()) {
                    owner.setName(email);
                } else {
                    owner.setName(AI_PERSONA_OWNER_PREFIX + displayName);
                }
                owner.setDisplayName(displayName + " AI");

                Map<String, String> ownerAnnotations = new HashMap<>();
                ownerAnnotations.put("comment-ai-autopilot.nxxy335.top/is-ai", "true");
                if (email != null && !email.isBlank()) {
                    String gravatarUrl = GravatarUtil.generateUrl(email);
                    ownerAnnotations.put(Comment.CommentOwner.AVATAR_ANNO, gravatarUrl);
                }
                owner.setAnnotations(ownerAnnotations);
                spec.setOwner(owner);

                log.info("[Publisher] Creating reply for comment: {}, finalContent length: {}", parentCommentName, finalContent.length());

                return client.create(reply)
                    .doOnSuccess(created -> log.info("[Publisher] AI Persona '{}' reply published for comment: {}", displayName, parentCommentName))
                    .doOnError(e -> log.error("[Publisher] Failed to publish AI reply: {}", e.getMessage()));
            });
    }

    /**
     * 新增辅助方法：构建 Markdown 引用块
     */
    private String buildQuoteMarkdown(String username, String rawText) {
        if (rawText == null || rawText.isBlank()) return "";
        // 清除 HTML 标签，防止破坏 Markdown 结构
        String plainText = org.jsoup.Jsoup.clean(rawText, org.jsoup.safety.Safelist.none()).trim();
        // 截断过长的引用内容（超过 40 个字符加省略号）
        String truncated = plainText.length() > 40 ? plainText.substring(0, 40) + "..." : plainText;
        // 生成标准 Markdown Blockquote
        return "> 💬 **@" + username + "** : " + truncated;
    }

    /**
     * Resolve persona info from AiPersona extension.
     * Priority:
     * 1. If personaName is provided, fetch from AiPersona extension
     * 2. If personaName is empty, find the default AiPersona (isDefault=true)
     * 3. If no AiPersona found, return default "小回" with empty email
     */
    private Mono<ResolvedPersona> resolvePersona(String personaName) {
        if (personaName != null && !personaName.isBlank()) {
            return client.fetch(AiPersona.class, personaName)
                .map(p -> {
                    log.info("[Publisher] Resolved persona by name: {}, email={}",
                        personaName, p.getSpec().getEmail());
                    return new ResolvedPersona(
                        p.getSpec().getDisplayName(),
                        p.getSpec().getEmail()
                    );
                })
                .defaultIfEmpty(new ResolvedPersona("小回", ""));
        }
        // Find default persona
        return client.list(AiPersona.class,
                persona -> persona.getSpec() != null && Boolean.TRUE.equals(persona.getSpec().getIsDefault()),
                null)
            .next()
            .map(p -> {
                log.info("[Publisher] Resolved default persona: {}, email={}",
                    p.getMetadata().getName(), p.getSpec().getEmail());
                return new ResolvedPersona(
                    p.getSpec().getDisplayName(),
                    p.getSpec().getEmail()
                );
            })
            .defaultIfEmpty(new ResolvedPersona("小回", ""));
    }

    private record ResolvedPersona(String displayName, String email) {}

    private String generateReplyName() {
        return "ai-comment-reply-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
