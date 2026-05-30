package xenosoft.imldintelligence.module.community.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Community module settings.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imld.community")
public class CommunityProperties {
    /**
     * Community scope mode.
     *
     * <p>GLOBAL means all ToC community data is stored in a single tenant specified by {@link #globalTenantCode}.</p>
     */
    private Scope scope = Scope.TENANT;

    /**
     * Tenant code used when {@link #scope} is GLOBAL.
     */
    private String globalTenantCode = "";

    public enum Scope {
        TENANT,
        GLOBAL
    }
}

