package xenosoft.imldintelligence.module.identity.internal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(IdentitySecurityProperties.class)
public class IdentitySecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http,
                                               IdentitySecurityProperties properties,
                                               JwtAuthenticationFilter jwtAuthenticationFilter,
                                               AuthenticationEntryPoint authenticationEntryPoint,
                                               AccessDeniedHandler accessDeniedHandler,
                                               ObjectProvider<ModuleRequestAuthorizationCustomizer> authorizationCustomizers) throws Exception {
        properties.validateWhenSecurityEnabled();

        applyStatelessDefaults(http)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> {
                    authorizationCustomizers.orderedStream().forEach(customizer -> customizer.customize(authorize));
                    authorize.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain openSecurityFilterChain(HttpSecurity http) throws Exception {
        return applyStatelessDefaults(http)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .anonymous(Customizer.withDefaults())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                    AuthenticationEntryPoint authenticationEntryPoint) {
        return new JwtAuthenticationFilter(jwtUtil, authenticationEntryPoint);
    }

    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return new JsonAccessDeniedHandler(objectMapper);
    }

    private HttpSecurity applyStatelessDefaults(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }
}
