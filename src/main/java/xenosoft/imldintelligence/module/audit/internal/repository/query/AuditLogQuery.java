package xenosoft.imldintelligence.module.audit.internal.repository.query;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AuditLogQuery {
    private Long tenantId;
    private Long userId;
    private String action;
    private String resourceType;
    private String resourceId;
    private String traceId;
    private OffsetDateTime from;
    private OffsetDateTime to;
}
