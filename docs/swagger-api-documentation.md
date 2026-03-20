# Swagger/OpenAPI 接口文档

## 概述

IMLD Intelligence 使用 Springdoc OpenAPI 3.0 提供交互式 API 文档和测试界面。

## 访问 Swagger UI

### 本地开发环境

启动应用后，访问以下 URL：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### 部署环境

根据部署模式访问：

- **SaaS 模式**: https://your-domain.com/swagger-ui.html
- **私有化部署**: http://hospital-internal-ip:port/swagger-ui.html
- **混合模式**: 根据网络配置访问

## 功能特性

### 1. 部署模式感知

配置会自动检测部署模式（SaaS/Private/Hybrid），并在 API 文档中显示相应信息。

### 2. 安全认证

API 文档包含两种安全认证方案：

- **Bearer Authentication**: JWT 访问令牌认证
  - 在 Swagger UI 中点击右上角 "Authorize" 按钮
  - 输入格式：`Bearer YOUR_JWT_TOKEN`

- **Tenant ID Header**: 多租户标识
  - 在请求头中添加 `X-Tenant-Id`
  - 值为租户的数字 ID

### 3. API 分组

文档按业务模块分组：

- **Identity & Access Management**: 用户认证、患者管理、权限控制
- **Audit & Compliance**: 审计日志查询
- **Clinical Operations**: 临床业务（待补充）
- **Care Planning & Screening**: 护理计划与筛查（待补充）
- **Payment & Subscriptions**: 支付与订阅（待补充）

### 4. 交互式测试

在 Swagger UI 中可以：

- 查看所有 API 端点
- 查看请求/响应模型
- 直接发送测试请求
- 查看实际响应结果

## 配置选项

### 启用/禁用 Swagger

通过环境变量控制：

```bash
# 启用 API 文档（默认：true）
IMLD_SWAGGER_ENABLED=true

# 启用 Swagger UI（默认：true）
IMLD_SWAGGER_UI_ENABLED=true
```

### 配置文件

在 `application.yml` 中配置：

```yaml
springdoc:
  api-docs:
    enabled: ${IMLD_SWAGGER_ENABLED:true}
    path: /v3/api-docs
  swagger-ui:
    enabled: ${IMLD_SWAGGER_UI_ENABLED:true}
    path: /swagger-ui.html
    operations-sorter: alpha
    tags-sorter: alpha
```

## 部署建议

### SaaS 部署

- 建议保持 Swagger UI 启用，方便客户查看 API 文档
- 可在生产环境配置访问控制（如 IP 白名单）

### 私有化部署

- 默认启用，方便院内开发者和运维人员使用
- 如需禁用，设置环境变量：
  ```bash
  IMLD_SWAGGER_ENABLED=false
  IMLD_SWAGGER_UI_ENABLED=false
  ```

### 混合部署

- 建议在院内网络启用
- 不建议将 Swagger UI 暴露到互联网

## 安全配置

Swagger UI 和 API 文档端点已添加到公共路径白名单：

```yaml
imld:
  security:
    public-paths:
      - /swagger-ui.html
      - /swagger-ui/**
      - /v3/api-docs/**
      - /swagger-resources/**
      - /webjars/**
```

**注意**: 如果 `imld.security.enabled=true`，需要确保这些路径在安全配置中正确设置。

## 添加新的 API 文档

### 1. 为 Controller 添加标签

```java
@Tag(name = "Module Name", description = "模块描述")
@RestController
@RequestMapping("/api/v1/module")
public class YourController {
    // ...
}
```

### 2. 为端点添加操作描述

```java
@Operation(
    summary = "操作摘要",
    description = "详细描述",
    security = {@SecurityRequirement(name = "Bearer Authentication")}
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "成功"),
    @ApiResponse(responseCode = "403", description = "无权限")
})
@GetMapping("/endpoint")
public ResponseEntity<?> endpoint() {
    // ...
}
```

### 3. 为参数添加描述

```java
@Parameter(description = "参数描述", required = true)
@RequestParam String param
```

## 故障排查

### Swagger UI 无法访问

1. 检查应用是否启动成功
2. 检查配置：`springdoc.swagger-ui.enabled=true`
3. 检查安全配置是否正确
4. 查看日志是否有错误信息

### API 端点未显示

1. 确保 Controller 类有 `@RestController` 注解
2. 确保方法有正确的 HTTP 映射注解
3. 检查是否有 `@ConditionalOnProperty` 限制
4. 重启应用重新生成文档

### 认证测试失败

1. 先通过 `/api/v1/identity/auth/login` 获取 JWT token
2. 在 Swagger UI 点击 "Authorize" 按钮
3. 输入格式：`Bearer <token>`（注意 Bearer 后有空格）
4. 确保 token 未过期

## 参考资料

- [Springdoc OpenAPI 官方文档](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI 使用指南](https://swagger.io/tools/swagger-ui/)
