# 设计文档：组件原子化重构（Atomic Design 组件体系）

- 日期：2026-06-02
- 状态：待评审（Draft）
- 范围：`web/`（Vue 3 + TS + Vite 前端）
- 关联 spec：本文件是**地基**，被 `2026-06-02-core-intelligence-features-design.md` 与 `2026-06-02-module-intelligence-sweep-design.md` 依赖。

## 1. 背景与目标

目标是让 web 端"全面智能化"。要让分散在各模块的智能效果**视觉一致、可复用**，必须先有一套共享的组件词汇表，而不是每个页面各写一套内联样式。

当前组件层很薄：
- `src/components/PatientAvatar.vue`（唯一的通用组件）
- `src/layouts/components/AppSidebar.vue`、`DesktopTitlebar.vue`
- 功能特定组件散落在 `src/features/<domain>/components/`（如 `auth/components/Liver3DModel.vue`、`FloatingParticles.vue`）
- 页面（`src/pages/*`）内含大量内联 `<style scoped>` 与硬编码颜色（如 `#0f6d8d`、`#f56c6c` 反复出现）。

本 spec 采用 **Atomic Design** 重构组件层，并抽出**设计 token**，统一这套视觉语言。

### 本 spec 只做"词汇表"，不做"句子"

- ✅ 设计 token、原子分层目录、所有**纯展示型**组件（props 进、事件出，无数据获取、无引擎调用、无 API）、迁移现有组件、约定与桶文件导出。
- ❌ 不重写任何页面、不建洞察引擎、不写助手应答逻辑、不接 mock 数据。这些在 spec 2 / spec 3。

判定标准：本 spec 产出的每个组件都应能**仅靠传入 props 在隔离环境中渲染**。凡是"需要知道患者数据从哪来 / 需要算什么"的逻辑都不属于这里。

## 2. 设计 token

新增 `src/styles/tokens.css`，在 `src/main.ts` 引入（紧邻现有 `@/style.css` 全局样式；亦可直接并入 `@/style.css`）。以 CSS 自定义属性承载，组件一律引用 `var(--imld-*)`，禁止再硬编码颜色/圆角/阴影。

```css
:root{
  /* 品牌 */
  --imld-navy:#001529;        --imld-navy-deep:#000c17;
  --imld-teal-1:#0f6d8d;      --imld-teal-2:#22a39f;
  --imld-brand-grad:linear-gradient(135deg,var(--imld-teal-1),var(--imld-teal-2));
  /* 中性 */
  --imld-bg:#eef3f8;          --imld-card:#ffffff;     --imld-border:#e6edf4;
  --imld-text:#1c2d3f;        --imld-muted:#6a7d90;
  /* 语义 */
  --imld-primary:#409eff;     --imld-success:#67c23a;
  --imld-warning:#e6a23c;     --imld-danger:#f56c6c;   --imld-info:#909399;
  /* 风险（对齐 riskLevel.ts 的 低/中/高） */
  --imld-risk-low:#67c23a;    --imld-risk-mid:#e6a23c; --imld-risk-high:#f56c6c;
  /* 圆角 */
  --imld-radius-sm:8px; --imld-radius-md:12px; --imld-radius-lg:15px; --imld-radius-pill:20px;
  /* 阴影 */
  --imld-shadow-card:0 8px 20px rgba(23,48,66,.05);
  --imld-shadow-float:0 22px 50px rgba(13,40,60,.22);
  /* 间距（4 基准） */
  --imld-sp-1:4px; --imld-sp-2:8px; --imld-sp-3:12px; --imld-sp-4:16px; --imld-sp-5:20px; --imld-sp-6:24px;
  /* 动效 */
  --imld-ease-out:cubic-bezier(.2,.7,.2,1);
  --imld-dur-rise:.5s; --imld-dur-count:900ms;
}
```

