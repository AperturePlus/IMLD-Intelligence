package xenosoft.imldintelligence.module.audit.internal.api.dto;

import lombok.Data;
import xenosoft.imldintelligence.common.model.AuditLog;

import java.time.OffsetDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String userRole;
    private String action;
    private String resourceType;
    private String resourceId;
    private com.fasterxml.jackson.databind.JsonNode beforeData;
    private com.fasterxml.jackson.databind.JsonNode afterData;
    private String ipAddress;
    private String deviceInfo;
    private String appVersion;
    private String traceId;
    private OffsetDateTime createdAt;

    public static AuditLogResponse from(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(log.getId());
        response.setTenantId(log.getTenantId());
        response.setUserId(log.getUserId());
        response.setUserRole(log.getUserRole());
        response.setAction(log.getAction());
        response.setResourceType(log.getResourceType());
        response.setResourceId(log.getResourceId());
        response.setBeforeData(log.getBeforeData());
        response.setAfterData(log.getAfterData());
        response.setIpAddress(log.getIpAddress());
        response.setDeviceInfo(log.getDeviceInfo());
        response.setAppVersion(log.getAppVersion());
        response.setTraceId(log.getTraceId());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
