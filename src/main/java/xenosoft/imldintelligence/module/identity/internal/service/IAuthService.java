package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

public interface IAuthService {
    String generateToken(Role role);

    String refreshToken(String token);

    boolean validateToken(String token);

    void revokeToken(String token);

    AuthToken login(LoginRequest loginRequest);

}
