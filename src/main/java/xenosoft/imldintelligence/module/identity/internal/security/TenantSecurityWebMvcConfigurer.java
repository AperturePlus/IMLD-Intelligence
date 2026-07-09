package xenosoft.imldintelligence.module.identity.internal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers {@link TenantHeaderInterceptor} on all paths except the configured public paths.
 *
 * <p>Public paths are sourced from {@link IdentitySecurityProperties#getPublicPaths()} (the same
 * list used for {@code permitAll} in the security filter chain) plus common static/actuator paths,
 * so there is one source of truth for "public."</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class TenantSecurityWebMvcConfigurer implements WebMvcConfigurer {
    private static final String[] STATIC_PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/error"
    };

    private final TenantHeaderInterceptor tenantHeaderInterceptor;
    private final IdentitySecurityProperties properties;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        List<String> exclude = new ArrayList<>(properties.getPublicPaths());
        exclude.addAll(List.of(STATIC_PUBLIC_PATHS));

        registry.addInterceptor(tenantHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(exclude.toArray(String[]::new));
    }
}
