package xenosoft.imldintelligence.module.audit.internal.service.command;

import lombok.Data;

@Data
public class SensitiveAccessRecordCommand {
    private Long tenantId;
    private Long userId;
    private String sensitiveType;
    private String resourceType;
    private String resourceId;
    private String accessReason;
    private String accessResult;
    private String ipAddress;
    private String traceId;
}
