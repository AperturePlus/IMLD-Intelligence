package xenosoft.imldintelligence.module.clinical.api;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.clinical.api.dto.WebPatientApiDtos;

/**
 * Web 医生工作站患者门面。
 *
 * <p>该契约面向前端业务页面，内部仍复用身份域患者主数据，避免把患者列表暴露为 identity 路径。</p>
 */
@Validated
@RequestMapping("/api/v1/web/patients")
public interface WebPatientApi {

    @GetMapping({"", "/"})
    ApiResponse<WebPatientApiDtos.Response.PatientListResponse> listPatients(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam(value = "keyword", required = false)
            String keyword
    );
}
