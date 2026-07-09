package xenosoft.imldintelligence.module.diagnoses.internal.repository.query;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * DiagnosisSession 查询对象，封装诊断会话列表的筛选条件。
 *
 * <p>用于把过滤/排序/分页下沉到 SQL，避免全量加载后内存分页。</p>
 */
@Data
public class DiagnosisSessionQuery {
    private Long tenantId;
    private Long patientId;
    private Long encounterId;
    private Long doctorId;
    private String triggeredBy;
    private String status;
    private OffsetDateTime startedFrom;
    private OffsetDateTime startedTo;
}
