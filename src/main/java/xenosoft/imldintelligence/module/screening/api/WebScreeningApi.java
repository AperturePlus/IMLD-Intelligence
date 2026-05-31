package xenosoft.imldintelligence.module.screening.api;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.screening.api.dto.ScreeningApiDtos;

/**
 * Web 医生工作站筛查数据门面。
 *
 * <p>该门面只返回前端筛查总览所需的聚合字段，避免暴露患者明细和敏感原文。</p>
 */
@Validated
@RequestMapping("/api/v1/web/screening")
public interface WebScreeningApi {

    @GetMapping({"/overview", "/overview/"})
    ApiResponse<ScreeningApiDtos.Response.ScreeningOverviewResponse> getOverview(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @RequestParam(value = "from", required = false)
            String from,
            @RequestParam(value = "to", required = false)
            String to
    );
}
