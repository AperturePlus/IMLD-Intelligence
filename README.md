# IMLD-Intelligence

IMLD-Intelligence 是一个面向医疗场景的 `Spring Boot` 模块化单体系统，支持同一代码基线下的三种运行形态：

- `SaaS`
- `private`（院内部署）
- `private + private-bridge`（院内部署并开启受控云通信）

## 环境准备

- JDK `21`
- PostgreSQL（默认 `localhost:5432/imld_core`）
- Redis（默认 `localhost:6379`）
- Windows 下命令示例均使用 `.\gradlew.bat`（Linux/macOS 可替换为 `./gradlew`）

## 启动命令（开发联调）

### 1) SaaS

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-saas"
```

### 2) Private（严格验签+激活）

先配置许可证相关环境变量：

```powershell
$env:IMLD_LICENSE_FILE_PATH="D:\imld\license\license.json"
$env:IMLD_LICENSE_PUBLIC_KEY_FILE_PATH="D:\imld\license\public.pem"
$env:IMLD_ACTIVATION_STATE_FILE_PATH="D:\imld\license\activation-state.json"
```

启动：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private"
```

如果只是本地功能联调、暂不做激活验签，可临时关闭启动校验：

```powershell
$env:IMLD_PRIVATE_STARTUP_VALIDATION_ENABLED="false"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private"
```

### 3) Private + Bridge（受控云通信）

```powershell
$env:IMLD_LICENSE_FILE_PATH="D:\imld\license\license.json"
$env:IMLD_LICENSE_PUBLIC_KEY_FILE_PATH="D:\imld\license\public.pem"
$env:IMLD_ACTIVATION_STATE_FILE_PATH="D:\imld\license\activation-state.json"
.\gradlew.bat bootRun --args="--spring.profiles.active=dev-private-bridge"
```

## 启动命令（部署环境）

- `saas`：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=saas"
```

- `private`：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=private"
```

- `private + private-bridge`：

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=private,private-bridge"
```

## Private 激活 CLI

导入并激活许可证（离线）：

```powershell
.\gradlew.bat bootRun --args="--spring.main.web-application-type=none --spring.profiles.active=private --license-cli-command=import-license --license-cli-license-file=D:\imld\license\license.json --license-cli-public-key-file=D:\imld\license\public.pem --license-cli-activation-code=ACT-2026-LOCAL --license-cli-target-license-file=D:\imld\runtime\license\license.json --license-cli-expected-mode=private --license-cli-machine-binding=true"
```

更多参数见：[docs/license-cli.md](docs/license-cli.md)
