package xenosoft.imldintelligence.modules.clinical.internal.model;

import lombok.Data;

@Data
public class IndicatorDict {
    private String code;            // 系统统一指标编码
    private String indicatorName;
    private String category;        // 肝功/代谢/遗传等
    private String dataType;        // NUMERIC/TEXT/ENUM
    private String defaultUnit;
    //Logical Observation Identifiers Names and Codes，逻辑观察标识符名称和代码，是一个国际标准，用于标识临床实验室测试和测量结果的类型和含义
    private String loincCode;
    private java.math.BigDecimal normalLow;
    private java.math.BigDecimal normalHigh;
    private String status;
    private java.time.OffsetDateTime createdAt;
}

/**
 * Schema:
 * CREATE TABLE indicator_dict (
 *     code                    VARCHAR(64) PRIMARY KEY,          -- 系统统一指标编码
 *     indicator_name          VARCHAR(200) NOT NULL,
 *     category                VARCHAR(64) NOT NULL,             -- 肝功/代谢/遗传等
 *     data_type               VARCHAR(16) NOT NULL,             -- NUMERIC/TEXT/ENUM
 *     default_unit            VARCHAR(32),
 *     loinc_code              VARCHAR(64),
 *     normal_low              NUMERIC(18,6),
 *     normal_high             NUMERIC(18,6),
 *     status                  VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
 *     created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
 * );
 */