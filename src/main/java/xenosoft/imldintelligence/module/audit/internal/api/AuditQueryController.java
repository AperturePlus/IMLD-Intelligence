package xenosoft.imldintelligence.module.audit.internal.api;

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
import xenosoft.imldintelligence.module.audit.internal.api.dto.AuditLogResponse;
import xenosoft.imldintelligence.module.audit.internal.api.dto.ModelInvocationLogResponse;
import xenosoft.imldintelligence.module.audit.internal.api.dto.PagedResponse;
import xenosoft.imldintelligence.module.audit.internal.api.dto.SensitiveAccessLogResponse;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.repository.query.AuditLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.ModelInvocationLogQuery;
import xenosoft.imldintelligence.module.audit.internal.repository.query.SensitiveDataAccessLogQuery;
import xenosoft.imldintelligence.module.audit.internal.service.AuditQueryService;
import xenosoft.imldintelligence.module.audit.internal.service.model.PageResult;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;
import xenosoft.imldintelligence.common.model.AuditLog;
import xenosoft.imldintelligence.common.model.ModelInvocationLog;
import xenosoft.imldintelligence.common.model.SensitiveDataAccessLog;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/audit")
@ConditionalOnProperty(prefix = "imld.audit", name = {"enabled", "query-api-enabled"}, havingValue = "true", matchIfMissing = true)
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

    @GetMapping("/logs")
    public PagedResponse<AuditLogResponse> queryAuditLogs(HttpServletRequest request,
                                                          @RequestHeader(value = AuditHeaderNames.TENANT_ID, required = false) String tenantHeader,
                                                          @RequestParam(value = "userId", required = false) Long userId,
                                                          @RequestParam(value = "action", required = false) String action,
                                                          @RequestParam(value = "resourceType", required = false) String resourceType,
                                                          @RequestParam(value = "resourceId", required = false) String resourceId,
                                                          @RequestParam(value = "traceId", required = false) String traceId,
                                                          @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                          @RequestParam(value = "page", required = false) Integer page,
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
