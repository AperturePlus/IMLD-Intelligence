package xenosoft.imldintelligence.module.license.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 许可证模块 DTO 分类目录。
 *
 * <p>许可证是部署级授权能力，不直接绑定租户头，而是围绕医院部署、支持期、
 * 升级准入和离线验签展开。</p>
 */
public final class LicenseApiDtos {
    private LicenseApiDtos() {
    }

    /**
     * 查询类 DTO。
     */
    public static final class Query {
        private Query() {
        }

        /**
         * 发布清单准入校验条件。
         */
        public record ReleaseEligibilityQuery(
                @NotNull(message = "releaseDate must not be null")
                LocalDate releaseDate,
                Boolean securityPatch,
                @Size(max = 32, message = "channel must be at most 32 characters")
                String channel
        ) {
        }
    }

    /**
     * 写入类 DTO。
     */
    public static final class Request {
        private Request() {
        }

        /**
         * 上传许可证信封。
         */
        public record UploadLicenseEnvelopeRequest(
                @NotNull(message = "payload must not be null")
                JsonNode payload,
                @NotBlank(message = "signature must not be blank")
                String signature,
                @Size(max = 128, message = "activationCode must be at most 128 characters")
                String activationCode
        ) {
        }

        /**
         * 激活许可证。
         */
        public record ActivateLicenseRequest(
                @NotBlank(message = "activationCode must not be blank")
                @Size(max = 128, message = "activationCode must be at most 128 characters")
                String activationCode,
                @NotBlank(message = "machineFingerprintHash must not be blank")
                @Size(max = 128, message = "machineFingerprintHash must be at most 128 characters")
                String machineFingerprintHash
        ) {
        }

        /**
         * 校验许可证有效性。
         */
        public record ValidateLicenseRequest(
                @NotNull(message = "payload must not be null")
                JsonNode payload,
                @NotBlank(message = "signature must not be blank")
                String signature
        ) {
        }
    }

    /**
     * 响应类 DTO。
     */
    public static final class Response {
        private Response() {
        }

        /**
         * 许可证摘要。
         */
        public record LicenseSummaryResponse(
                String licenseId,
                String hospitalId,
                String deploymentMode,
                String issuer,
                OffsetDateTime issuedAt,
                LocalDate supportStartDate,
                LocalDate supportEndDate,
                List<String> features,
                List<String> scenarios,
                boolean machineBound,
                boolean activated
        ) {
        }

        /**
         * 许可证校验结果。
         */
        public record LicenseValidationResponse(
                boolean valid,
                String reason,
                String machineFingerprintHash,
                boolean supportActive
        ) {
        }

        /**
         * 升级准入结果。
         */
        public record ReleaseEligibilityResponse(
                boolean allowed,
                String reason,
                boolean securityPatchOverride
        ) {
        }

        /**
         * 指纹摘要。
         */
        public record FingerprintResponse(
                String cpuIdHash,
                String macAddrHash,
                String hdIdHash,
                String osInfoHash,
                String machineFingerprintHash
        ) {
        }
    }
}
