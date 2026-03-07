package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.service.IAuthService;

@Service
public class AuthServiceImpl implements IAuthService {

    @Override
    public AuthToken generateToken(Role role) {
        throw unsupported();
    }

    @Override
    public AuthToken refreshToken(String token) {
        throw unsupported();
    }

    @Override
    public boolean validateToken(String token) {
        throw unsupported();
    }

    @Override
    public void revokeToken(String token) {
        throw unsupported();
    }

    @Override
    public AuthToken login(LoginRequest loginRequest) {
        throw unsupported();
    }

    @Override
    public void logout(String refreshToken) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Auth flow is not implemented yet.");
    }
}
