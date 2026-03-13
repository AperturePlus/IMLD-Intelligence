package xenosoft.imldintelligence.module.identity.api;

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
import xenosoft.imldintelligence.common.dto.ApiResponse;
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
public interface IdentityApi {

    /**
     * 用户登录。
     */
    @PostMapping("/auth/login")
    ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> login(
            @Valid @RequestBody IdentityApiDtos.Request.LoginCommand request
    );

    /**
     * 刷新令牌。
     */
    @PostMapping("/auth/refresh")
    ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> refreshToken(
            @Valid @RequestBody IdentityApiDtos.Request.RefreshTokenCommand request
    );

    /**
     * 退出登录。
     */
    @PostMapping("/auth/logout")
    ApiResponse<Void> logout(
            @Valid @RequestBody IdentityApiDtos.Request.LogoutCommand request
    );

    /**
     * 分页查询患者。
     */
    @GetMapping("/patients")
    ApiResponse<PagedResultResponse<IdentityApiDtos.Response.PatientDetailResponse>> listPatients(
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
    ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> createPatient(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.CreatePatientRequest request
    );

    /**
     * 绑定患者外部标识。
     */
    @PostMapping("/patients/{patientId}/external-ids")
    ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> bindExternalId(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("patientId")
            @Positive(message = "patientId must be positive")
            Long patientId,
            @Valid @RequestBody IdentityApiDtos.Request.BindExternalIdRequest request
    );

    /**
     * 分页查询用户账号。
     */
    @GetMapping("/users")
    ApiResponse<PagedResultResponse<IdentityApiDtos.Response.UserAccountResponse>> listUsers(
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
    ApiResponse<IdentityApiDtos.Response.UserAccountResponse> grantRole(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @PathVariable("userId")
            @Positive(message = "userId must be positive")
            Long userId,
            @Valid @RequestBody IdentityApiDtos.Request.GrantRoleRequest request
    );

    /**
     * 分页查询知情同意记录。
     */
    @GetMapping("/consents")
    ApiResponse<PagedResultResponse<IdentityApiDtos.Response.ConsentRecordResponse>> listConsents(
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
    ApiResponse<IdentityApiDtos.Response.ConsentRecordResponse> upsertConsentRecord(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.UpsertConsentRecordRequest request
    );

    /**
     * 发起 ABAC/RBAC 联合权限判定。
     */
    @PostMapping("/permissions/decision")
    ApiResponse<IdentityApiDtos.Response.PermissionDecisionResponse> evaluatePermission(
            @RequestHeader("X-Tenant-Id")
            @Positive(message = "tenantId must be positive")
            Long tenantId,
            @Valid @RequestBody IdentityApiDtos.Request.EvaluatePermissionRequest request
    );
}
