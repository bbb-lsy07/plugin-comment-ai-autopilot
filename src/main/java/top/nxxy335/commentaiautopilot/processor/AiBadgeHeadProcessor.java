package top.nxxy335.commentaiautopilot.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * Template head processor for comment-ai-autopilot.
 * Previously used to inject avatar replacement scripts, but now
 * avatar is handled natively via Gravatar (email-based).
 * Kept as a no-op to avoid breaking the Spring component scan.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AiBadgeHeadProcessor implements TemplateHeadProcessor {

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
                               IElementModelStructureHandler structureHandler) {
        return Mono.empty();
    }
}
