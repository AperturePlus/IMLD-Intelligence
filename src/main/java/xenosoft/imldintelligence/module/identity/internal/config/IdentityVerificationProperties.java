package xenosoft.imldintelligence.module.identity.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imld.identity.verification")
public class IdentityVerificationProperties {
    private int codeLength = 6;
    private Duration expiresIn = Duration.ofMinutes(10);
    private Duration resendInterval = Duration.ofSeconds(60);
    private int maxVerifyAttempts = 5;
    private String emailSubjectPrefix = "[IMLD]";
    private String fromAddress = "";
    private Sms sms = new Sms();

    @Getter
    @Setter
    public static class Sms {
        private boolean enabled = true;
        private String provider = "mock";
    }
}
