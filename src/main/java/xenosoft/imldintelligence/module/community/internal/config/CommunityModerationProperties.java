package xenosoft.imldintelligence.module.community.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Community moderation settings.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imld.community.moderation")
public class CommunityModerationProperties {
    /**
     * Spam keywords for initial rule screening.
     */
    private List<String> spamKeywords = new ArrayList<>();

    private Llm llm = new Llm();

    @Getter
    @Setter
    public static class Llm {
        /**
         * Whether LLM moderation is enabled.
         */
        private boolean enabled = false;

        /**
         * LLM moderation endpoint (HTTP).
         */
        private String endpoint = "";

        /**
         * Call timeout.
         */
        private Duration timeout = Duration.ofSeconds(3);
    }
}

