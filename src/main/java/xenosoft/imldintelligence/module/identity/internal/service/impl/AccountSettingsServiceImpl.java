package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider;
import xenosoft.imldintelligence.module.identity.internal.service.AccountSettingsService;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class AccountSettingsServiceImpl implements AccountSettingsService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String BEARER_PREFIX = "Bearer ";

    private final CurrentUserSubjectProvider currentUserSubjectProvider;
    private final UserAccountRepository userAccountRepository;
    private final PermissionService permissionService;
    private final SensitiveDataEncryptor sensitiveDataEncryptor;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final Optional<AuditTrailService> auditTrailService;

    @Override
    public IdentityApiDtos.Response.AccountProfileResponse getCurrentProfile(Long tenantId) {
        UserSubject subject = requireSubjectInTenant(tenantId);
        UserAccount user = requireActiveUser(subject);
        Set<String> roleCodes = permissionService.getEffectiveRoleCodes(tenantId, user.getId());
        return toProfileResponse(user, roleCodes);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdentityApiDtos.Response.AccountProfileResponse updateCurrentProfile(
            Long tenantId,
            IdentityApiDtos.Request.UpdateAccountProfileCommand request) {
        UserSubject subject = requireSubjectInTenant(tenantId);
        UserAccount user = requireActiveUser(subject);

        String displayName = normalizeOptionalText(request.displayName());
        if (request.displayName() != null) {
            if (displayName == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "displayName must not be blank");
            }
            user.setDisplayName(displayName);
        }

        if (request.deptName() != null) {
            user.setDeptName(normalizeOptionalText(request.deptName()));
        }

        boolean contactChanged = false;
        String newEmail = normalizeOptionalEmail(request.email());
        if (request.email() != null && newEmail == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email must not be blank");
        }
        if (request.email() != null && !equalsIgnoreCase(newEmail, normalizeOptionalText(user.getEmail()))) {
            contactChanged = true;
        }

        String newMobile = normalizeOptionalText(request.mobilePlaintext());
        if (newMobile != null && !Objects.equals(newMobile, decryptMobile(user.getMobileEncrypted()))) {
            contactChanged = true;
        }

        if (contactChanged) {
            requireCurrentPassword(user, request.currentPassword());
        }

        if (request.email() != null && !equalsIgnoreCase(newEmail, normalizeOptionalText(user.getEmail()))) {
            ensureEmailAvailable(tenantId, user.getId(), newEmail);
            user.setEmail(newEmail);
        }

        if (newMobile != null && !Objects.equals(newMobile, decryptMobile(user.getMobileEncrypted()))) {
            user.setMobileEncrypted(sensitiveDataEncryptor.encrypt(newMobile));
        }

        try {
            userAccountRepository.update(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists", ex);
        }

        Set<String> roleCodes = permissionService.getEffectiveRoleCodes(tenantId, user.getId());
        recordAccountAudit(tenantId, user.getId(), roleCodes, "ACCOUNT_PROFILE_UPDATE");
        return toProfileResponse(user, roleCodes);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void changeCurrentPassword(
            Long tenantId,
            IdentityApiDtos.Request.ChangePasswordCommand request,
            String authorizationHeader) {
        UserSubject subject = requireSubjectInTenant(tenantId);
        UserAccount user = requireActiveUser(subject);

        if (!Objects.equals(request.newPassword(), request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match");
        }
        requireCurrentPassword(user, request.currentPassword());

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userAccountRepository.update(user);

        authService.revokeToken(request.refreshToken());
        String accessToken = extractBearerToken(authorizationHeader);
        if (accessToken != null) {
            authService.revokeToken(accessToken);
        }

        Set<String> roleCodes = permissionService.getEffectiveRoleCodes(tenantId, user.getId());
        recordAccountAudit(tenantId, user.getId(), roleCodes, "ACCOUNT_PASSWORD_CHANGE");
    }

    private UserSubject requireSubjectInTenant(Long tenantId) {
        UserSubject subject = currentUserSubjectProvider.requireCurrentSubject();
        if (!Objects.equals(subject.tenantId(), tenantId)) {
            throw new AccessDeniedException("Tenant context does not match authenticated user");
        }
        return subject;
    }

    private UserAccount requireActiveUser(UserSubject subject) {
        return userAccountRepository.findById(subject.tenantId(), subject.userId())
                .filter(user -> STATUS_ACTIVE.equalsIgnoreCase(user.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or inactive"));
    }

    private void requireCurrentPassword(UserAccount user, String currentPassword) {
        String normalized = normalizeOptionalText(currentPassword);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "currentPassword is required for contact changes");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AccessDeniedException("Current password is incorrect");
        }
    }

    private void ensureEmailAvailable(Long tenantId, Long currentUserId, String email) {
        if (email == null) {
            return;
        }
        userAccountRepository.findByEmail(tenantId, email)
                .filter(existing -> !Objects.equals(existing.getId(), currentUserId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                });
    }

    private IdentityApiDtos.Response.AccountProfileResponse toProfileResponse(
            UserAccount user,
            Set<String> roleCodes) {
        List<String> sortedRoles = roleCodes == null
                ? List.of()
                : roleCodes.stream().filter(Objects::nonNull).sorted().toList();
        return new IdentityApiDtos.Response.AccountProfileResponse(
                user.getId(),
                user.getTenantId(),
                user.getUserNo(),
                user.getUsername(),
                user.getDisplayName(),
                user.getUserType(),
                user.getDeptName(),
                user.getEmail(),
                maskMobile(user.getMobileEncrypted()),
                sortedRoles,
                user.getLastLoginAt()
        );
    }

    private String maskMobile(String encryptedMobile) {
        String plaintext = decryptMobile(encryptedMobile);
        return plaintext == null ? null : sensitiveDataEncryptor.mask(plaintext, SensitiveDataEncryptor.MaskType.MOBILE);
    }

    private String decryptMobile(String encryptedMobile) {
        if (encryptedMobile == null || encryptedMobile.isBlank()) {
            return null;
        }
        try {
            return normalizeOptionalText(sensitiveDataEncryptor.decrypt(encryptedMobile));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String normalizeOptionalEmail(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            return null;
        }
        String email = normalized.toLowerCase(Locale.ROOT);
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email format is invalid");
        }
        return email;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.equalsIgnoreCase(right);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String trimmed = authorizationHeader.trim();
        if (!trimmed.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = trimmed.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private void recordAccountAudit(Long tenantId, Long userId, Set<String> roleCodes, String action) {
        auditTrailService.ifPresent(service -> {
            AuditRecordCommand command = new AuditRecordCommand();
            command.setTenantId(tenantId);
            command.setUserId(userId);
            command.setUserRole(roleCodes == null
                    ? null
                    : String.join(",", roleCodes.stream().filter(Objects::nonNull).sorted().toList()));
            command.setAction(action);
            command.setResourceType("USER_ACCOUNT");
            command.setResourceId(String.valueOf(userId));
            service.recordAudit(command);
        });
    }
}
