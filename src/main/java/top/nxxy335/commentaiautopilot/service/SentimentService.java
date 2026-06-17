package top.nxxy335.commentaiautopilot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class SentimentService {

    private final AiFoundationClient aiFoundationClient;

    public SentimentService(AiFoundationClient aiFoundationClient) {
        this.aiFoundationClient = aiFoundationClient;
    }

    public record SentimentResult(String sentiment, double confidence) {
        public static final String POSITIVE = "POSITIVE";
        public static final String NEUTRAL = "NEUTRAL";
        public static final String NEGATIVE = "NEGATIVE";
    }

    private static final List<String> CHOICES = List.of(
        SentimentResult.POSITIVE, SentimentResult.NEUTRAL, SentimentResult.NEGATIVE
    );

    /**
     * Analyze sentiment using AI Foundation structured output
     * ({@code OutputSpec.choice}) for reliable classification.
     */
    public Mono<SentimentResult> analyzeSentiment(String commentContent, String modelName) {
        String systemPrompt = "你是一个情感分析助手。请分析评论的情感倾向，只返回 POSITIVE、NEUTRAL 或 NEGATIVE 之一。";
        String userPrompt = "分析以下评论的情感倾向：\n\n" + commentContent;

        return aiFoundationClient.classify(systemPrompt, userPrompt, CHOICES, modelName)
            .map(sentiment -> {
                String upper = sentiment.toUpperCase();
                // Validate against known choices; default to NEUTRAL if unexpected
                if (!CHOICES.contains(upper)) {
                    log.warn("[Sentiment] Unexpected classification result: {}, defaulting to NEUTRAL", sentiment);
                    return new SentimentResult(SentimentResult.NEUTRAL, 0.0);
                }
                return new SentimentResult(upper, 1.0);
            })
            .onErrorResume(e -> {
                log.warn("[Sentiment] Failed to analyze sentiment, defaulting to NEUTRAL: {}", e.getMessage());
                return Mono.just(new SentimentResult(SentimentResult.NEUTRAL, 0.0));
            })
            .defaultIfEmpty(new SentimentResult(SentimentResult.NEUTRAL, 0.0));
    }
}
