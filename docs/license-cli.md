# License 运维 CLI

该 CLI 用于私有化部署场景的许可证导入与升级可用性判断。

## 命令一：导入许可证

```powershell
.\gradlew.bat bootRun --args="--spring.main.web-application-type=none --spring.profiles.active=private --license-cli-command=import-license --license-cli-license-file=C:\imld\license\license.json --license-cli-public-key-file=C:\imld\license\public.pem --license-cli-activation-code=ACT-2026-LOCAL --license-cli-target-license-file=C:\imld\runtime\license\license.json --license-cli-expected-mode=private --license-cli-machine-binding=true"
```

成功输出关键字段：
- `LICENSE_IMPORT_OK`
- `licenseId`
- `hospitalId`
- `supportEndDate`
- `installedPath`

## 命令二：检查升级是否允许

```powershell
.\gradlew.bat bootRun --args="--spring.main.web-application-type=none --spring.profiles.active=private --license-cli-command=check-upgrade --license-cli-license-file=C:\imld\runtime\license\license.json --license-cli-public-key-file=C:\imld\license\public.pem --license-cli-manifest-file=C:\imld\upgrade\release-manifest.json --license-cli-activation-code=ACT-2026-LOCAL --license-cli-expected-mode=private --license-cli-enforce-support-window=true --license-cli-allow-security-patch-after-expiry=true --license-cli-machine-binding=true"
```

输出结构：
- `UPGRADE_DECISION`
- `allowed=true|false`
- `reason`
- `licenseId`
- `supportEndDate`
- `releaseVersion`
- `releaseDate`

当 `allowed=false` 时，命令会抛出异常并返回失败。

## 参数说明

- `--license-cli-command`
  - `import-license`
  - `check-upgrade`
- `--license-cli-license-file`：签名许可证文件路径。
- `--license-cli-public-key-file`：厂商公钥文件路径（PEM）。
- `--license-cli-activation-code`：医院现场输入激活码。
- `--license-cli-manifest-file`：签名升级清单路径（仅 `check-upgrade` 必填）。
- `--license-cli-target-license-file`：导入目标路径（仅 `import-license`，可选）。
- `--license-cli-expected-mode`：默认 `private`。
- `--license-cli-machine-binding`：默认 `true`。
- `--license-cli-enforce-support-window`：默认读取 `imld.upgrade.entitlement.enforce-support-window`。
- `--license-cli-allow-security-patch-after-expiry`：默认读取 `imld.upgrade.entitlement.allow-security-patch-after-expiry`。
