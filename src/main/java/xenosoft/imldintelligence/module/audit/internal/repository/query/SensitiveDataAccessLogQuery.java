package xenosoft.imldintelligence.module.audit.internal.repository.query;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SensitiveDataAccessLogQuery {
    private Long tenantId;
    private Long userId;
    private String sensitiveType;
    private String resourceType;
    private String resourceId;
    private String accessResult;
    private OffsetDateTime from;
    private OffsetDateTime to;
}
