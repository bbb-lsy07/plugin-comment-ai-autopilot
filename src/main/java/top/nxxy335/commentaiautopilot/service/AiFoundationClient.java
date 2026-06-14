package top.nxxy335.commentaiautopilot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.aifoundation.AiModelService;
import run.halo.aifoundation.chat.LanguageModel;
import run.halo.aifoundation.chat.GenerateTextResult;

/**
 * AI Foundation client that calls the AI Foundation plugin's AiModelService.
 * Only instantiated when AI Foundation classes are available (via @ConditionalOnClass).
 */
@Slf4j
@RequiredArgsConstructor
public class AiFoundationClient {

    private static final String AI_FOUNDATION_PLUGIN_NAME = "ai-foundation";

    private final ExtensionGetter extensionGetter;
    private final ReactiveExtensionClient client;

    /**
     * Call AI Foundation to generate a chat response using the specified model.
     * Checks at runtime whether the ai-foundation plugin is installed and enabled
     * before attempting to use it.
     *
     * @param prompt    the prompt text
     * @param modelName the AiModel metadata.name, null or blank to use default model
     * @return the generated text, or empty if AI Foundation is unavailable
     */
    public Mono<String> chat(String prompt, String modelName) {
        return isAiFoundationEnabled()
            .flatMap(enabled -> {
                if (!enabled) {
                    log.warn("AI Foundation plugin is not installed or not enabled, skipping AI reply");
                    return Mono.empty();
                }
                return doChat(prompt, modelName);
            });
    }

    /**
     * Check if the ai-foundation plugin is installed and enabled at runtime.
     */
    private Mono<Boolean> isAiFoundationEnabled() {
        return client.fetch(Plugin.class, AI_FOUNDATION_PLUGIN_NAME)
            .map(plugin -> plugin.getSpec().getEnabled())
            .defaultIfEmpty(false)
            .onErrorResume(e -> {
                log.debug("Failed to check AI Foundation plugin status: {}", e.getMessage());
                return Mono.just(false);
            });
    }

    private Mono<String> doChat(String prompt, String modelName) {
        return extensionGetter.getEnabledExtension(AiModelService.class)
            .flatMap(service -> {
                Mono<LanguageModel> modelMono;
                if (modelName != null && !modelName.isBlank()) {
                    modelMono = service.languageModel(modelName);
                } else {
                    modelMono = service.languageModel();
                }
                return modelMono.flatMap(model -> model.generateText(prompt)
                    .map(GenerateTextResult::getText)
                    .doOnNext(text -> log.debug("AI generated reply ({} chars) using model '{}'",
                        text.length(), modelName != null ? modelName : "default"))
                );
            })
            .doOnError(e -> log.error("AI Foundation call failed: {}", e.getMessage()))
            .onErrorResume(e -> {
                log.warn("AI Foundation not available: {}", e.getMessage());
                return Mono.empty();
            });
    }
}
