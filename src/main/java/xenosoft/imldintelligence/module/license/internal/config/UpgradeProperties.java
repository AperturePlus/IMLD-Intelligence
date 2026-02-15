package xenosoft.imldintelligence.module.license.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "imld.upgrade")
public class UpgradeProperties {
    private boolean checkEnabled = false;
    private String releaseChannel = "lts";
    private String deliveryMode = "offline-package";
    private String manifestFilePath = "";
    private Entitlement entitlement = new Entitlement();

    public boolean isCheckEnabled() {
        return checkEnabled;
    }

    public void setCheckEnabled(boolean checkEnabled) {
        this.checkEnabled = checkEnabled;
    }

    public String getReleaseChannel() {
        return releaseChannel;
    }

    public void setReleaseChannel(String releaseChannel) {
        this.releaseChannel = releaseChannel;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getManifestFilePath() {
        return manifestFilePath;
    }

    public void setManifestFilePath(String manifestFilePath) {
        this.manifestFilePath = manifestFilePath;
    }

    public Entitlement getEntitlement() {
        return entitlement;
    }

    public void setEntitlement(Entitlement entitlement) {
        this.entitlement = entitlement;
    }

    public static class Entitlement {
        private boolean enforceSupportWindow = true;
        private boolean allowSecurityPatchAfterExpiry = true;

        public boolean isEnforceSupportWindow() {
            return enforceSupportWindow;
        }

        public void setEnforceSupportWindow(boolean enforceSupportWindow) {
            this.enforceSupportWindow = enforceSupportWindow;
        }

        public boolean isAllowSecurityPatchAfterExpiry() {
            return allowSecurityPatchAfterExpiry;
        }

        public void setAllowSecurityPatchAfterExpiry(boolean allowSecurityPatchAfterExpiry) {
            this.allowSecurityPatchAfterExpiry = allowSecurityPatchAfterExpiry;
        }
    }
}
