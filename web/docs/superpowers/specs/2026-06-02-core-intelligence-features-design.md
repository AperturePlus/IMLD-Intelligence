# 设计文档：核心智能特性（洞察引擎 + 两个头牌 + 隐私助手 + 全局外壳）

- 日期：2026-06-02
- 状态：待评审（Draft）
- 范围：`web/`
- 依赖：`2026-06-02-atomic-component-system-design.md`（必须先完成，本 spec 消费其组件与契约类型）
- 被依赖：`2026-06-02-module-intelligence-sweep-design.md` 复用本 spec 的洞察引擎。

## 1. 背景与目标

让 web 端"显得功能上智能化"的主线价值，由四块组成：

1. **洞察引擎**：纯前端、mock 驱动，从现有数据推导/合成"智能"输出。
2. **驾驶舱**：重写首页（`WelcomePage.vue`）为数据驱动的智能总览。
3. **推理剧场**：增强 `AiDiagnosisPage.vue`，把"点一下→出报告"变成可见的分步推理。
4. **隐私优先助手 + 全局外壳**：顶栏引擎状态/隐私徽标 + 右下角助手坞，全局可用。

驱动方式：**纯前端演示**。允许确定性合成数据，但必须诚实标注"合成"与"派生"，并保留可替换为真实后端的接缝。

### 与项目安全基线的对齐（重要）

`AGENTS.md` 第一优先级为隐私/合规：`本地优先与最小出域`、`脱敏/去标识化`、`全链路可审计`、`deny by default`。本 spec 的助手据此设计：**只消费脱敏聚合快照，永不触达患者明细**——这不是附加项，而是产品定位的体现。

## 2. 洞察引擎 `src/features/intelligence/`

```
src/features/intelligence/
  types.ts                 # 引擎输出类型（对齐 spec 1 组件契约）
  dataSource.ts            # IntelligenceDataSource：包装现有 api 层（mock/后端的接缝）
  cohort.ts                # 队列聚合：风险分布/疾病谱/阳性率/KPI
  worklist.ts              # 风险打分 + 优先工作清单
  insights.ts              # 群体级洞察流（无明细）
  forecast.ts              # 趋势外推
  reasoningTrace.ts        # 诊断推理轨迹合成（喂给 ReasoningTimeline）
  aggregateSnapshot.ts     # 助手唯一可见的脱敏聚合快照
  *.test.ts                # 纯函数单测（bun test）
```

### 2.1 数据来源接缝 `IntelligenceDataSource`

引擎不直接调 axios，而是经 `dataSource.ts` 适配器读取：诊断队列、诊断会话/报告、患者概要。该适配器封装现有 api（如 `diagnosisApi.getAiQueue()`、会话/报告查询、患者列表）。实现细节（具体函数名）在实施时确定；引擎只依赖适配器接口。这样 mock / 真实后端切换不影响引擎与组件。

### 2.2 派生 vs 合成（诚实标注）

每个聚合输出带 `origin: 'derived' | 'synthesized'`：
- **derived**：来自真实（mock）数据。例：已出报告的疾病构成、队列待诊数、由病历字段算出的个体风险分。
- **synthesized**：现有数据不覆盖时确定性合成。例：1200+ 全队列风险分布、按周的历史趋势序列、未来预测。合成用稳定种子（如队列规模 + 当日日期派生的伪随机），保证演示期间数值稳定、可复现。

> 这层标注让"演示数据"与"真实派生"可区分，也为将来接真后端留出替换点（把 synthesized 项逐步替成 derived）。

### 2.3 关键算法

- **个体风险打分 `worklist.ts`**：对待诊患者，取其病历（经 dataSource）→ 复用 `features/diagnosis/services/diagnosisEvidence.ts` 统计异常证据数（warning/exception）+ 关键基因/标志 → 映射到 0–100 分，再过 `riskLevel.ts` 得 `低/中/高`。按分降序取 Top-N 作为驾驶舱"今日高危"，每条附**派生理由**（取该患者真实异常项，如"铁蛋白 1240↑↑、HFE C282Y 纯合"）。
- **推理轨迹 `reasoningTrace.ts`**：纯函数 `buildReasoningTrace(result: DiagnosisResult, record?): ReasoningTrace`：
  - `featureExtraction`：按来源统计特征数（生化/血常规凝血/影像/基因/病史查体）→ "40 项特征"。
  - `evidenceContributions`：从 `result.evidenceItems` + `keySigns` 派生贡献权重（severity 排序：exception>warning>info；基因/纯合给最高权重），归一化后降序 → 喂 `EvidenceBar`。
  - `differentials`：主导 = `result.diseaseName` @ `result.probability`；其余取 `result.differentials`，给下调概率 + 模板化排除理由（"铜蓝蛋白正常→排除 Wilson"）→ 喂 `DifferentialRace`。
  - `confidence`：取 `result.confidence` + `probability` → 喂 `ConfidenceGauge`。
  - 纯函数、确定性、可单测。
