package xenosoft.imldintelligence.module.license.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * 部署模式配置属性，定义系统当前运行形态及相关部署参数。
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "imld.deployment")
public class DeploymentProperties {
    private static final String MODE_SAAS = "saas";
    private static final String MODE_PRIVATE = "private";
    private static final String MODE_HYBRID = "hybrid";
    private static final Set<String> SUPPORTED_MODES = Set.of(MODE_SAAS, MODE_PRIVATE);

    private String mode = MODE_SAAS;
    private String tenantMode = "multi-tenant";

    public boolean isPrivateMode() {
        return MODE_PRIVATE.equals(normalizedMode());
    }

    public boolean isSaasMode() {
        return MODE_SAAS.equals(normalizedMode());
    }

    public boolean isSupportedMode() {
        return SUPPORTED_MODES.contains(normalizedMode());
    }

    public String normalizedMode() {
        if (mode == null) {
            return MODE_SAAS;
        }
        String normalized = mode.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return MODE_SAAS;
        }
        // Backward-compatible alias: old "hybrid" runs as private mode with bridge enabled.
        if (MODE_HYBRID.equals(normalized)) {
            return MODE_PRIVATE;
        }
        return normalized;
    }
}