共享 keyframes（`rise`、`ping`、`spin`、`blink`）也放入 `tokens.css`，供各组件复用，避免重复定义。

> 说明：仓库已装 Tailwind v4（`@tailwindcss/vite`）。本 spec 选择**纯 CSS 变量**而非 Tailwind `@theme`，因为现有组件全部使用 `<style scoped>` + Element Plus，CSS 变量侵入最小、与现状一致。Tailwind 仍可用，但不作为 token 载体。

## 3. 目录结构与分层

```
src/components/
  atoms/        # 不可再分的原语；无业务语义
  molecules/    # 少量原子组合 + 轻量逻辑
  organisms/    # 复杂、自包含的区块（纯展示，数据由父级注入）
  index.ts      # 桶文件，统一导出
src/layouts/    # = templates（沿用现名，CenterLayout 等）
src/pages/      # = pages（位置不变；由 spec 2/3 改写）
```

功能特定、不可复用的组件（`Liver3DModel`、`FloatingParticles`）**保留在** `src/features/<domain>/components/`，不强行上移。

### 命名与编码约定（与现有代码一致）

- 单文件组件 `PascalCase.vue`，`<script setup lang="ts">`。
- props 用 `withDefaults(defineProps<...>())`；透传场景用 `defineOptions({ inheritAttrs:false })`（参考现有 `PatientAvatar.vue`）。
- 纯展示组件**只接受 props、只 `emit` 事件**，不引入 api/mock/engine。
- 每个组件目录可含 `Foo.vue` + 可选 `Foo.types.ts`（导出该组件的 prop 契约类型，供 spec 2/3 的引擎产出对齐）。
- 桶文件 `src/components/index.ts` 重导出常用组件，页面侧 `import { KpiStat } from '@/components'`。

## 4. 组件清单（本 spec 全部交付）

下列组件均为 prop 驱动的展示件。"契约类型"列指该组件对外暴露、需被 spec 2/3 引擎对齐的 TS 接口（定义在组件旁的 `*.types.ts`）。

### atoms/

| 组件 | 职责 | 关键 props |
|---|---|---|
| `BaseButton` | 主/次/幽灵按钮，渐变主色 | `variant: primary\|ghost\|text`、`size` |
| `BaseTag` | 通用标签 | `type`、`effect`、`size` |
| `BaseIcon` | 统一 SVG 线性图标（house/users/cpu/activity/settings/lock/check/spark/chevron/refresh…） | `name`、`size` |
| `StatusDot` | 呼吸状态点（运行中绿点 + ping 光环） | `color`、`pulse:boolean` |
| `Sparkline` | 迷你趋势线（内联 SVG polyline） | `points:number[]`、`color`、`width/height` |
| `ProgressRing` | 圆环进度（SVG，stroke-dashoffset 动画） | `percentage`、`color`、`size`、`stroke` |
| `BaseAvatar` | 纯头像（图/字母回退） | `src`、`text`、`size`、`bg` |
| `BaseBadge` | 角标/计数 | `value`、`type` |
| `StreamingText` | 打字机流式文本（逐字 `textContent`，禁用 innerHTML） | `text`、`speed`、`autoplay` |

> 安全：`StreamingText` 与一切动态文本组件**严禁使用 `innerHTML`**（项目 PreToolUse 安全钩子会拦截，且符合 AGENTS.md 安全基线）。逐字渲染用 `textContent`；需要强调样式时拆成多个已存在的元素切换 class。

### molecules/

