package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.service.IAuthService;

/**
 * 认证服务实现类，负责登录、令牌签发、刷新、校验与退出处理。
 */
@Service
public class AuthServiceImpl implements IAuthService {

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthToken generateToken(Role role) {
        throw unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthToken refreshToken(String token) {
        throw unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateToken(String token) {
        throw unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revokeToken(String token) {
        throw unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthToken login(LoginRequest loginRequest) {
        throw unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(String refreshToken) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Auth flow is not implemented yet.");
    }
}
