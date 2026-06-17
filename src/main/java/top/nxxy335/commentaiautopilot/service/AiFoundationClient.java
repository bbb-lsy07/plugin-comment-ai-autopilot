package top.nxxy335.commentaiautopilot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.aifoundation.AiModelService;
import run.halo.aifoundation.chat.GenerateTextRequest;
import run.halo.aifoundation.chat.GenerateTextResult;
import run.halo.aifoundation.schema.OutputSpec;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

import java.util.List;

/**
 * AI Foundation client that uses Halo's {@link ExtensionGetter} to obtain the
 * {@link AiModelService} extension provided by the ai-foundation plugin.
 * <p>
 * This is the recommended way to integrate with AI Foundation, see
 * <a href="https://github.com/halo-dev/plugin-ai-foundation/blob/main/dev/dev.md">dev guide</a>.
 * <p>
 * Requires the following declaration in plugin.yaml:
 * <pre>
 * spec:
 *   pluginDependencies:
 *     ai-foundation?: "*"
 * </pre>
 * The dependency is optional, so the plugin still loads when AI Foundation is
 * not installed; availability is checked at runtime and all calls return empty
 * in that case.
 */
@Slf4j
@Component
public class AiFoundationClient {

    private final ExtensionGetter extensionGetter;

    public AiFoundationClient(ExtensionGetter extensionGetter) {
        this.extensionGetter = extensionGetter;
    }

    /**
     * Call AI Foundation to generate a chat response using the specified model.
     * Uses {@link GenerateTextRequest} with {@code maxRetries=2} so that
     * transient model errors are retried by the SDK.
     *
     * @param prompt    the prompt text
     * @param modelName the AiModel metadata.name, null or blank to use default model
     * @return the generated text, or empty if AI Foundation is unavailable
     */
    public Mono<String> chat(String prompt, String modelName) {
        return aiModelService()
            .flatMap(service -> service.languageModel(modelName != null ? modelName : "")
                .flatMap(model -> model.generateText(
                    GenerateTextRequest.builder().prompt(prompt).maxRetries(2).build()))
                .map(GenerateTextResult::getText))
            .doOnError(e -> log.error("AI Foundation call failed: {}", e.getMessage()))
            .onErrorResume(e -> {
                log.warn("AI Foundation not available: {}", e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Call AI Foundation to classify text into one of the given choices using
     * structured output ({@link OutputSpec#choice(List)}).
     * <p>
     * This is the recommended way to do classification per the dev guide,
     * as it is more reliable than prompt parsing.
     *
     * @param systemPrompt system prompt describing the task
     * @param userPrompt   the user input to classify
     * @param choices      the allowed classification values
     * @param modelName    the AiModel metadata.name, null or blank to use default model
     * @return the selected choice string, or empty if AI Foundation is unavailable
     */
    public Mono<String> classify(String systemPrompt, String userPrompt,
                                  List<String> choices, String modelName) {
        return aiModelService()
            .flatMap(service -> service.languageModel(modelName != null ? modelName : "")
                .flatMap(model -> model.generateText(
                    GenerateTextRequest.builder()
                        .system(systemPrompt)
                        .prompt(userPrompt)
                        .output(OutputSpec.choice(choices))
                        .maxRetries(2)
                        .build()))
                .map(result -> {
                    Object output = result.getOutput();
                    return output != null ? String.valueOf(output).trim() : "";
                }))
            .doOnError(e -> log.error("AI Foundation classify failed: {}", e.getMessage()))
            .onErrorResume(e -> {
                log.warn("AI Foundation not available: {}", e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Check if AI Foundation is available: plugin installed and an
     * AiModelService extension is enabled.
     */
    public Mono<Boolean> isAvailable() {
        return aiModelService().hasElement()
            .onErrorResume(e -> {
                log.debug("AI Foundation not available: {}", e.getMessage());
                return Mono.just(false);
            });
    }

    /**
     * Obtain the enabled AiModelService extension via ExtensionGetter.
     * <p>
     * Wrapped in {@link Mono#defer} with a {@link NoClassDefFoundError} guard so
     * that the plugin still works when the optional ai-foundation dependency is
     * not installed (the AiModelService API class is then absent from the
     * classloader).
     */
    private Mono<AiModelService> aiModelService() {
        return Mono.defer(() -> {
            try {
                return extensionGetter.getEnabledExtension(AiModelService.class);
            } catch (NoClassDefFoundError e) {
                log.debug("AI Foundation API not on classpath: {}", e.getMessage());
                return Mono.empty();
            }
        });
    }
}
