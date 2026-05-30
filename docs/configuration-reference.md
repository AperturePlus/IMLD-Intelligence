# IMLD 配置参考（Configuration Reference）

## 1. 目标

- 统一管理部署模式、Flyway 行为、SMS/推理 real-mock 选择、生产启动校验规则。
- 保持一套代码同时支持 `SaaS`、`private`、`private-bridge`。

## 2. 启动命令基线

- `dev-saas`: `.\gradlew.bat bootRun --args="--spring.profiles.active=dev-saas"`
- `dev-private`: `.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private"`
- `dev-private-bridge`: `.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private-bridge"`

## 3. 关键配置项

### 3.1 部署与安全

| Key | Env | Default | 说明 |
| --- | --- | --- | --- |
| `imld.deployment.mode` | `IMLD_DEPLOYMENT_MODE` | `saas` | 部署模式：`develop/dev/saas/private`，`hybrid` 兼容映射到 `private` |
| `imld.security.enabled` | `IMLD_SECURITY_ENABLED` | `false` | 全局安全开关 |
| `imld.security.jwt.secret` | `IMLD_JWT_SECRET` | 空 | 安全开启时必须提供（至少 32 bytes） |

### 3.2 Flyway

| Key | Env | Default | 说明 |
| --- | --- | --- | --- |
| `spring.flyway.enabled` | `IMLD_FLYWAY_ENABLED` | `true` | 启用 Flyway |
| `spring.flyway.validate-on-migrate` | `IMLD_FLYWAY_VALIDATE_ON_MIGRATE` | `true` | 迁移前校验 |
| `spring.flyway.out-of-order` | `IMLD_FLYWAY_OUT_OF_ORDER` | `false` | 是否允许乱序迁移 |
| `spring.flyway.clean-disabled` | `IMLD_FLYWAY_CLEAN_DISABLED` | `true` | 是否禁用 `clean` |

### 3.3 身份验证 SMS

| Key | Env | Default | 说明 |
| --- | --- | --- | --- |
| `imld.identity.verification.sms.enabled` | `IMLD_SMS_ENABLED` | `true` | 是否启用短信发送能力 |
| `imld.identity.verification.sms.provider` | `IMLD_SMS_PROVIDER` | `mock` | 提供方：`mock` / `real` |

### 3.4 IMLD 推理

| Key | Env | Default | 说明 |
| --- | --- | --- | --- |
| `imld.inference.imld.engine` | `IMLD_INFERENCE_IMLD_ENGINE` | `xgboost-java` | 引擎选择：`xgboost-java` / `mock` |
| `imld.inference.imld.low-risk-threshold` | `IMLD_INFERENCE_IMLD_LOW_RISK_THRESHOLD` | `0.2` | 低风险阈值上限 |
| `imld.inference.imld.high-risk-threshold` | `IMLD_INFERENCE_IMLD_HIGH_RISK_THRESHOLD` | `0.7` | 高风险阈值下限 |

## 4. Profile 行为矩阵（关键项）

| 配置项 | `dev` | `saas` | `private` |
| --- | --- | --- | --- |
| `imld.deployment.mode` | `develop` | `saas` | `private` |
| `imld.security.enabled` | `false` | `true` | `true` |
| `imld.identity.verification.sms.enabled` | `true` | `false` | `false` |
| `imld.identity.verification.sms.provider` | `mock` | `real` | `real` |
| `imld.inference.imld.engine` | `mock` | `xgboost-java` | `xgboost-java` |
| `spring.flyway.out-of-order` | `true` | `false` | `false` |
| `spring.flyway.clean-disabled` | `false` | `true` | `true` |
| `spring.flyway.validate-on-migrate` | `true` | `true` | `true` |

## 5. 启动 Fail-Fast 规则（ConfigurationGuard）

- `imld.deployment.mode` 非法值：拒绝启动。
- `saas/private` 下 `imld.security.enabled=false`：拒绝启动。
- `saas/private` 下 `sms.provider=mock`：拒绝启动。
- `saas/private` 下 `inference.engine=mock`：拒绝启动。
- `sms.enabled=true && sms.provider=real` 且无真实 `VerificationSmsSender` Bean：拒绝启动。

## 6. Flyway 迁移规则

- 已移除反模式 `V99__comments.sql`，改为 `V10__comments.sql`，后续版本必须线性递增（`V11`、`V12`...）。
- 禁止使用 `99/999` 作为“永远最后”的占位版本号。

## 7. 本地恢复 Runbook（开发环境）

适用：已确认本地库数据无需保留，且需要从头重放迁移。

1. 清空 `public` schema：

```powershell
docker exec imld-postgresql-16 psql -U imld_user -d imld_core -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

2. 重新启动应用（示例）：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-saas"
```

3. 验证迁移历史连续（V1..V10）：

```powershell
docker exec imld-postgresql-16 psql -U imld_user -d imld_core -c "SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"
```
