package top.nxxy335.commentaiautopilot.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Configuration that registers AiFoundationClient only when
 * AI Foundation plugin classes are available in the classloader.
 * When AI Foundation is not installed, this entire configuration is skipped.
 */
@Configuration
@ConditionalOnClass(name = "run.halo.aifoundation.AiModelService")
public class AiFoundationConfiguration {

    @Bean
    public AiFoundationClient aiFoundationClient(ExtensionGetter extensionGetter,
                                                  ReactiveExtensionClient client) {
        return new AiFoundationClient(extensionGetter, client);
    }
}
