package xenosoft.imldintelligence.module.audit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.api.dto.AuditLogResponse;
import xenosoft.imldintelligence.module.audit.api.dto.ModelInvocationLogResponse;
import xenosoft.imldintelligence.module.audit.api.dto.PagedResponse;
import xenosoft.imldintelligence.module.audit.api.dto.SensitiveAccessLogResponse;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.audit.internal.security.AuditQueryAccessGuard;
import xenosoft.imldintelligence.module.audit.internal.service.AuditQueryService;
import xenosoft.imldintelligence.module.audit.internal.service.model.PageResult;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

import java.time.OffsetDateTime;

/**
 * Exposes read-only endpoints for querying audit data within a tenant boundary.
 *
 * <p>Each request must pass the audit query access guard and provide a valid tenant identifier
 * through the {@code X-Tenant-Id} header.</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@ConditionalOnProperty(prefix = "imld.audit", name = {"enabled", "query-api-enabled"}, havingValue = "true", matchIfMissing = true)
@Tag(name = "Audit & Compliance", description = "审计日志查询 APIs - 提供通用审计、敏感数据访问和模型调用审计日志查询")
public class AuditQueryController {
    private final AuditQueryService auditQueryService;
    private final AuditQueryAccessGuard accessGuard;
    private final AuditProperties auditProperties;

    public AuditQueryController(AuditQueryService auditQueryService,
                                AuditQueryAccessGuard accessGuard,
                                AuditProperties auditProperties) {
        this.auditQueryService = auditQueryService;
        this.accessGuard = accessGuard;
        this.auditProperties = auditProperties;
    }

    /**
     * Queries generic audit logs.
     *
     * @param request current HTTP request used for authorization checks
     * @param tenantHeader tenant identifier supplied through {@code X-Tenant-Id}
     * @param userId optional user identifier filter
     * @param action optional action code filter
     * @param resourceType optional resource type filter
     * @param resourceId optional resource identifier filter
     * @param traceId optional distributed trace identifier filter
     * @param from optional inclusive start timestamp
     * @param to optional inclusive end timestamp
     * @param page optional zero-based page index
     * @param size optional page size
     * @return paged audit log response payload
     * @throws ResponseStatusException if access is denied or request parameters are invalid
     */
    @GetMapping("/logs")
    @Operation(
            summary = "查询通用审计日志",
            description = "分页查询系统操作审计日志，支持按用户、操作、资源类型等条件过滤。需要 SYSTEM_ADMIN 或 COMPLIANCE_AUDITOR 角色。",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    public PagedResponse<AuditLogResponse> queryAuditLogs(HttpServletRequest request,
                                                          @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
                                                          @RequestHeader(value = AuditHeaderNames.TENANT_ID, required = false) String tenantHeader,
                                                          @Parameter(description = "用户 ID 过滤条件")
                                                          @RequestParam(value = "userId", required = false) Long userId,
                                                          @Parameter(description = "操作代码过滤条件")
                                                          @RequestParam(value = "action", required = false) String action,
                                                          @Parameter(description = "资源类型过滤条件")
                                                          @RequestParam(value = "resourceType", required = false) String resourceType,
                                                          @Parameter(description = "资源 ID 过滤条件")
                                                          @RequestParam(value = "resourceId", required = false) String resourceId,
                                                          @Parameter(description = "分布式追踪 ID 过滤条件")
                                                          @RequestParam(value = "traceId", required = false) String traceId,
                                                          @Parameter(description = "开始时间（ISO 8601 格式）")
                                                          @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                          @Parameter(description = "结束时间（ISO 8601 格式）")
                                                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                          @Parameter(description = "页码（从0开始）")
                                                          @RequestParam(value = "page", required = false) Integer page,
                                                          @Parameter(description = "每页大小")
                                                          @RequestParam(value = "size", required = false) Integer size) {
        accessGuard.assertAllowed(request);

        AuditLogQuery query = new AuditLogQuery();
        query.setTenantId(parseTenantId(tenantHeader));
        query.setUserId(userId);
        query.setAction(trimToNull(action));
        query.setResourceType(trimToNull(resourceType));
        query.setResourceId(trimToNull(resourceId));
        query.setTraceId(trimToNull(traceId));
        query.setFrom(from);
        query.setTo(to);

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);
        PageResult<AuditLog> result = auditQueryService.queryAuditLogs(query, resolvedPage, resolvedSize);
        return PagedResponse.from(result, AuditLogResponse::from);
    }

    /**
     * Queries sensitive-data access audit logs.
     *
     * @param request current HTTP request used for authorization checks
     * @param tenantHeader tenant identifier supplied through {@code X-Tenant-Id}
     * @param userId optional user identifier filter
     * @param sensitiveType optional sensitive data classification filter
     * @param resourceType optional resource type filter
     * @param resourceId optional resource identifier filter
     * @param accessResult optional access result filter
     * @param from optional inclusive start timestamp
     * @param to optional inclusive end timestamp
     * @param page optional zero-based page index
     * @param size optional page size
     * @return paged sensitive access log response payload
     * @throws ResponseStatusException if access is denied or request parameters are invalid
     */
    @GetMapping("/sensitive-access-logs")
    public PagedResponse<SensitiveAccessLogResponse> querySensitiveAccessLogs(HttpServletRequest request,
                                                                               @RequestHeader(value = AuditHeaderNames.TENANT_ID, required = false) String tenantHeader,
                                                                               @RequestParam(value = "userId", required = false) Long userId,
                                                                               @RequestParam(value = "sensitiveType", required = false) String sensitiveType,
                                                                               @RequestParam(value = "resourceType", required = false) String resourceType,
                                                                               @RequestParam(value = "resourceId", required = false) String resourceId,
                                                                               @RequestParam(value = "accessResult", required = false) String accessResult,
                                                                               @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                               @RequestParam(value = "page", required = false) Integer page,
                                                                               @RequestParam(value = "size", required = false) Integer size) {
        accessGuard.assertAllowed(request);

        SensitiveDataAccessLogQuery query = new SensitiveDataAccessLogQuery();
        query.setTenantId(parseTenantId(tenantHeader));
        query.setUserId(userId);
        query.setSensitiveType(trimToNull(sensitiveType));
        query.setResourceType(trimToNull(resourceType));
        query.setResourceId(trimToNull(resourceId));
        query.setAccessResult(trimToNull(accessResult));
        query.setFrom(from);
        query.setTo(to);

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);
        PageResult<SensitiveDataAccessLog> result = auditQueryService.querySensitiveAccessLogs(query, resolvedPage, resolvedSize);
        return PagedResponse.from(result, SensitiveAccessLogResponse::from);
    }

    /**
     * Queries model invocation audit logs.
     *
     * @param request current HTTP request used for authorization checks
     * @param tenantHeader tenant identifier supplied through {@code X-Tenant-Id}
     * @param sessionId optional diagnosis session identifier filter
     * @param modelRegistryId optional model registry identifier filter
     * @param provider optional model provider filter
     * @param requestId optional upstream request identifier filter
     * @param status optional invocation status filter
     * @param from optional inclusive start timestamp
     * @param to optional inclusive end timestamp
     * @param page optional zero-based page index
     * @param size optional page size
     * @return paged model invocation log response payload
     * @throws ResponseStatusException if access is denied or request parameters are invalid
     */
    @GetMapping("/model-invocation-logs")
    public PagedResponse<ModelInvocationLogResponse> queryModelInvocationLogs(HttpServletRequest request,
                                                                               @RequestHeader(value = AuditHeaderNames.TENANT_ID, required = false) String tenantHeader,
                                                                               @RequestParam(value = "sessionId", required = false) Long sessionId,
                                                                               @RequestParam(value = "modelRegistryId", required = false) Long modelRegistryId,
                                                                               @RequestParam(value = "provider", required = false) String provider,
                                                                               @RequestParam(value = "requestId", required = false) String requestId,
                                                                               @RequestParam(value = "status", required = false) String status,
                                                                               @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                                               @RequestParam(value = "page", required = false) Integer page,
                                                                               @RequestParam(value = "size", required = false) Integer size) {
        accessGuard.assertAllowed(request);

        ModelInvocationLogQuery query = new ModelInvocationLogQuery();
        query.setTenantId(parseTenantId(tenantHeader));
        query.setSessionId(sessionId);
        query.setModelRegistryId(modelRegistryId);
        query.setProvider(trimToNull(provider));
        query.setRequestId(trimToNull(requestId));
        query.setStatus(trimToNull(status));
        query.setFrom(from);
        query.setTo(to);

        int resolvedPage = resolvePage(page);
        int resolvedSize = resolveSize(size);
        PageResult<ModelInvocationLog> result = auditQueryService.queryModelInvocationLogs(query, resolvedPage, resolvedSize);
        return PagedResponse.from(result, ModelInvocationLogResponse::from);
    }

    private Long parseTenantId(String tenantHeader) {
        if (tenantHeader == null || tenantHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id header is required");
        }
        try {
            long tenantId = Long.parseLong(tenantHeader.trim());
            if (tenantId <= 0) {
                throw new NumberFormatException("tenantId must be positive");
            }
            return tenantId;
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id header must be a positive long");
        }
    }

    private int resolvePage(Integer page) {
        int resolved = page == null ? 0 : page;
        if (resolved < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        return resolved;
    }

    private int resolveSize(Integer size) {
        int resolved = size == null ? auditProperties.getDefaultPageSize() : size;
        if (resolved <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be > 0");
        }
        if (resolved > auditProperties.getMaxPageSize()) {
            return auditProperties.getMaxPageSize();
        }
        return resolved;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
