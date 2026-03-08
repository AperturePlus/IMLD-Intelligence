package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

/**
 * 认证服务接口，封装登录、令牌签发、刷新、校验与注销能力。
 */
public interface AuthService {
    /**
     * 根据角色信息生成认证令牌。
     *
     * @param role 用于签发令牌的角色信息
     * @return 生成后的认证令牌
     */
    AuthToken generateToken(Role role);

    /**
     * 使用现有刷新令牌换取新的认证令牌。
     *
     * @param token 客户端提交的刷新令牌
     * @return 新的认证令牌
     */
    AuthToken refreshToken(String token);

    /**
     * 校验令牌是否仍然有效。
     *
     * @param token 待校验的令牌字符串
     * @return 令牌有效时返回 {@code true}，否则返回 {@code false}
     */
    boolean validateToken(String token);

    /**
     * 撤销指定令牌，使其后续不可再用。
     *
     * @param token 待撤销的令牌字符串
     */
    void revokeToken(String token);

    /**
     * 处理登录请求并签发认证令牌。
     *
     * @param loginRequest 登录请求参数
     * @return 登录成功后签发的认证令牌
     */
    AuthToken login(LoginRequest loginRequest);

    /**
     * 处理退出登录请求并使刷新令牌失效。
     *
     * @param refreshToken 当前会话使用的刷新令牌
     */
    void logout(String refreshToken);

}
