package xenosoft.imldintelligence.module.audit.internal.context;

import lombok.Data;

@Data
public class AuditContext {
    private Long tenantId;
    private Long userId;
    private String userRole;
    private String traceId;
    private String ipAddress;
    private String deviceInfo;
    private String appVersion;
}
