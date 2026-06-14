package top.nxxy335.commentaiautopilot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AiReplyService {

    private final ObjectProvider<AiFoundationClient> aiFoundationClientProvider;

    public AiReplyService(ObjectProvider<AiFoundationClient> aiFoundationClientProvider) {
        this.aiFoundationClientProvider = aiFoundationClientProvider;
    }

    /**
     * Generate an AI reply using the AI Foundation plugin.
     *
     * @param prompt    the prompt text
     * @param modelName the model name (null for default)
     */
    public Mono<String> generateReply(String prompt, String modelName) {
        AiFoundationClient client = aiFoundationClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("AI Foundation plugin is not installed, cannot generate reply");
            return Mono.empty();
        }
        return client.chat(prompt, modelName)
            .doOnError(e -> log.error("AI reply generation failed: {}", e.getMessage()))
            .onErrorResume(e -> {
                log.warn("AI Foundation not available: {}", e.getMessage());
                return Mono.empty();
            });
    }
}
