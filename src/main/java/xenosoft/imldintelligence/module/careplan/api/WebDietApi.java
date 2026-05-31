package xenosoft.imldintelligence.module.careplan.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.careplan.api.dto.DietApiDtos;

/**
 * Web 医生工作站饮食管理门面。
 *
 * <p>接口服务于院内膳食处方页面，私有化部署下不触发云端出域通信。</p>
 */
@Validated
@RequestMapping("/api/v1/web/diet")
public interface WebDietApi {

    @GetMapping({"/patients", "/patients/"})
    ApiResponse<DietApiDtos.Response.DietPatientsResponse> listDietPatients(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam(value = "keyword", required = false)
            String keyword
    );

    @GetMapping({"/patients/{patientNo}/plan", "/patients/{patientNo}/plan/"})
    ApiResponse<DietApiDtos.Response.DietPlanResponse> getDietPlan(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("patientNo")
            @NotBlank(message = "patientNo must not be blank")
            @Size(max = 64, message = "patientNo must be at most 64 characters")
            String patientNo
    );

    @PostMapping({"/patients/{patientNo}/regenerate", "/patients/{patientNo}/regenerate/"})
    ApiResponse<DietApiDtos.Response.RegenerateDietPlanResponse> regenerateDietPlan(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("patientNo")
            @NotBlank(message = "patientNo must not be blank")
            @Size(max = 64, message = "patientNo must be at most 64 characters")
            String patientNo
    );

    @PostMapping({"/patients/{patientNo}/push", "/patients/{patientNo}/push/"})
    ApiResponse<DietApiDtos.Response.PushDietPlanResponse> pushDietPlan(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("patientNo")
            @NotBlank(message = "patientNo must not be blank")
            @Size(max = 64, message = "patientNo must be at most 64 characters")
            String patientNo
    );
}
