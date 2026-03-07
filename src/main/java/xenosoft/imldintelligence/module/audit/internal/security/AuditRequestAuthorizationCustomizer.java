package xenosoft.imldintelligence.module.audit.internal.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.identity.internal.security.ModuleRequestAuthorizationCustomizer;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;

import java.util.Set;

@Component
@Order(200)
@ConditionalOnProperty(prefix = "imld.audit", name = {"enabled", "query-api-enabled"}, havingValue = "true", matchIfMissing = true)
public class AuditRequestAuthorizationCustomizer implements ModuleRequestAuthorizationCustomizer {
    private final AuditProperties auditProperties;

    public AuditRequestAuthorizationCustomizer(AuditProperties auditProperties) {
        this.auditProperties = auditProperties;
    }

    @Override
    public void customize(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        Set<String> allowedAuthorities = RoleAuthorityUtils.expandAuthorityNames(auditProperties.getQueryRoleAllowlist());
        if (allowedAuthorities.isEmpty()) {
            requests.requestMatchers("/api/v1/audit/**").denyAll();
            return;
        }
        requests.requestMatchers("/api/v1/audit/**").hasAnyAuthority(allowedAuthorities.toArray(String[]::new));
    }
}
