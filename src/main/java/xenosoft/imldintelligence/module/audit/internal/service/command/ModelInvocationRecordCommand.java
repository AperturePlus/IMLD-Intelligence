package xenosoft.imldintelligence.module.audit.internal.service.command;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModelInvocationRecordCommand {
    private Long tenantId;
    private Long sessionId;
    private Long modelRegistryId;
    private String provider;
    private String requestId;
    private String inputDigest;
    private String outputDigest;
    private Integer latencyMs;
    private Integer tokenIn;
    private Integer tokenOut;
    private BigDecimal costAmount;
    private String status;
    private String errorMessage;
    private String traceId;
}
