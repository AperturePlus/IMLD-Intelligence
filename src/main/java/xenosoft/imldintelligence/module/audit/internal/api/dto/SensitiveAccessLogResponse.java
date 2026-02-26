package xenosoft.imldintelligence.module.audit.internal.api.dto;

import lombok.Data;
import xenosoft.imldintelligence.shared.model.SensitiveDataAccessLog;

import java.time.OffsetDateTime;

@Data
public class SensitiveAccessLogResponse {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String sensitiveType;
    private String resourceType;
    private String resourceId;
    private String accessReason;
    private String accessResult;
    private String ipAddress;
    private OffsetDateTime createdAt;

    public static SensitiveAccessLogResponse from(SensitiveDataAccessLog log) {
        SensitiveAccessLogResponse response = new SensitiveAccessLogResponse();
        response.setId(log.getId());
        response.setTenantId(log.getTenantId());
        response.setUserId(log.getUserId());
        response.setSensitiveType(log.getSensitiveType());
        response.setResourceType(log.getResourceType());
        response.setResourceId(log.getResourceId());
        response.setAccessReason(log.getAccessReason());
        response.setAccessResult(log.getAccessResult());
        response.setIpAddress(log.getIpAddress());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
