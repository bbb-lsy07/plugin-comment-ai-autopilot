package top.nxxy335.commentaiautopilot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import top.nxxy335.commentaiautopilot.extension.AiPersona;

/**
 * Service for checking wake words in comment content.
 * A wake word is a prefix that triggers AI reply from a specific persona,
 * even if the page hasn't enabled AI auto-reply.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WakeWordService {

    private final ReactiveExtensionClient client;

    /**
     * Result of a wake word match.
     *
     * @param personaName the metadata.name of the matched persona
     * @param wakeWord    the wake word that matched
     * @param contentWithoutWakeWord the comment content with the wake word prefix removed
     */
    public record WakeWordMatch(String personaName, String wakeWord, String contentWithoutWakeWord) {}

    /**
     * Check if the given content starts with any persona's wake word.
     * Returns the first matching persona's info, or empty if no match.
     *
     * @param content the comment/reply content to check
     * @return WakeWordMatch if a wake word is found, or empty Mono
     */
    public Mono<WakeWordMatch> checkWakeWord(String content) {
        if (content == null || content.isBlank()) {
            return Mono.empty();
        }

        return client.list(AiPersona.class, null, null)
            .filter(persona -> {
                String wakeWord = persona.getSpec().getWakeWord();
                return wakeWord != null && !wakeWord.isBlank() && content.startsWith(wakeWord);
            })
            .next()
            .map(persona -> {
                String wakeWord = persona.getSpec().getWakeWord();
                String remaining = content.substring(wakeWord.length()).trim();
                log.info("[WakeWord] Matched wake word '{}' for persona '{}'",
                    wakeWord, persona.getSpec().getDisplayName());
                return new WakeWordMatch(persona.getMetadata().getName(), wakeWord, remaining);
            });
    }

    /**
     * Blocking version for use in Reconciler (sync context).
     * Checks if the given content starts with any persona's wake word.
     *
     * @param syncClient the blocking ExtensionClient
     * @param content the comment/reply content to check
     * @return WakeWordMatch if a wake word is found, or null
     */
    public WakeWordMatch checkWakeWordBlocking(run.halo.app.extension.ExtensionClient syncClient, String content) {
        if (content == null || content.isBlank()) {
            log.info("[WakeWord] Content is null or blank, skipping");
            return null;
        }

        String trimmedContent = content.trim();
        var personas = syncClient.listAll(AiPersona.class, null, Sort.unsorted());
        log.info("[WakeWord] Checking {} personas against content: '{}'", personas.size(),
            trimmedContent.length() > 50 ? trimmedContent.substring(0, 50) + "..." : trimmedContent);

        for (var persona : personas) {
            String wakeWord = persona.getSpec().getWakeWord();
            if (wakeWord == null || wakeWord.isBlank()) {
                log.info("[WakeWord] Persona '{}' has no wakeWord, skipping", persona.getSpec().getDisplayName());
                continue;
            }
            String trimmedWakeWord = wakeWord.trim();
            log.info("[WakeWord] Checking persona '{}' with wakeWord '{}' against content starting with '{}'",
                persona.getSpec().getDisplayName(), trimmedWakeWord,
                trimmedContent.length() >= trimmedWakeWord.length()
                    ? trimmedContent.substring(0, trimmedWakeWord.length()) : trimmedContent);

            if (trimmedContent.startsWith(trimmedWakeWord)) {
                String remaining = trimmedContent.substring(trimmedWakeWord.length()).trim();
                log.info("[WakeWord] MATCHED! wakeWord='{}' for persona '{}', remaining content: '{}'",
                    trimmedWakeWord, persona.getSpec().getDisplayName(),
                    remaining.length() > 30 ? remaining.substring(0, 30) + "..." : remaining);
                return new WakeWordMatch(persona.getMetadata().getName(), trimmedWakeWord, remaining);
            }
        }
        log.info("[WakeWord] No wake word matched");
        return null;
    }
}