- **洞察流 `insights.ts`**：基于聚合量（科室/疾病谱/比率）从**模板库**生成群体级条目（带置信度），只含群体措辞、不含任何患者标识。确定性选取。
- **预测 `forecast.ts`**：对历史序列做简单线性/季节外推，输出 `forecast[]`（喂 `ForecastChart` 虚线段）。

## 3. 头牌一：智能驾驶舱（重写 `WelcomePage.vue`）

保持路由 `welcome`（`/center/welcome`）。用 spec 1 organisms 组合（数据来自引擎）：

- `WorklistBanner`（顶部）← `worklist.ts` Top-N 高危；"查看 AI 推理过程"深链至推理剧场（带 `patientId`）。
- `KpiGrid` ← `cohort.ts`（在管患者 / AI 高危 / 阳性率 / 自动报告数），数字滚动 + Sparkline。
- `RiskDonut` ← 风险分布（ECharts）。
- `DiseaseSpectrum` ← 疾病谱构成。
- `ForecastChart` ← 历史 + 预测。
- `InsightFeed` ← `insights.ts`。

页面只做"取引擎数据 + 摆放 organisms"，无样式硬编码（用 token）。删除原写死的 `stats`/`quickActions` 静态数组。

## 4. 头牌二：诊断推理剧场（增强 `AiDiagnosisPage.vue`）

保留左侧队列与 `已出报告` 逻辑不变。改造右侧主区状态机：

```
未选患者 → 已就绪(启动按钮) → [推理中: ReasoningTimeline 播放] → 报告(DiagnosisReportCard)
```

- `startDiagnosis()`：调用现有 `diagnosisApi.runAiDiagnosis(id)` 取 `DiagnosisResult` → `buildReasoningTrace(result, record)` → 渲染 `ReasoningTimeline`（自动播放：特征提取→证据加权→鉴别赛跑→置信收敛）→ 播放完 `emit` 展开 `DiagnosisReportCard`。
- 现有报告区块抽成 `DiagnosisReportCard`（spec 1）后**视觉 1:1**复用，不丢字段。
- `已出报告` 患者：直接载入历史 `DiagnosisResult` → 展示 `DiagnosisReportCard`，并提供"重播推理"（由 result 重建 trace）。
- 入口深链：驾驶舱 `WorklistBanner` 点击 → 路由带 `patientId` → 自动选中并可直接启动。
- 不改诊断 API 契约（`AGENTS.md` 的 `diagnoses` 接口与审计链路不动）；推理轨迹是**前端对既有 result 的二次表达**。

## 5. 隐私优先助手 + 全局外壳

### 5.1 全局外壳（改 `CenterLayout.vue`）

当前 `CenterLayout` = `<AppSidebar/> + <router-view/>`。新增**全局顶栏**与**助手坞**：

```
center-layout
├─ AppSidebar
└─ center-main
   ├─ AppTopbar        ← EngineStatusPill + PrivacyBadge + "问 IMLD 助手" 按钮
   ├─ router-view
   └─ AiCopilotDock    ← position:fixed 浮层，按钮切换显隐
```

- `AppTopbar`：新增轻组件（或 organism），全局一致。
- 迁移注意：引入顶栏后，页面内容区高度从 `100vh` 改为 `100%`/flex 适配（`AiDiagnosisPage` 现用 `calc(100vh-60px)`，需核对调整）。这是已知迁移项，成本低。
- `AiCopilotDock` 浮层 `position:fixed`，不影响布局流，可安全全局挂载。

### 5.2 助手数据边界（隐私核心）

```
src/features/intelligence/aggregateSnapshot.ts
  → buildAggregateSnapshot(): CohortAggregateSnapshot
```

