package xenosoft.imldintelligence.module.license.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "imld.deployment")
public class DeploymentProperties {
    private String mode = "SaaS";
    private String tenantMode = "multi-tenant";

    public boolean isPrivateMode() {
        return "private".equalsIgnoreCase(mode);
    }

    public String normalizedMode() {
        return mode == null ? "" : mode.toLowerCase(Locale.ROOT);
    }
}
