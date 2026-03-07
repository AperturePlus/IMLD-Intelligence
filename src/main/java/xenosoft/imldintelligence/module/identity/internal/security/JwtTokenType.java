package xenosoft.imldintelligence.module.identity.internal.security;

import io.jsonwebtoken.JwtException;

import java.util.Arrays;

public enum JwtTokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String claimValue;

    JwtTokenType(String claimValue) {
        this.claimValue = claimValue;
    }

    public String claimValue() {
        return claimValue;
    }

    public static JwtTokenType fromClaim(String value) {
        if (value == null || value.isBlank()) {
            throw new JwtException("JWT token_type claim is missing");
        }
        return Arrays.stream(values())
                .filter(tokenType -> tokenType.claimValue.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new JwtException("Unsupported JWT token type: " + value));
    }
}
