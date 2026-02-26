package xenosoft.imldintelligence.module.audit.internal.api.dto;

import lombok.Data;
import xenosoft.imldintelligence.shared.model.ModelInvocationLog;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ModelInvocationLogResponse {
    private Long id;
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
    private OffsetDateTime createdAt;

    public static ModelInvocationLogResponse from(ModelInvocationLog log) {
        ModelInvocationLogResponse response = new ModelInvocationLogResponse();
        response.setId(log.getId());
        response.setTenantId(log.getTenantId());
        response.setSessionId(log.getSessionId());
        response.setModelRegistryId(log.getModelRegistryId());
        response.setProvider(log.getProvider());
        response.setRequestId(log.getRequestId());
        response.setInputDigest(log.getInputDigest());
        response.setOutputDigest(log.getOutputDigest());
        response.setLatencyMs(log.getLatencyMs());
        response.setTokenIn(log.getTokenIn());
        response.setTokenOut(log.getTokenOut());
        response.setCostAmount(log.getCostAmount());
        response.setStatus(log.getStatus());
        response.setErrorMessage(log.getErrorMessage());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
