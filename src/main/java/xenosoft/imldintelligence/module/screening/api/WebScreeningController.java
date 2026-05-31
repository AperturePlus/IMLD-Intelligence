package xenosoft.imldintelligence.module.screening.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.screening.api.dto.ScreeningApiDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
public class WebScreeningController implements WebScreeningControllerContract {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Override
    public ApiResponse<ScreeningApiDtos.Response.ScreeningOverviewResponse> getOverview(Long tenantId,
                                                                                        String from,
                                                                                        String to) {
        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before or equal to to");
        }

        return ApiResponse.success(emptyOverview());
    }

    private ScreeningApiDtos.Response.ScreeningOverviewResponse emptyOverview() {
        return new ScreeningApiDtos.Response.ScreeningOverviewResponse(
                nowUtc(),
                List.of(
                        statCard("累计筛查总人数", "User", "#409EFF", null),
                        statCard("检出高危/阳性", "WarnTriangleFilled", "#f56c6c", "例"),
                        statCard("基因突变携带率", "TrendCharts", "#e6a23c", "%"),
                        statCard("AI 干预采纳数", "MagicStick", "#67c23a", "次")
                ),
                List.of(),
                List.of(),
                new ScreeningApiDtos.Response.AiEfficiencyMetrics(ZERO, "0%", "--"),
                List.of()
        );
    }

    private ScreeningApiDtos.Response.StatCardItem statCard(String title, String icon, String color, String suffix) {
        return new ScreeningApiDtos.Response.StatCardItem(title, ZERO, color, icon, ZERO, suffix);
    }

    private LocalDate parseDate(String value, String fieldName) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " must be ISO-8601 date (yyyy-MM-dd)", ex);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0).toString();
    }
}
