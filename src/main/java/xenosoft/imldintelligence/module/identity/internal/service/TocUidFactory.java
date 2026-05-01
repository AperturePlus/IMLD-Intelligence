package xenosoft.imldintelligence.module.identity.internal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Generates stable ToC identifiers without storing raw mobile in keys.
 */
@Component
public class TocUidFactory {

    private final String pepper;

    public TocUidFactory(@Value("${imld.security.data-encryption-key:}") String pepper) {
        this.pepper = pepper == null ? "" : pepper;
    }

    public String mobileHash(String mobile) {
        if (pepper.isBlank()) {
            throw new IllegalStateException("imld.security.data-encryption-key must be configured for mobile login");
        }
        String normalized = normalizeMobile(mobile);
        return base64UrlSha256(pepper + "|" + normalized);
    }

    public String mobileTocUid(String mobile) {
        return "MOBILE:" + mobileHash(mobile);
    }

    public String wechatTocUid(String unionid, String openid) {
        if (hasText(unionid)) {
            return "WXU:" + unionid.trim();
        }
        if (hasText(openid)) {
            return "WXO:" + openid.trim();
        }
        throw new IllegalArgumentException("openid is required");
    }

    private String base64UrlSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash input", e);
        }
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null) {
            return "";
        }
        return mobile.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

