package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AuthToken(
        @NotBlank(message = "Access token must not be blank")
        String accessToken,
        @NotBlank(message = "Refresh token must not be blank")
        String refreshToken,
        @Positive(message = "ExpiresIn must be greater than 0")
        Long expiresIn,
        @NotBlank(message = "Token type must not be blank")
        @Size(max = 32, message = "Token type must be at most 32 characters")
        String tokenType
) {
    public AuthToken {
        tokenType = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;
    }

    public static AuthToken bearer(String accessToken, String refreshToken, Long expiresIn) {
        return new AuthToken(accessToken, refreshToken, expiresIn, "Bearer");
    }
}
