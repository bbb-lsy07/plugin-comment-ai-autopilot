package top.nxxy335.commentaiautopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class CommentReplyPublisher {

    private final ReactiveExtensionClient client;
    private final ObjectMapper objectMapper;

    public CommentReplyPublisher(ReactiveExtensionClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }

    private static final String DEFAULT_PERSONA_NAME = "小回";
    private static final String AI_PERSONA_OWNER_PREFIX = "ai-persona-";
    private static final String CONFIG_MAP_NAME = "comment-ai-autopilot-configmap";

    /**
     * Publish a reply to a comment automatically using AI Persona identity.
     * Includes a final dedup check: if an AI reply already exists for this comment,
     * skip publishing to prevent duplicate replies.
     */
    public Mono<Reply> publishReply(String parentCommentName, String replyContent,
                                     String postName, String quoteReplyName, boolean autoPublish) {
        return checkExistingAiReply(parentCommentName, quoteReplyName)
            .flatMap(exists -> {
                if (exists) {
                    log.info("[Publisher] AI reply already exists for comment: {}, skipping duplicate publish",
                        parentCommentName);
                    return Mono.empty();
                }
                return doPublish(parentCommentName, replyContent, postName, quoteReplyName, autoPublish);
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
                                   String postName, String quoteReplyName, boolean autoPublish) {
        return getPersonaName().flatMap(personaName ->
            getPersonaEmail().flatMap(email -> {
                Reply reply = new Reply();
                reply.setMetadata(new Metadata());
                reply.getMetadata().setName(generateReplyName());
                reply.setSpec(new Reply.ReplySpec());

                var spec = reply.getSpec();
                spec.setCommentName(parentCommentName);
                spec.setRaw(replyContent);
                spec.setContent(replyContent);
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
                    owner.setName(AI_PERSONA_OWNER_PREFIX + personaName);
                }
                owner.setDisplayName(personaName + " AI");

                Map<String, String> ownerAnnotations = new HashMap<>();
                ownerAnnotations.put("comment-ai-autopilot.nxxy335.top/is-ai", "true");
                if (email != null && !email.isBlank()) {
                    String gravatarUrl = generateGravatarUrl(email);
                    ownerAnnotations.put(Comment.CommentOwner.AVATAR_ANNO, gravatarUrl);
                }
                owner.setAnnotations(ownerAnnotations);
                spec.setOwner(owner);

                return client.create(reply)
                    .doOnSuccess(created -> log.info("[Publisher] AI Persona '{}' reply published for comment: {}, quoteReply: {}",
                        personaName, parentCommentName, quoteReplyName))
                    .doOnError(e -> log.error("[Publisher] Failed to publish AI reply: {}", e.getMessage()));
            })
        );
    }

    /**
     * Read persona setting directly from ConfigMap to avoid ClassLoader conflict.
     * Halo's ReactiveSettingFetcher returns JsonNode loaded by the main app ClassLoader,
     * which is incompatible with the plugin's PluginClassLoader, causing ClassCastException.
     */
    private Mono<String> getPersonaName() {
        return client.fetch(ConfigMap.class, CONFIG_MAP_NAME)
            .mapNotNull(cm -> {
                var data = cm.getData();
                if (data == null) return null;
                String personaJson = data.get("persona");
                if (personaJson == null || personaJson.isBlank()) return null;
                try {
                    JsonNode node = objectMapper.readTree(personaJson);
                    JsonNode nameNode = node.get("personaName");
                    if (nameNode != null && !nameNode.asText().isBlank()) {
                        return nameNode.asText();
                    }
                } catch (Exception e) {
                    log.warn("[Publisher] Failed to parse personaName from ConfigMap: {}", e.getMessage());
                }
                return null;
            })
            .defaultIfEmpty(DEFAULT_PERSONA_NAME);
    }

    /**
     * Read persona email directly from ConfigMap to avoid ClassLoader conflict.
     */
    private Mono<String> getPersonaEmail() {
        return client.fetch(ConfigMap.class, CONFIG_MAP_NAME)
            .mapNotNull(cm -> {
                var data = cm.getData();
                if (data == null) return null;
                String personaJson = data.get("persona");
                if (personaJson == null || personaJson.isBlank()) return null;
                try {
                    JsonNode node = objectMapper.readTree(personaJson);
                    JsonNode emailNode = node.get("personaEmail");
                    if (emailNode != null && !emailNode.asText().isBlank()) {
                        String email = emailNode.asText().trim().toLowerCase();
                        log.info("[Publisher] personaEmail resolved from ConfigMap: {}", email);
                        return email;
                    }
                    log.info("[Publisher] personaEmail is blank in ConfigMap");
                } catch (Exception e) {
                    log.warn("[Publisher] Failed to parse personaEmail from ConfigMap: {}", e.getMessage());
                }
                return null;
            })
            .defaultIfEmpty("");
    }

    private String generateReplyName() {
        return "ai-comment-reply-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate Gravatar URL from email address using SHA-256 hash.
     */
    private String generateGravatarUrl(String email) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashBytes = digest.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            var hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return "https://cn.cravatar.com/avatar/" + hexString;
        } catch (Exception e) {
            log.error("[Publisher] Failed to generate Gravatar URL: {}", e.getMessage());
            return "";
        }
    }
}
