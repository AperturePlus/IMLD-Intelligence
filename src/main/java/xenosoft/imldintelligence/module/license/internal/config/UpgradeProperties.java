package xenosoft.imldintelligence.module.license.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "imld.upgrade")
public class UpgradeProperties {
    private boolean checkEnabled = false;
    private String releaseChannel = "lts";
    private String deliveryMode = "offline-package";
    private String manifestFilePath = "";
    private Entitlement entitlement = new Entitlement();

    @Setter
    @Getter
    public static class Entitlement {
        private boolean enforceSupportWindow = true;
        private boolean allowSecurityPatchAfterExpiry = true;

    }
}
