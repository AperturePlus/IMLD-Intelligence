package xenosoft.imldintelligence.module.identity.internal.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * 模块级请求授权定制器，用于向统一安全链注册模块专属的 URL 授权规则。
 */
@FunctionalInterface
public interface ModuleRequestAuthorizationCustomizer {
    /**
     * 向请求授权注册表追加当前模块的授权规则。
     *
     * @param requests 待扩展的请求授权注册表
     */
    void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry requests);
}
