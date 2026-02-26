package xenosoft.imldintelligence.module.audit.internal.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContext;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContextHolder;
import xenosoft.imldintelligence.module.audit.internal.repository.AuditLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.ModelInvocationLogRepository;
import xenosoft.imldintelligence.module.audit.internal.repository.SensitiveDataAccessLogRepository;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.command.ModelInvocationRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.command.SensitiveAccessRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.exception.AuditPersistenceException;
import xenosoft.imldintelligence.shared.model.AuditLog;
import xenosoft.imldintelligence.shared.model.ModelInvocationLog;
import xenosoft.imldintelligence.shared.model.SensitiveDataAccessLog;

import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "imld.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditTrailServiceImpl implements AuditTrailService {
    private static final Logger LOG = LoggerFactory.getLogger(AuditTrailServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository;
    private final ModelInvocationLogRepository modelInvocationLogRepository;
    private final AuditPayloadSanitizer sanitizer;
    private final AuditProperties auditProperties;

    public AuditTrailServiceImpl(AuditLogRepository auditLogRepository,
                                 SensitiveDataAccessLogRepository sensitiveDataAccessLogRepository,
                                 ModelInvocationLogRepository modelInvocationLogRepository,
                                 AuditPayloadSanitizer sanitizer,
                                 AuditProperties auditProperties) {
        this.auditLogRepository = auditLogRepository;
        this.sensitiveDataAccessLogRepository = sensitiveDataAccessLogRepository;
        this.modelInvocationLogRepository = modelInvocationLogRepository;
        this.sanitizer = sanitizer;
        this.auditProperties = auditProperties;
    }

    @Override
    @Transactional
    public AuditLog recordAudit(AuditRecordCommand command) {
        requireNonNull(command, "command is required");
        ResolvedContext context = resolveContext(
                command.getTenantId(),
                command.getUserId(),
                command.getUserRole(),
                command.getTraceId(),
                command.getIpAddress(),
                command.getDeviceInfo(),
                command.getAppVersion()
        );

        String action = requireText(command.getAction(), "action");
        String resourceType = requireText(command.getResourceType(), "resourceType");

        AuditLog log = new AuditLog();
        log.setTenantId(context.tenantId());
        log.setUserId(context.userId());
        log.setUserRole(context.userRole());
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(trimToNull(command.getResourceId()));
        log.setBeforeData(sanitizer.sanitize(command.getBeforeData()));
        log.setAfterData(sanitizer.sanitize(command.getAfterData()));
        log.setIpAddress(context.ipAddress());
        log.setDeviceInfo(context.deviceInfo());
        log.setAppVersion(context.appVersion());
        log.setTraceId(context.traceId());

        try {
            return auditLogRepository.save(log);
        } catch (RuntimeException ex) {
            throwPersistenceFailure("audit_log", ex);
            return log;
        }
    }

    @Override
    @Transactional
    public SensitiveDataAccessLog recordSensitiveAccess(SensitiveAccessRecordCommand command) {
        requireNonNull(command, "command is required");
        ResolvedContext context = resolveContext(
                command.getTenantId(),
                command.getUserId(),
                null,
                command.getTraceId(),
                command.getIpAddress(),
                null,
                null
        );

        SensitiveDataAccessLog log = new SensitiveDataAccessLog();
        log.setTenantId(context.tenantId());
        log.setUserId(context.userId());
        log.setSensitiveType(requireText(command.getSensitiveType(), "sensitiveType"));
        log.setResourceType(requireText(command.getResourceType(), "resourceType"));
        log.setResourceId(trimToNull(command.getResourceId()));
        log.setAccessReason(trimToNull(command.getAccessReason()));
        log.setAccessResult(requireText(command.getAccessResult(), "accessResult"));
        log.setIpAddress(context.ipAddress());

        try {
            return sensitiveDataAccessLogRepository.save(log);
        } catch (RuntimeException ex) {
            throwPersistenceFailure("sensitive_data_access_log", ex);
            return log;
        }
    }

    @Override
    @Transactional
    public ModelInvocationLog recordModelInvocation(ModelInvocationRecordCommand command) {
        requireNonNull(command, "command is required");
        ResolvedContext context = resolveContext(
                command.getTenantId(),
                null,
                null,
                command.getTraceId(),
                null,
                null,
                null
        );

        ModelInvocationLog log = new ModelInvocationLog();
        log.setTenantId(context.tenantId());
        log.setSessionId(command.getSessionId());
        log.setModelRegistryId(command.getModelRegistryId());
        log.setProvider(trimToNull(command.getProvider()));
        log.setRequestId(trimToNull(command.getRequestId()));
        log.setInputDigest(trimToNull(command.getInputDigest()));
        log.setOutputDigest(trimToNull(command.getOutputDigest()));
        log.setLatencyMs(command.getLatencyMs());
        log.setTokenIn(command.getTokenIn());
        log.setTokenOut(command.getTokenOut());
        log.setCostAmount(command.getCostAmount());
        log.setStatus(requireText(command.getStatus(), "status"));
        log.setErrorMessage(trimToNull(command.getErrorMessage()));

        try {
            return modelInvocationLogRepository.save(log);
        } catch (RuntimeException ex) {
            throwPersistenceFailure("model_invocation_log", ex);
            return log;
        }
    }

    private void throwPersistenceFailure(String logType, RuntimeException ex) {
        if (auditProperties.isFailClosed()) {
            throw new AuditPersistenceException("Audit persistence failed for " + logType, ex);
        }
        LOG.error("Audit persistence failed for {}", logType, ex);
    }

    private ResolvedContext resolveContext(Long commandTenantId,
                                           Long commandUserId,
                                           String commandUserRole,
                                           String commandTraceId,
                                           String commandIpAddress,
                                           String commandDeviceInfo,
                                           String commandAppVersion) {
        AuditContext requestContext = AuditContextHolder.get();

        Long tenantId = firstNonNull(commandTenantId, requestContext == null ? null : requestContext.getTenantId());
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }

        Long userId = firstNonNull(commandUserId, requestContext == null ? null : requestContext.getUserId());
        String userRole = firstNonBlank(commandUserRole, requestContext == null ? null : requestContext.getUserRole());
        String traceId = firstNonBlank(commandTraceId, requestContext == null ? null : requestContext.getTraceId());
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        String ipAddress = firstNonBlank(commandIpAddress, requestContext == null ? null : requestContext.getIpAddress());
        String deviceInfo = firstNonBlank(commandDeviceInfo, requestContext == null ? null : requestContext.getDeviceInfo());
        String appVersion = firstNonBlank(commandAppVersion, requestContext == null ? null : requestContext.getAppVersion());

        return new ResolvedContext(tenantId, userId, userRole, traceId, ipAddress, deviceInfo, appVersion);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private record ResolvedContext(Long tenantId,
                                   Long userId,
                                   String userRole,
                                   String traceId,
                                   String ipAddress,
                                   String deviceInfo,
                                   String appVersion) {
    }
}
