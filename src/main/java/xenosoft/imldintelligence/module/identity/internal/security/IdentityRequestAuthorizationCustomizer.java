package xenosoft.imldintelligence.module.identity.internal.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentityRequestAuthorizationCustomizer implements ModuleRequestAuthorizationCustomizer {
    private final IdentitySecurityProperties properties;

    public IdentityRequestAuthorizationCustomizer(IdentitySecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public void customize(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests) {
        List<String> publicPaths = properties.getPublicPaths();
        if (publicPaths == null || publicPaths.isEmpty()) {
            return;
        }
        requests.requestMatchers(publicPaths.toArray(String[]::new)).permitAll();
    }
}
