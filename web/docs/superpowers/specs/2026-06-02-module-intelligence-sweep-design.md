# 设计文档：模块智能点缀（次要模块铺开）

- 日期：2026-06-02
- 状态：待评审（Draft）
- 范围：`web/`
- 依赖：`2026-06-02-atomic-component-system-design.md`（组件）+ `2026-06-02-core-intelligence-features-design.md`（洞察引擎）。本 spec 不新建基础设施，只把已有组件与引擎能力铺到其余模块。

## 1. 背景与目标

两个头牌（驾驶舱、推理剧场）承载主线价值；本 spec 让**其余模块也"有 AI 味"**，使"全面智能化"在全 app 视觉与体验上统一。原则：**轻量点缀、复用优先、不重写业务逻辑**。

涉及模块：患者列表 / 电子病历查看 / 病历录入 / 专家报告 / 膳食干预 / 筛查数据。

### 隐私边界澄清（与 spec 2 不冲突）

- **助手（copilot）**：只看脱敏聚合快照（spec 2）。
- **本模块的"单患者 AI 摘要/高亮"**：是医生在**已打开的本患者病历**上下文中、**本地渲染**的二次表达，数据不出本机、不进助手。属于 `AGENTS.md` 的"PHI 优先本地处理"，与聚合边界是两件事。

为此新增**本地**记录分析器（区别于聚合快照）：

```
src/features/intelligence/recordInsights.ts
  summarizeRecord(record): RecordSummary   # 本地、单患者、不出域
```

`RecordSummary` 含：异常化验项列表（复用 `diagnosisEvidence.ts` 的 severity 判定）、关键标志（基因/病史/查体阳性）、一段本地生成的中文摘要、风险提示。供病历查看/录入/列表复用。

## 2. 各模块改造

### 2.1 患者列表 `PatientListPage.vue`
- **加**：顶部队列概览条（`KpiGrid`：在管/高危/今日新增/待诊）；列表行加 `RiskPill`（风险等级+分），支持"按风险排序"；保留现有 `aiStatus` 标签。
- **组件**：`KpiGrid`、`KpiStat`、`RiskPill`、`AiTag`。
- **数据**：队列概览来自引擎 `cohort.ts`；行风险来自 `worklist.ts` 风险分（或退化用 `aiStatus`）。
- **隐私**：列表为院内工作场景，展示风险标签属正常诊疗，不出域。

### 2.2 电子病历查看 `PatientRecordViewPage.vue`
- **加**："AI 病历摘要"卡（一段本地摘要 + 关键异常标签）置于顶部；化验区**异常值高亮**（超参考范围标红/橙）；可选"风险提示"行。
- **组件**：复用 spec 1 的卡片/标签/`EvidenceBar`；摘要文本经 `StreamingText` 轻流式（首次进入播放一次）。
- **数据**：`recordInsights.summarizeRecord(record)`（本地，基于当前已加载病历）。
- **隐私**：本地、单患者、不出域。

### 2.3 病历录入 `PatientRecordPage.vue`（轻量）
- **加**：录入化验值时**即时异常提示**（输入超参考范围给行内角标/颜色）；可选"完整度"小环（已填关键项占比）。
- **组件**：`BaseBadge`、`ProgressRing`、行内 token 化样式。
- **数据**：复用 `features/patient-record/constants/laboratoryScreening.ts` 的参考范围 + `recordInsights` 的异常判定；纯前端即时计算。
- **约束**：该页面已 1242 行且逻辑重（多草稿/校验）。**只做非侵入点缀**，不重构其表单与草稿逻辑，避免回归。

### 2.4 专家报告 `ExpertDiagnosisPage.vue`
- **加**：报告卡加 `AiTag` + `ConfidenceGauge`（AI 置信）；`aiFindings` 区复用 `EvidenceBar`/证据展示组件，视觉与推理剧场统一。
- **组件**：`AiTag`、`ConfidenceGauge`、`EvidenceBar`。
- **数据**：现有 `ExpertReport.aiFindings`（见 `api/types.ts`）。
- **约束**：不改签发/复核审计链路（`AGENTS.md` `submitDoctorFeedback`）。

### 2.5 膳食干预 `DietPage.vue`
- **加**："AI 膳食建议"生成区：按患者疾病给出建议（流式生成动效）+ 建议标签。
- **组件**：`StreamingText`、`BaseTag`、卡片。
- **数据**：复用现有疾病→膳食映射（`DiagnosisResult.diet` / `dietTags` 与 `mock/handlers/dietHandlers.ts` 的现有数据）；不引入新数据源。
- **隐私**：基于已选患者/疾病本地生成，不出域。

### 2.6 筛查数据 `DataScreeningPage.vue`
- **加**：数据质量洞察（完整度、异常项计数、缺失项）+ 图表（分布/趋势）；可挂一条 `InsightFeed` 风格的"数据质控提示"。
- **组件**：`KpiStat`、`RiskDonut`/`DiseaseSpectrum`/`ForecastChart`（按需）、`InsightItem`。
- **数据**：完整度/异常来自 `laboratoryScreening` 结构的本地统计；分布/趋势可用引擎聚合（含合成项，标注 origin）。

## 3. 实施顺序（spec 内分步）

按"改动小、收益高、风险低"排序：
1. `recordInsights.ts` + 单测（被多个模块依赖）。
2. 患者列表（概览条 + RiskPill 排序）。
3. 电子病历查看（AI 摘要卡 + 异常高亮）。
4. 专家报告（AiTag + 置信 + 证据）。
5. 膳食干预（AI 建议生成）。
6. 筛查数据（质控洞察 + 图表）。
7. 病历录入（即时异常提示，最后做、最克制）。

每步 `bun run typecheck` 保持绿，且对应页面手动走查无回归。

## 4. 测试

- `recordInsights.summarizeRecord` 等纯函数配 `*.test.ts`（`bun test`）：异常判定、完整度计算、摘要生成确定性。
- 各页面以 typecheck + 手动走查验收（项目无组件测试设施）。
- **隐私测试**：断言 `recordInsights` 为纯本地函数（输入为已加载 record、无网络副作用），且其结果不进入 spec 2 的 `aggregateSnapshot`。

## 5. 开放问题 / 风险

- **病历录入页体量大**：1242 行、含多草稿/校验。严格"只点缀不重构"，所有新增以独立子组件 + 最小 props 注入，避免触碰表单状态机。
- **异常高亮的参考范围来源**：以 `laboratoryScreening.ts` 既有参考范围为准；缺范围的项不高亮（不臆造）。
- **筛查图表数据**：若现有 mock 不足，用引擎合成并标 `origin:'synthesized'`，并在 UI 上以"演示数据"措辞避免误导。
- **铺开优先级可调**：若工期紧，2.3 病历录入 / 2.6 筛查数据 可作为最后批次或延后，不阻塞前四个。

## 6. 完成定义（DoD）

- 六个模块均出现一致的智能点缀（复用 spec 1 组件 + spec 2 引擎），视觉统一。
- `recordInsights` 单测 + 隐私测试通过；`bun run typecheck` 通过。
- 各页面原有业务逻辑无回归（尤其病历录入草稿/校验、专家报告签发链路）。
- 单患者本地分析与聚合快照边界清晰、互不串数据。
