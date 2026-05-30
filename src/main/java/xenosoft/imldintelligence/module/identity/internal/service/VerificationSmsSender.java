package xenosoft.imldintelligence.module.identity.internal.service;

import java.time.Duration;

public interface VerificationSmsSender {
    void sendVerificationCode(String mobile, String scenario, String code, Duration ttl);
}

