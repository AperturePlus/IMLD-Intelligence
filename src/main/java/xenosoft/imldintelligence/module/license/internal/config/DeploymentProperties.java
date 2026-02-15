package xenosoft.imldintelligence.module.license.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@ConfigurationProperties(prefix = "imld.deployment")
public class DeploymentProperties {
    private String mode = "saas";
    private String tenantMode = "multi-tenant";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTenantMode() {
        return tenantMode;
    }

    public void setTenantMode(String tenantMode) {
        this.tenantMode = tenantMode;
    }

    public boolean isPrivateMode() {
        return "private".equalsIgnoreCase(mode);
    }

    public String normalizedMode() {
        return mode == null ? "" : mode.toLowerCase(Locale.ROOT);
    }
}