`CohortAggregateSnapshot` **类型层面只含聚合量**（计数、分布、比率、疾病谱、趋势点），**不含任何患者级字段**（无姓名/病号/明细/可定位标识）。`AiCopilotDock` 只接收 `snapshot` + `responder`，结构上无法访问原始 store/明细。

### 5.3 应答层（可插拔）

```
src/features/intelligence/copilot/
  responder.ts        # interface CopilotResponder { respond(q, snapshot): AsyncIterable<string> }
  scriptedResponder.ts# 演示实现：意图匹配 → 基于 snapshot 的模板化回答（流式）
  intents.ts          # 建议 prompt + 关键词 → 意图映射
```

- 演示实现 `scriptedResponder`：把建议 prompt（"今日队列风险概览""解读 ALT 群体升高""生成本周筛查简报"）与自由输入关键词匹配到意图，用 `snapshot` 聚合量填充模板，经 `StreamingText` 流式输出。
- **每条回答附来源行**："数据来源：本机脱敏聚合统计 · 未访问任何患者明细"。
- 隐私横幅常驻助手头部。
- **可插拔**：将来接真 LLM/后端，只需替换 `responder` 实现，且只喂 `snapshot`（聚合），明细永不出本机——符合 `AGENTS.md` 最小出域。

## 6. 数据流总览

```
现有 api/mock ──IntelligenceDataSource──▶ 引擎(cohort/worklist/insights/forecast)
                                              │
        ┌─────────────────────────────────────┼───────────────────────────┐
        ▼                                       ▼                           ▼
   驾驶舱(organisms)                    aggregateSnapshot ──▶ AiCopilotDock(scriptedResponder)
                                              
  runAiDiagnosis(result) ──buildReasoningTrace──▶ ReasoningTimeline ──▶ DiagnosisReportCard
```

## 7. 测试

- 引擎纯函数全部 `*.test.ts`（`bun test`）：`buildReasoningTrace`、风险打分、聚合计算、`forecast`、意图匹配、`buildAggregateSnapshot`。
- **隐私回归测试（关键）**：断言 `CohortAggregateSnapshot` 不含患者级字段（类型 + 运行时形状校验）；断言 `scriptedResponder` 的输入仅为 snapshot。对齐 `AGENTS.md`"关键安全测试（泄露）"。
- `bun run typecheck` 通过；页面交互手动走查（启动诊断→看推理→出报告；驾驶舱深链；助手问答与来源行）。

## 8. 实施顺序（spec 内分步）

1. 引擎 `types.ts` + `dataSource.ts` 接缝 + `cohort.ts`/`worklist.ts`/`insights.ts`/`forecast.ts` + 单测。
2. `reasoningTrace.ts` + 单测。
3. 驾驶舱重写（`WelcomePage.vue` → organisms + 引擎）。
4. 推理剧场（`AiDiagnosisPage.vue` 增强 + `DiagnosisReportCard` 接入）。
5. 全局外壳（`CenterLayout` + `AppTopbar` + 高度迁移）。
6. 助手（`aggregateSnapshot` + `copilot/*` + `AiCopilotDock` 挂载）+ 隐私测试。

每步可独立演示、`bun run typecheck` 保持绿。

## 9. 开放问题 / 风险

- **全局顶栏的高度迁移**：可能影响未重写页面（spec 3 范围）的 `100vh` 布局。缓解：顶栏引入时统一把内容区改 `flex:1; min-height:0`，逐页核对。**默认：引入全局顶栏。**
- **合成数据的稳定性**：演示期数值需稳定（避免每次刷新跳变）。用稳定种子，不用 `Math.random()` 直出（或仅用于无关紧要的微动效）。
- **个体风险打分需要病历**：若 dataSource 在某模式下拿不到完整病历，worklist 退化为基于队列 + 合成分（标 synthesized），不报错。
- **推理轨迹与报告一致**：trace 的主导诊断/概率必须与 `DiagnosisReportCard` 完全一致，避免"推理说 A、报告写 B"。

## 10. 完成定义（DoD）

- 驾驶舱、推理剧场、全局外壳、隐私助手均可演示。
- 引擎单测 + 隐私回归测试通过；`bun run typecheck` 通过。
- 诊断 API 契约与审计链路未改动。
- 助手结构上只能访问脱敏聚合快照。