| 组件 | 职责 | 契约类型 |
|---|---|---|
| `PatientAvatar` | 现有组件迁入（字母/中文回退 + 调色板） | — |
| `RiskPill` | 风险标签：接 `riskLevel.ts` 的 `低/中/高` → 颜色 + 文案 + 可选概率 | `RiskPillProps{ level?, probability? }` |
| `AiTag` | 小号"AI/置信度"标记片 | — |
| `KpiStat` | 指标卡：标签 + 数字滚动 + delta + Sparkline | `KpiStatProps{ label, value, delta?, trend?, format? }` |
| `EngineStatusPill` | 顶栏"AI 引擎运行中 · 模型 vX"胶囊（含 StatusDot） | `EngineStatusProps{ modelName, version, running }` |
| `PrivacyBadge` | "数据不出域 · 院内离线"徽标（含 lock 图标） | `text?` |
| `ConfidenceGauge` | 置信/概率仪表（ProgressRing + 数字 + 风险色） | `ConfidenceGaugeProps{ percentage, level, label }` |
| `EvidenceBar` | 证据贡献条：标签 + 贡献值进度条 + 权重 | `EvidenceBarProps{ label, value?, weight, weak? }` |
| `InsightItem` | 洞察流单条：时间 + 节点 + 标题 + 置信度 | `InsightItemProps{ time, title, confidence?, severity? }` |
| `ChatMessage` | 助手气泡（user/ai + 可选来源行 + 流式占位） | `ChatMessageProps{ role, text, source?, streaming? }` |
| `ChatComposer` | 输入条 + 建议 chips（受控） | `ChatComposerProps{ placeholder, suggestions }`，`emit send/pick` |
| `NavItem` | 侧边栏菜单项（图标 + 文案 + 激活态） | — |

### organisms/

均为纯展示，数据通过 props 注入；交互只 `emit`。

| 组件 | 职责 | 契约类型 |
|---|---|---|
| `AppSidebar` | 现有侧边栏迁入并按原子件重构（用 `NavItem`/`BaseAvatar`/`BaseIcon`），结构不变 | — |
| `DesktopTitlebar` | 现有标题栏迁入 | — |
| `KpiGrid` | KPI 卡栅格容器 | `items: KpiStatProps[]` |
| `RiskDonut` | 队列风险分布甜甜圈 + 图例（ECharts 或 conic-gradient，见 §5） | `RiskDonutProps{ high, mid, low, total }` |
| `DiseaseSpectrum` | 疾病谱水平条 | `DiseaseSpectrumProps{ items:{name,pct}[] }` |
| `ForecastChart` | 趋势 + AI 预测折线（ECharts，实线史/虚线测） | `ForecastChartProps{ history:[], forecast:[] }` |
| `InsightFeed` | 洞察流列表（组合 `InsightItem`） | `items: InsightItemProps[]` |
| `WorklistBanner` | 驾驶舱顶部"AI 优先高危"横幅 + 病例 chips | `WorklistBannerProps{ greeting, highRiskCount, cases:[] }`，`emit openCase` |
| `ReasoningTimeline` | 推理剧场时间线（步骤 + `EvidenceBar` + `DifferentialRace` + `ConfidenceGauge`，含播放/重播） | `ReasoningTimelineProps{ trace: ReasoningTrace }`，`emit replay/expandReport` |
| `DifferentialRace` | 鉴别诊断候选概率赛跑 | `DifferentialRaceProps{ candidates:{name,prob,win?,why?}[] }` |
| `DiagnosisReportCard` | 把现有 `AiDiagnosisPage` 报告区块抽成可复用展示件 | `DiagnosisReportProps{ result: DiagnosisResult }` |
| `AiCopilotDock` | 隐私优先助手**外壳**：头部 + 隐私横幅 + 消息区 + ChatComposer | `AiCopilotDockProps{ messages, privacyNote, suggestions }`，`emit send` |

> `ReasoningTrace`、`InsightItemProps`、`KpiStatProps` 等契约类型在本 spec 定义骨架（字段名 + 类型），其**数据生产**在 spec 2 的洞察引擎。`DiagnosisReportCard` 复用 `src/api/types.ts` 既有的 `DiagnosisResult`。

## 5. 图表实现取舍

