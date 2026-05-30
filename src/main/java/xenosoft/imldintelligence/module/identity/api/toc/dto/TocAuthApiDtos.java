package xenosoft.imldintelligence.module.identity.api.toc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * ToC auth DTO catalog.
 */
public final class TocAuthApiDtos {
    private TocAuthApiDtos() {
    }

    public static final class Request {
        private Request() {
        }

        public record WechatLoginRequest(
                @NotBlank(message = "jsCode must not be blank")
                @Size(max = 128, message = "jsCode must be at most 128 characters")
                String jsCode,
                @Size(max = 100, message = "nickname must be at most 100 characters")
                String nickname
        ) {
        }

        public record SendPhoneCodeRequest(
                @NotBlank(message = "mobile must not be blank")
                @Size(max = 32, message = "mobile must be at most 32 characters")
                String mobile
        ) {
        }

        public record PhoneLoginRequest(
                @NotBlank(message = "mobile must not be blank")
                @Size(max = 32, message = "mobile must be at most 32 characters")
                String mobile,
                @NotBlank(message = "code must not be blank")
                @Size(max = 16, message = "code must be at most 16 characters")
                String code
        ) {
        }

        public record RefreshRequest(
                @NotBlank(message = "refreshToken must not be blank")
                String refreshToken
        ) {
        }

        public record LogoutRequest(
                @NotBlank(message = "refreshToken must not be blank")
                String refreshToken
        ) {
        }
    }

    public static final class Response {
        private Response() {
        }

        public record TocAuthSessionResponse(
                String accessToken,
                String refreshToken,
                OffsetDateTime expiresAt,
                Long tenantId,
                Long tocUserId,
                String nickname
        ) {
        }

        public record PhoneCodeSendResponse(
                String purpose,
                OffsetDateTime expiresAt,
                long resendAfterSeconds
        ) {
        }
    }
}
