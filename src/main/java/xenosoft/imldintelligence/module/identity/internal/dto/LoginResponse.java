package xenosoft.imldintelligence.module.identity.internal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Login 响应对象，封装Login相关的返回字段。
 */
public record LoginResponse(
        @NotNull(message = "Auth token must not be null")
        @Valid
        AuthToken authToken,
        @NotNull(message = "Principal must not be null")
        @Valid
        AuthenticatedUserDto principal
) {
}
