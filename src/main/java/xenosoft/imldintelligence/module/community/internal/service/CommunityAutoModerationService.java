package xenosoft.imldintelligence.module.community.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.community.internal.config.CommunityModerationProperties;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Hybrid moderation: rule screening (always-on) + optional LLM screening.
 */
@Service
@RequiredArgsConstructor
public class CommunityAutoModerationService {

    private final CommunityModerationProperties properties;
    private final WebClient.Builder webClientBuilder;

    // Basic privacy patterns (best-effort, MVP)
    private static final Pattern MOBILE_PATTERN = Pattern.compile("(?<!\\d)(1[3-9]\\d{9})(?!\\d)");
    private static final Pattern QQ_PATTERN = Pattern.compile("(?i)(?:qq)\\s*[:：]?\\s*([1-9]\\d{4,11})");
    private static final Pattern WECHAT_PATTERN = Pattern.compile("(?i)(?:微信|wx|wechat)\\s*[:：]?\\s*([a-z][-_a-z0-9]{5,19})");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(?<!\\d)(\\d{17}[0-9xX])(?!\\d)");

    public AutoReviewDecision reviewPost(String title, String content) {
        String normalizedTitle = title == null ? "" : title;
        String normalizedContent = content == null ? "" : content;

        RuleDecision rule = applyRuleScreening(normalizedTitle + "\n" + normalizedContent);
        if (rule.status != AutoReviewStatus.PASS) {
            return new AutoReviewDecision(rule.status, "RULE", rule.reason, nowUtc());
        }

        if (!properties.getLlm().isEnabled()) {
            return new AutoReviewDecision(AutoReviewStatus.PASS, "RULE", null, nowUtc());
        }
        LlmDecision llmDecision = applyLlmScreening(normalizedTitle, normalizedContent);
        return new AutoReviewDecision(llmDecision.status, llmDecision.provider, llmDecision.reason, nowUtc());
    }

    /**
     * For comments we only block obvious privacy leakage in MVP.
     */
    public void assertCommentContentAllowed(String content) {
        RuleDecision decision = applyPrivacyRules(content == null ? "" : content);
        if (decision.status != AutoReviewStatus.PASS) {
            throw new ResponseStatusException(BAD_REQUEST, decision.reason);
        }
    }

    private RuleDecision applyRuleScreening(String text) {
        RuleDecision privacy = applyPrivacyRules(text);
        if (privacy.status != AutoReviewStatus.PASS) {
            return privacy;
        }

        if (properties.getSpamKeywords() != null) {
            for (String keyword : properties.getSpamKeywords()) {
                if (keyword == null || keyword.isBlank()) {
                    continue;
                }
                if (text.contains(keyword.trim())) {
                    return new RuleDecision(AutoReviewStatus.FLAG, "Spam keyword detected");
                }
            }
        }

        return new RuleDecision(AutoReviewStatus.PASS, null);
    }

    private RuleDecision applyPrivacyRules(String text) {
        if (text == null || text.isBlank()) {
            return new RuleDecision(AutoReviewStatus.PASS, null);
        }
        if (MOBILE_PATTERN.matcher(text).find()) {
            return new RuleDecision(AutoReviewStatus.FLAG, "Contains mobile number");
        }
        if (WECHAT_PATTERN.matcher(text).find()) {
            return new RuleDecision(AutoReviewStatus.FLAG, "Contains WeChat ID");
        }
        if (QQ_PATTERN.matcher(text).find()) {
            return new RuleDecision(AutoReviewStatus.FLAG, "Contains QQ number");
        }
        if (ID_CARD_PATTERN.matcher(text).find()) {
            return new RuleDecision(AutoReviewStatus.FLAG, "Contains ID card number");
        }
        return new RuleDecision(AutoReviewStatus.PASS, null);
    }

    private LlmDecision applyLlmScreening(String title, String content) {
        String endpoint = properties.getLlm().getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return new LlmDecision(AutoReviewStatus.UNKNOWN, "LLM", "LLM moderation endpoint is not configured");
        }

        WebClient client = webClientBuilder.build();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("content", content);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri(endpoint.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(properties.getLlm().getTimeout())
                    .block();

            String status = response == null ? null : Objects.toString(response.get("status"), null);
            String reason = response == null ? null : Objects.toString(response.get("reason"), null);
            if (status == null || status.isBlank()) {
                return new LlmDecision(AutoReviewStatus.UNKNOWN, "LLM", "LLM moderation returned empty status");
            }
            return switch (status.trim().toUpperCase()) {
                case "SAFE" -> new LlmDecision(AutoReviewStatus.PASS, "LLM", null);
                case "UNSAFE" -> new LlmDecision(AutoReviewStatus.UNSAFE, "LLM", reason != null ? reason : "LLM flagged content");
                case "UNKNOWN" -> new LlmDecision(AutoReviewStatus.UNKNOWN, "LLM", reason != null ? reason : "LLM returned UNKNOWN");
                default -> new LlmDecision(AutoReviewStatus.UNKNOWN, "LLM", "Unsupported LLM status: " + status);
            };
        } catch (Exception ex) {
            return new LlmDecision(AutoReviewStatus.UNKNOWN, "LLM", "LLM moderation call failed");
        }
    }

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record RuleDecision(AutoReviewStatus status, String reason) {
    }

    private record LlmDecision(AutoReviewStatus status, String provider, String reason) {
    }

    public enum AutoReviewStatus {
        UNSET,
        PASS,
        FLAG,
        UNSAFE,
        UNKNOWN
    }

    public record AutoReviewDecision(
            AutoReviewStatus status,
            String provider,
            String reason,
            OffsetDateTime reviewedAt
    ) {
    }
}
