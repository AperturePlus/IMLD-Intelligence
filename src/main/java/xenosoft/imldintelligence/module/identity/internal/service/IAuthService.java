package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.Role;

public interface IAuthService {
    AuthToken generateToken(Role role);

    AuthToken refreshToken(String token);

    boolean validateToken(String token);

    void revokeToken(String token);

    AuthToken login(LoginRequest loginRequest);

    void logout(String refreshToken);

}
