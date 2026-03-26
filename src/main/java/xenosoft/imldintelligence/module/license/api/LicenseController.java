package xenosoft.imldintelligence.module.license.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.common.RequireAnyRole;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.license.api.dto.LicenseApiDtos;
import xenosoft.imldintelligence.module.license.internal.service.impl.LicenseManagementService;

/**
 * HTTP controller implementation for license activation and validation operations.
 */
@RestController
@RequiredArgsConstructor
public class LicenseController implements LicenseControllerContract {
    private final LicenseManagementService licenseManagementService;

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.LicenseSummaryResponse> getLicenseStatus() {
        return ApiResponse.success(licenseManagementService.getLicenseStatus());
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.LicenseSummaryResponse> uploadLicenseEnvelope(
            LicenseApiDtos.Request.UploadLicenseEnvelopeRequest request) {
        return ApiResponse.success(licenseManagementService.uploadLicenseEnvelope(request));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.LicenseValidationResponse> activateLicense(
            LicenseApiDtos.Request.ActivateLicenseRequest request) {
        return ApiResponse.success(licenseManagementService.activateLicense(request));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.LicenseValidationResponse> validateLicense(
            LicenseApiDtos.Request.ValidateLicenseRequest request) {
        return ApiResponse.success(licenseManagementService.validateLicense(request));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.FingerprintResponse> getFingerprint() {
        return ApiResponse.success(licenseManagementService.getFingerprint());
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<LicenseApiDtos.Response.ReleaseEligibilityResponse> evaluateReleaseEligibility(
            LicenseApiDtos.Query.ReleaseEligibilityQuery query) {
        return ApiResponse.success(licenseManagementService.evaluateReleaseEligibility(query));
    }
}
