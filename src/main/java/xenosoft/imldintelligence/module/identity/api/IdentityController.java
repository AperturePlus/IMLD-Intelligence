package xenosoft.imldintelligence.module.identity.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.common.CheckPermission;
import xenosoft.imldintelligence.common.RequireAnyRole;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.ConsentRecordService;
import xenosoft.imldintelligence.module.identity.internal.service.PatientService;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.UserManagementService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class IdentityController implements IdentityControllerContract {

    private final AuthService authService;
    private final PatientService patientService;
    private final UserManagementService userManagementService;
    private final ConsentRecordService consentRecordService;
    private final PermissionService permissionService;
    private final IdentityResponseAssembler assembler;
    private final UserAccountRepository userAccountRepository;
    private final TenantRepository tenantRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    @Override
    public ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> login(
            IdentityApiDtos.Request.LoginCommand request) {
        LoginRequest loginReq = new LoginRequest(
                request.username(), request.password(), request.tenantCode());

        AuthToken token = authService.login(loginReq);

        // Load user info for response
        var tenant = resolveTenant(request.tenantCode());
        UserAccount user = userAccountRepository
                .findByUsername(tenant.getId(), request.username())
                .orElse(null);

        Set<String> roleCodes = user != null
                ? permissionService.getEffectiveRoleCodes(tenant.getId(), user.getId())
                : Set.of();

        IdentityApiDtos.Shared.AuthenticatedUserItem userItem = user != null
                ? assembler.toAuthenticatedUserItem(user, roleCodes)
                : null;

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusSeconds(token.expiresIn());

        return ApiResponse.success(new IdentityApiDtos.Response.AuthSessionResponse(
                token.accessToken(), token.refreshToken(), expiresAt, userItem));
    }

    @Override
    public ApiResponse<IdentityApiDtos.Response.AuthSessionResponse> refreshToken(
            IdentityApiDtos.Request.RefreshTokenCommand request) {
        AuthToken token = authService.refreshToken(request.refreshToken());

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusSeconds(token.expiresIn());

        return ApiResponse.success(new IdentityApiDtos.Response.AuthSessionResponse(
                token.accessToken(), token.refreshToken(), expiresAt, null));
    }

    @Override
    public ApiResponse<Void> logout(IdentityApiDtos.Request.LogoutCommand request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success();
    }

    @Override
    @CheckPermission(resource = "PATIENT", action = "READ")
    public ApiResponse<PagedResultResponse<IdentityApiDtos.Response.PatientDetailResponse>> listPatients(
            Long tenantId,
            IdentityApiDtos.Query.PatientPageQuery query,
            PageQueryRequest pageQuery) {
        int page = pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        long total = patientService.countPatients(tenantId, query);
        List<Patient> patients = patientService.listPatients(tenantId, query, offset, size);

        List<IdentityApiDtos.Response.PatientDetailResponse> items = patients.stream()
                .map(assembler::toPatientDetail)
                .toList();

        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @CheckPermission(resource = "PATIENT", action = "CREATE")
    public ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> createPatient(
            Long tenantId,
            IdentityApiDtos.Request.CreatePatientRequest request) {
        Patient patient = patientService.createPatient(tenantId, request);
        return ApiResponse.success(assembler.toPatientDetail(patient));
    }

    @Override
    @CheckPermission(resource = "PATIENT", action = "UPDATE")
    public ApiResponse<IdentityApiDtos.Response.PatientDetailResponse> bindExternalId(
            Long tenantId, Long patientId,
            IdentityApiDtos.Request.BindExternalIdRequest request) {
        Patient patient = patientService.bindExternalId(tenantId, patientId, request);
        return ApiResponse.success(assembler.toPatientDetail(patient));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<PagedResultResponse<IdentityApiDtos.Response.UserAccountResponse>> listUsers(
            Long tenantId,
            IdentityApiDtos.Query.UserAccountPageQuery query,
            PageQueryRequest pageQuery) {
        int page = pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        long total = userManagementService.countUsers(tenantId, query);
        List<UserAccount> users = userManagementService.listUsers(tenantId, query, offset, size);

        List<IdentityApiDtos.Response.UserAccountResponse> items = users.stream()
                .map(assembler::toUserAccountResponse)
                .toList();

        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<IdentityApiDtos.Response.UserAccountResponse> grantRole(
            Long tenantId, Long userId,
            IdentityApiDtos.Request.GrantRoleRequest request) {
        UserAccount user = userManagementService.grantRole(tenantId, userId, request);
        return ApiResponse.success(assembler.toUserAccountResponse(user));
    }

    @Override
    @CheckPermission(resource = "CONSENT", action = "READ")
    public ApiResponse<PagedResultResponse<IdentityApiDtos.Response.ConsentRecordResponse>> listConsents(
            Long tenantId,
            IdentityApiDtos.Query.ConsentRecordPageQuery query,
            PageQueryRequest pageQuery) {
        int page = pageQuery.page() != null ? pageQuery.page() : DEFAULT_PAGE;
        int size = pageQuery.size() != null ? pageQuery.size() : DEFAULT_SIZE;
        long offset = (long) page * size;

        long total = consentRecordService.countConsents(tenantId, query);
        List<ConsentRecord> records = consentRecordService.listConsents(tenantId, query, offset, size);

        List<IdentityApiDtos.Response.ConsentRecordResponse> items = records.stream()
                .map(assembler::toConsentRecordResponse)
                .toList();

        return ApiResponse.success(new PagedResultResponse<>(page, size, total, items));
    }

    @Override
    @CheckPermission(resource = "CONSENT", action = "CREATE")
    public ApiResponse<IdentityApiDtos.Response.ConsentRecordResponse> upsertConsentRecord(
            Long tenantId,
            IdentityApiDtos.Request.UpsertConsentRecordRequest request) {
        ConsentRecord record = consentRecordService.upsertConsent(tenantId, request);
        return ApiResponse.success(assembler.toConsentRecordResponse(record));
    }

    @Override
    @RequireAnyRole({"SYSTEM_ADMIN"})
    public ApiResponse<IdentityApiDtos.Response.PermissionDecisionResponse> evaluatePermission(
            Long tenantId,
            IdentityApiDtos.Request.EvaluatePermissionRequest request) {
        Map<String, Object> resAttr = new java.util.LinkedHashMap<>();
        if (request.resourceAttributes() != null) {
            request.resourceAttributes().fields().forEachRemaining(
                    entry -> resAttr.put(entry.getKey(), entry.getValue()));
        }

        boolean allowed = permissionService.isAllowed(
                tenantId, request.userId(),
                request.resourceType(), request.action(), resAttr);

        Set<String> roleCodes = permissionService.getEffectiveRoleCodes(tenantId, request.userId());
        String decisionSource = roleCodes.contains("SYSTEM_ADMIN") && allowed ? "RBAC_SUPER" : "ABAC";

        return ApiResponse.success(new IdentityApiDtos.Response.PermissionDecisionResponse(
                allowed, decisionSource, null,
                allowed ? "Access granted" : "Access denied"));
    }

    private xenosoft.imldintelligence.module.identity.internal.model.Tenant resolveTenant(String tenantCode) {
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantRepository.findByTenantCode(tenantCode)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        }
        return tenantRepository.listAll().stream()
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active tenant available"));
    }
}
