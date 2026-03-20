package xenosoft.imldintelligence.module.identity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;

/**
 * 身份与权限模块 HTTP 契约。
 *
 * <p>该接口仅声明边界，不注册控制器实现。接口设计默认要求调用方提供租户上下文，
 * 并在响应中优先返回脱敏后的身份数据。</p>
 */
@Validated
@RequestMapping("/api/v1/identity")
@Tag(name = "Identity & Access Management", description = "用户认证、患者管理、权限控制与知情同意管理 APIs")
public interface IdentityApi {

    /**
     * 用户登录。
     */
    @PostMapping("/auth/login")
    @Operation(
            summary = "用户登录",
            description = "通过用户名、密码和租户代码进行身份认证，返回 JWT 访问令牌和刷新令牌",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功，返回访问令牌和用户信息"),
            @ApiResponse(responseCode = "401", description = "认证失败，用户名或密码错误"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> login(
            @Valid @RequestBody IdentityApiDtos.Request.LoginCommand request
    );

    /**
     * 刷新令牌。
     */
    @PostMapping("/auth/refresh")
    @Operation(
            summary = "刷新访问令牌",
            description = "使用有效的刷新令牌获取新的访问令牌",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "令牌刷新成功"),
            @ApiResponse(responseCode = "401", description = "刷新令牌无效或已过期")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> refreshToken(
            @Valid @RequestBody IdentityApiDtos.Request.RefreshTokenCommand request
    );

    /**
     * 退出登录。
     */
    @PostMapping("/auth/logout")
    @Operation(
            summary = "退出登录",
            description = "撤销刷新令牌，使其失效",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "退出成功"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<Void> logout(
            @Valid @RequestBody IdentityApiDtos.Request.LogoutCommand request
    );

    /**
     * 分页查询患者。
     */
    @GetMapping("/patients")
    @Operation(
            summary = "分页查询患者",
            description = "根据查询条件分页获取患者列表，需要 PATIENT:READ 权限",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<PagedResultResponse<IdentityApiDtos.Response.PatientDetailResponse>> listPatients(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute IdentityApiDtos.Query.PatientPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    /**
     * 新增患者档案。
     */
    @PostMapping("/patients")
    @Operation(
            summary = "创建患者档案",
            description = "创建新的患者档案记录，需要 PATIENT:CREATE 权限",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> createPatient(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.CreatePatientRequest request
    );

    /**
     * 绑定患者外部标识。
     */
    @PostMapping("/patients/{patientId}/external-ids")
    @Operation(
            summary = "绑定患者外部标识",
            description = "为患者绑定外部系统的标识符，需要 PATIENT:UPDATE 权限",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "绑定成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "患者不存在")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> bindExternalId(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Parameter(description = "患者 ID", required = true)
            @PathVariable("patientId")
            @Positive(message = "patientId must be positive")
            Long patientId,
            @Valid @RequestBody IdentityApiDtos.Request.BindExternalIdRequest request
    );

    /**
     * 分页查询用户账号。
     */
    @GetMapping("/users")
    @Operation(
            summary = "分页查询用户账号",
            description = "查询系统用户账号列表，仅 SYSTEM_ADMIN 角色可访问",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<PagedResultResponse<IdentityApiDtos.Response.UserAccountResponse>> listUsers(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute IdentityApiDtos.Query.UserAccountPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    /**
     * 给用户授予角色。
     */
    @PostMapping("/users/{userId}/roles")
    @Operation(
            summary = "授予用户角色",
            description = "为指定用户授予新的角色，仅 SYSTEM_ADMIN 角色可访问",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "授予成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.UserAccountResponse> grantRole(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Parameter(description = "用户 ID", required = true)
            @PathVariable("userId")
            @Positive(message = "userId must be positive")
            Long userId,
            @Valid @RequestBody IdentityApiDtos.Request.GrantRoleRequest request
    );

    /**
     * 分页查询知情同意记录。
     */
    @GetMapping("/consents")
    @Operation(
            summary = "分页查询知情同意记录",
            description = "查询患者知情同意记录，需要 CONSENT:READ 权限",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<PagedResultResponse<IdentityApiDtos.Response.ConsentRecordResponse>> listConsents(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @ModelAttribute IdentityApiDtos.Query.ConsentRecordPageQuery query,
            @Valid @ModelAttribute PageQueryRequest pageQuery
    );

    /**
     * 录入知情同意记录。
     */
    @PostMapping("/consents")
    @Operation(
            summary = "录入知情同意记录",
            description = "创建或更新患者知情同意记录，需要 CONSENT:CREATE 权限",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "录入成功"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.ConsentRecordResponse> upsertConsentRecord(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.UpsertConsentRecordRequest request
    );

    /**
     * 发起 ABAC/RBAC 联合权限判定。
     */
    @PostMapping("/permissions/decision")
    @Operation(
            summary = "评估权限",
            description = "基于 ABAC/RBAC 进行权限判定，仅 SYSTEM_ADMIN 角色可访问",
            security = {@SecurityRequirement(name = "Bearer Authentication"), @SecurityRequirement(name = "Tenant ID Header")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "评估完成"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    xenosoft.imldintelligence.common.dto.ApiResponse<IdentityApiDtos.Response.PermissionDecisionResponse> evaluatePermission(
            @Parameter(description = "租户 ID", required = true, in = ParameterIn.HEADER)
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.EvaluatePermissionRequest request
    );
}
