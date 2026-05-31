# IMLD-Intelligence

## 启动命令

开发联调统一使用 `spring.profiles.active` 组：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-saas"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private-bridge"
```

`bootRun` 是前台长驻服务任务。看到 `Started ImldIntelligenceApplication` 后即表示后端已启动，不需要等待 Gradle 进度到 100%；停止服务请按 `Ctrl+C`。

启动后可用健康检查确认服务状态：

```powershell
curl http://127.0.0.1:8080/actuator/health
```

`dev-private` 与 `dev-private-bridge` 默认跳过许可证启动验签，避免本地开发缺少许可证文件时启动失败。

如需在开发环境验证真实许可证，可显式开启：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private --imld.licensing.private-edition.startup-validation-enabled=true --imld.licensing.private-edition.public-key-file-path=C:\imld\license\public.pem --imld.licensing.private-edition.license-file-path=C:\imld\license\license.json --imld.licensing.private-edition.activation-state-file-path=C:\imld\license\activation-state.json"
```

也可以通过环境变量开启：

```powershell
$env:IMLD_PRIVATE_STARTUP_VALIDATION_ENABLED="true"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private"
```

交付环境建议使用部署形态 profile：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=saas"
.\gradlew.bat bootRun --args="--spring.profiles.active=private"
.\gradlew.bat bootRun --args="--spring.profiles.active=private,private-bridge"
```

`private` 与 `private,private-bridge` 默认要求签名许可证校验；上线前必须明确 `IMLD_LICENSE_FILE_PATH`、`IMLD_LICENSE_PUBLIC_KEY_FILE_PATH`、`IMLD_ACTIVATION_STATE_FILE_PATH`。
