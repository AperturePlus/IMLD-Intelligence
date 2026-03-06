package xenosoft.imldintelligence.module.identity.internal.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthToken {
    private String accessToken;

    private String refreshToken;

    private Long expiresIn;

    private final String type="Bearer";

}
