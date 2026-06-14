package top.nxxy335.commentaiautopilot;

import org.springframework.stereotype.Component;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import top.nxxy335.commentaiautopilot.extension.AiCommentReply;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author 暖心向阳335
 * @since 1.0.0
 */
@Component
public class CommentAiAutopilotPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public CommentAiAutopilotPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(AiCommentReply.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<AiCommentReply, String>single("spec.commentId", String.class)
                .indexFunc(ext -> ext.getSpec().getCommentId()));
            indexSpecs.add(IndexSpecs.<AiCommentReply, String>single("spec.postId", String.class)
                .indexFunc(ext -> ext.getSpec().getPostId()));
            indexSpecs.add(IndexSpecs.<AiCommentReply, String>single("spec.status", String.class)
                .indexFunc(ext -> ext.getSpec().getStatus()));
        });
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(AiCommentReply.class));
    }
}
