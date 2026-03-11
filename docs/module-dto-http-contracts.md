# 模块 DTO 与 HTTP 契约设计

## 目标

本轮变更采用契约优先方式，为 11 个业务模块统一补齐 DTO 与 HTTP 接口边界，但不落具体控制器实现。
这样做的目的有三点：

- 让前后端和实现层先围绕稳定契约协作，而不是围绕仓储模型直接耦合。
- 保持 SaaS、私有化、混合通信三种部署方式下的接口语义一致。
- 把 PHI、授权、出域、签名许可证等高风险对象在边界层就分类收口。

## DTO 分类

除 `audit` 为兼容既有接口仅新增查询 DTO 外，其余模块统一采用四类 DTO：

- `Query`：仅承载筛选条件，不混入业务写入语义。
- `Request`：仅承载写操作入参，明文字段在后续实现中必须完成加密、脱敏、审计。
- `Response`：仅承载对外返回结构，不直接暴露仓储模型或加密后原文。
- `Shared`：模块内部复用的嵌套片段，避免在多个响应之间复制结构。

公共约定：

- 租户域接口默认要求 `X-Tenant-Id` 请求头。
- 新设计接口默认返回 `ApiResponse<T>`；分页列表返回 `ApiResponse<PagedResultResponse<T>>`。
- `audit` 模块为保持向后兼容，继续沿用既有 `PagedResponse<T>` 语义。
- `license` 模块是部署级能力，不绑定租户头。

## 模块总览

### `audit`

- 基路径：`/api/v1/audit`
- 契约接口：`AuditQueryApi`
- 设计重点：保持既有查询 API 不破坏，只补充 `AuditQueryDtos.Query` 做参数分类。

### `identity`

- 基路径：`/api/v1/identity`
- 契约接口：`IdentityApi`
- 主要资源：登录态、患者档案、外部标识、知情同意、用户角色、权限判定。
- 设计重点：患者和监护人响应只暴露脱敏字段，不返回加密后原文。

### `clinical`

- 基路径：`/api/v1/clinical`
- 契约接口：`ClinicalApi`
- 主要资源：检验结果、基因报告、影像报告、病史条目、指标映射。
- 设计重点：原始证据允许写入，但对外只通过结构化响应暴露必要内容。

### `diagnoses`

- 基路径：`/api/v1/diagnoses`
- 契约接口：`DiagnosesApi`
- 主要资源：诊断会话、模型注册表、诊断结果、诊断建议、医生反馈。
- 设计重点：自动结论与人工反馈分层，保留诊断证据快照边界。

### `report`

- 基路径：`/api/v1/reports`
- 契约接口：`ReportApi`
- 主要资源：报告草稿、报告版本、签署动作、报告模板。
- 设计重点：文书生命周期优先于底层表结构，支持版本化和签署扩展。

### `screening`

- 基路径：`/api/v1/screening`
- 契约接口：`ScreeningApi`
- 主要资源：问卷、答卷、院内转介。
- 设计重点：覆盖患者入口到院内审批的完整链路，兼容 SaaS 与私有化入口模式。

### `careplan`

- 基路径：`/api/v1/careplans`
- 契约接口：`CarePlanApi`
- 主要资源：随访计划、任务、患者上报数据、告警与处置动作。
- 设计重点：保证私有化医院在离线场景下也能闭环执行核心随访流程。

### `payment`

- 基路径：`/api/v1/payment`
- 契约接口：`PaymentApi`
- 主要资源：会员套餐、订单、订阅。
- 设计重点：边界仅定义业务状态，不绑定第三方支付网关实现。

### `notify`

- 基路径：`/api/v1/notify`
- 契约接口：`NotifyApi`
- 主要资源：通知消息、送达回执。
- 设计重点：渠道回调采用统一 DTO，不暴露具体供应商 SDK 类型。

### `integration`

- 基路径：`/api/v1/integration`
- 契约接口：`IntegrationApi`
- 主要资源：集成任务、重试补偿。
- 设计重点：响应只返回载荷摘要，不默认回传出域原文，符合最小必要出域原则。

### `license`

- 基路径：`/api/v1/license`
- 契约接口：`LicenseApi`
- 主要资源：许可证状态、许可证上传、激活、验签、发布清单准入、机器指纹。
- 设计重点：支持离线验签和支持期判定，不使用可伪造的纯文本开关。

## 与部署模式的关系

- SaaS：所有接口语义保持统一，租户上下文由平台层提供。
- 私有化：核心诊疗、诊断、报告、随访接口不依赖公网能力。
- 混合通信：`integration`、`notify`、`payment` 的接口设计预留了受控协同能力，但默认遵循最小出域策略。

## 后续实现建议

- 先按模块优先级逐个落控制器实现，并让控制器实现这里定义的接口。
- 写实现时优先复用 `Query/Request/Response/Shared` 分类，不要重新创建语义重复的 DTO。
- 对 `identity`、`clinical`、`diagnoses`、`integration` 的实现补齐越权、脱敏、重放与出域审计测试。

## Controller Contract Layer

为避免后续实现阶段出现“直接新建控制器、绕过既有 HTTP 契约”的漂移，本轮新增了显式的 `*ControllerContract` 接口层。
职责划分如下：

- `*Api`：定义 HTTP 方法、路径、请求头、参数与返回类型，是协议级契约。
- `*ControllerContract`：定义控制器实现应当承接的接口层，后续具体控制器类应实现这一层。
- `*ApiDtos`：定义该模块的 DTO 分类目录。

当前已补齐 controller contract 的模块包括：

- `identity`
- `clinical`
- `diagnoses`
- `report`
- `screening`
- `careplan`
- `payment`
- `notify`
- `integration`
- `license`

`audit` 模块目前已有运行中的具体控制器，因此暂时保持现状，继续以既有控制器承接查询能力。

## Global Error Handling

全局异常处理由 `GlobalExceptionHandler` 统一承接，主要规则如下：

- 参数校验失败、绑定失败、请求体解析失败统一返回 `400`。
- 鉴权拒绝统一返回 `403`。
- 显式抛出的 `ResponseStatusException` 保留原始状态码，并转为统一错误响应体。
- 未分类异常统一返回 `500`，避免直接向外暴露内部异常细节。

统一错误体继续复用 `ApiResponse<Void>`，便于前端与网关做一致处理。
