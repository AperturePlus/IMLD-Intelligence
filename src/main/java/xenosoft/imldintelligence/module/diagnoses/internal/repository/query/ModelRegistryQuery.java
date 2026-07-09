package xenosoft.imldintelligence.module.diagnoses.internal.repository.query;

import lombok.Data;

/**
 * ModelRegistry 查询对象，封装模型注册表列表的筛选条件。
 *
 * <p>用于把过滤/排序/分页下沉到 SQL，避免全量加载后内存分页。</p>
 */
@Data
public class ModelRegistryQuery {
    private Long tenantId;
    private String provider;
    private String modelType;
    private String status;
}