- 仓库已装 `echarts ^5.6.0`。`RiskDonut` / `ForecastChart` / 任何坐标轴类图表 → **用 ECharts**（封装为薄 wrapper 组件，`resize` 监听 + `dispose` 清理）。
- 极简装饰性图形（KPI 的 `Sparkline`、`ProgressRing`、`ConfidenceGauge`）→ **内联 SVG/CSS**，避免为小图标拉满 ECharts 实例。
- `RiskDonut` 给出 ECharts 实现；保留 conic-gradient 作为 mockup 参考但生产用 ECharts（可交互、图例、tooltip）。

## 6. 迁移与影响

- `PatientAvatar.vue`：`components/` → `components/molecules/`。更新现有引用（`AiDiagnosisPage` 等）。保留为薄包装：`molecules/PatientAvatar` 内部用 `atoms/BaseAvatar`。
- `AppSidebar.vue`、`DesktopTitlebar.vue`：`layouts/components/` → `components/organisms/`（或保留 `layouts/components` 但用原子件重写——见开放问题 §9）。行为/结构不变，仅重构内部实现与样式 token 化。
- 现有页面**暂不改**（spec 2/3 负责），但其引用路径若因迁移变动需同步更新 import。
- 旧的硬编码颜色逐步替换为 token：本 spec 只要求**新组件**全部 token 化；页面内联样式的 token 化随 spec 2/3 改写时顺带完成。

## 7. 测试

- 纯函数（如 `RiskPill` 的 level→color 映射、`Sparkline` 的点位归一化、`StreamingText` 的分片逻辑）抽成可测函数，配 `*.test.ts`，用 `bun test`（仓库现有测试方式）。
- `.vue` 组件本身不引入新测试框架（项目当前无组件测试设施）；以 `bun run typecheck`（`vue-tsc`）+ 可选 `/__ui` 手动走查为验收。
- 验收命令：`bun run typecheck` 通过；新增 `*.test.ts` 全绿。

## 8. 实施顺序（spec 内分步）

1. `tokens.css` + 入口引入 + 共享 keyframes。
2. atoms（含 `BaseIcon` 图标集、`Sparkline`、`ProgressRing`、`StreamingText`）+ 单测。
3. molecules（`RiskPill` 接 `riskLevel.ts`、`KpiStat`、`ConfidenceGauge`、`EvidenceBar`、`InsightItem`、`ChatMessage`/`ChatComposer`、`PatientAvatar` 迁移）。
4. organisms（charts wrapper、`InsightFeed`、`WorklistBanner`、`ReasoningTimeline`+`DifferentialRace`、`DiagnosisReportCard`、`AiCopilotDock` 外壳；`AppSidebar`/`DesktopTitlebar` 迁移）。
5. 桶文件 `components/index.ts` + 契约类型整理 + typecheck 收口。

每步 `bun run typecheck` 保持绿。

## 9. 开放问题 / 风险

- **侧边栏落位**：`AppSidebar`/`DesktopTitlebar` 迁到 `components/organisms/` 还是留 `layouts/components/`？倾向迁入 organisms 保持统一；若担心 router/layout 耦合，可保留原位但内部 token 化。**默认：迁入 organisms。**
- **可选 `/__ui` 组件走查页**：便于隔离验收原子件，但会进打包。**默认：作为 dev-only 路由或干脆不做，标记为 nice-to-have，不阻塞。**
- ECharts 体积：按需引入（`echarts/core` + 必要 charts/components），避免全量。
- `DiagnosisReportCard` 抽取须与现有 `AiDiagnosisPage` 报告区视觉 1:1，避免回归。

## 10. 完成定义（DoD）

- `tokens.css` 生效，新组件零硬编码颜色。
- §4 全部组件就位、prop 驱动、`bun run typecheck` 通过、相关单测通过。
- 现有功能无回归（`PatientAvatar`、侧边栏、标题栏行为不变）。
- `components/index.ts` 可被 spec 2/3 直接消费。
