package xenosoft.imldintelligence.module.audit.internal.repository.query;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ModelInvocationLogQuery {
    private Long tenantId;
    private Long sessionId;
    private Long modelRegistryId;
    private String provider;
    private String requestId;
    private String status;
    private OffsetDateTime from;
    private OffsetDateTime to;
}
