package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationSmsSender;

import java.time.Duration;

/**
 * Mock SMS sender implementation.
 *
 * <p>It logs verification codes for local debugging and must never be enabled in production modes.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "imld.identity.verification.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "imld.identity.verification.sms", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockVerificationSmsSender implements VerificationSmsSender {

    @Override
    public void sendVerificationCode(String mobile, String scenario, String code, Duration ttl) {
        String masked = maskMobile(mobile);
        log.warn("[DEV-ONLY] SMS verification code (scenario={}, mobile={}, ttlSeconds={}): {}",
                scenario,
                masked,
                ttl == null ? null : ttl.toSeconds(),
                code);
    }

    private String maskMobile(String mobile) {
        if (mobile == null) {
            return "";
        }
        String value = mobile.trim();
        if (value.length() <= 4) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }
}

