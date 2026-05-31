package xenosoft.imldintelligence.module.integration.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.integration.api.dto.DevPatientImportApiDtos;

/**
 * Dev-only patient import preview contract for frontend integration.
 */
@Validated
@RequestMapping("/api/v1/web/integration/imports/patient")
public interface DevPatientImportApi {

    @PostMapping("/his-lis")
    ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromHisLis(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody(required = false)
            DevPatientImportApiDtos.Request.HisLisPatientImportRequest request
    );

    @PostMapping("/image")
    ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromImage(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody(required = false)
            DevPatientImportApiDtos.Request.OcrPatientImportRequest request
    );

    @PostMapping("/pdf")
    ApiResponse<DevPatientImportApiDtos.Response.PatientImportPreview> previewFromPdf(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody(required = false)
            DevPatientImportApiDtos.Request.OcrPatientImportRequest request
    );
}
