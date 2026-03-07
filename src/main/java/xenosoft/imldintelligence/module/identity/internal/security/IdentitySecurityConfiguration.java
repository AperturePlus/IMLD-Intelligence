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

/**
 * 身份安全配置类，按配置在 JWT 鉴权链与开放访问链之间切换。
 *
 * <p>开启安全能力时启用无状态 JWT 认证；关闭时放行全部请求，便于本地开发或特定部署模式降级运行。</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(IdentitySecurityProperties.class)
public class IdentitySecurityConfiguration {

    /**
     * 构建启用 JWT 时使用的无状态安全过滤链。
     *
     * @param http Spring Security HTTP 配置对象
     * @param properties 身份安全配置属性
     * @param jwtAuthenticationFilter JWT 认证过滤器
     * @param authenticationEntryPoint 未认证请求处理入口
     * @param accessDeniedHandler 鉴权失败处理器
     * @param authorizationCustomizers 各业务模块注册的请求授权定制器
     * @return 启用鉴权后的安全过滤链
     * @throws Exception 构建过滤链过程中发生配置错误时抛出
     */
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

    /**
     * 构建关闭安全能力时使用的开放访问过滤链。
     *
     * @param http Spring Security HTTP 配置对象
     * @return 允许匿名访问的安全过滤链
     * @throws Exception 构建过滤链过程中发生配置错误时抛出
     */
    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "false", matchIfMissing = true)
    SecurityFilterChain openSecurityFilterChain(HttpSecurity http) throws Exception {
        return applyStatelessDefaults(http)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .anonymous(Customizer.withDefaults())
                .build();
    }

    /**
     * 创建 JWT 认证过滤器 Bean。
     *
     * @param jwtUtil JWT 工具类
     * @param authenticationEntryPoint 未认证请求处理入口
     * @return JWT 认证过滤器实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                    AuthenticationEntryPoint authenticationEntryPoint) {
        return new JwtAuthenticationFilter(jwtUtil, authenticationEntryPoint);
    }

    /**
     * 创建未认证请求处理入口 Bean。
     *
     * @param objectMapper 用于输出错误响应的 JSON 序列化器
     * @return 未认证请求处理入口
     */
    @Bean
    @ConditionalOnProperty(prefix = "imld.security", name = "enabled", havingValue = "true")
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    /**
     * 创建鉴权失败处理器 Bean。
     *
     * @param objectMapper 用于输出错误响应的 JSON 序列化器
     * @return 鉴权失败处理器
     */
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
