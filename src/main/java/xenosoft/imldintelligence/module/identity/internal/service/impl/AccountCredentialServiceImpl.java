package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.dto.AuthToken;
import xenosoft.imldintelligence.module.identity.internal.dto.LoginRequest;
import xenosoft.imldintelligence.module.identity.internal.model.EmailVerificationCode;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.EmailVerificationCodeRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.service.AccountCredentialService;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationEmailSender;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class AccountCredentialServiceImpl implements AccountCredentialService {
    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_LOCKED = "LOCKED";

    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final VerificationEmailSender verificationEmailSender;
    private final IdentityVerificationProperties verificationProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdentityApiDtos.Response.EmailCodeSendResponse sendRegistrationEmailCode(
            IdentityApiDtos.Request.SendRegistrationEmailCodeCommand request) {
        Tenant tenant = resolveTenant(request.tenantCode());
        String username = normalizeRequired(request.username(), "username");
        String email = normalizeEmail(request.email());

        if (userAccountRepository.findByUsername(tenant.getId(), username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userAccountRepository.findByEmail(tenant.getId(), email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        return issueAndSendCode(tenant.getId(), username, email, PURPOSE_REGISTER, "registration");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdentityApiDtos.Response.EmailCodeSendResponse sendPasswordResetEmailCode(
            IdentityApiDtos.Request.ForgotPasswordCommand request) {
        Tenant tenant = resolveTenant(request.tenantCode());
        String username = normalizeRequired(request.username(), "username");
        String email = normalizeEmail(request.email());

        UserAccount user = requireActiveUser(tenant.getId(), username);
        if (!emailEquals(user.getEmail(), email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email do not match");
        }
        return issueAndSendCode(tenant.getId(), username, email, PURPOSE_PASSWORD_RESET, "password reset");
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AuthToken register(IdentityApiDtos.Request.RegisterCommand request) {
        Tenant tenant = resolveTenant(request.tenantCode());
        String username = normalizeRequired(request.username(), "username");
        String email = normalizeEmail(request.email());
        String userType = normalizeOptional(request.userType(), "DOCTOR");
        String displayName = normalizeOptional(request.displayName(), username);
        String password = normalizeRequired(request.password(), "password");
        String emailCode = normalizeRequired(request.emailCode(), "emailCode");

        validateCode(tenant.getId(), username, email, PURPOSE_REGISTER, emailCode);

        if (userAccountRepository.findByUsername(tenant.getId(), username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userAccountRepository.findByEmail(tenant.getId(), email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserAccount user = new UserAccount();
        user.setTenantId(tenant.getId());
        user.setUserNo(generateUserNo(tenant.getId()));
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(displayName);
        user.setUserType(userType);
        user.setEmail(email);
        user.setStatus(STATUS_ACTIVE);
        try {
            userAccountRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists", ex);
        }

        bindDefaultRoleIfPresent(tenant.getId(), user.getId(), userType);
        return authService.login(new LoginRequest(username, password, request.tenantCode()));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void resetPassword(IdentityApiDtos.Request.ResetPasswordCommand request) {
        Tenant tenant = resolveTenant(request.tenantCode());
        String username = normalizeRequired(request.username(), "username");
        String email = normalizeEmail(request.email());
        String newPassword = normalizeRequired(request.newPassword(), "newPassword");
        String emailCode = normalizeRequired(request.emailCode(), "emailCode");

        UserAccount user = requireActiveUser(tenant.getId(), username);
        if (!emailEquals(user.getEmail(), email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and email do not match");
        }

        validateCode(tenant.getId(), username, email, PURPOSE_PASSWORD_RESET, emailCode);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.update(user);
    }

    private IdentityApiDtos.Response.EmailCodeSendResponse issueAndSendCode(Long tenantId,
                                                                             String username,
                                                                             String email,
                                                                             String purpose,
                                                                             String scenario) {
        validateVerificationConfig();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        emailVerificationCodeRepository.findLatestByCondition(tenantId, purpose, email, username)
                .ifPresent(latest -> checkResendInterval(latest, now));

        String code = generateCode();
        emailVerificationCodeRepository.replacePendingCodes(tenantId, purpose, email, username, now);

        EmailVerificationCode record = new EmailVerificationCode();
        record.setTenantId(tenantId);
        record.setUsername(username);
        record.setEmail(email);
        record.setPurpose(purpose);
        record.setCodeHash(passwordEncoder.encode(code));
        record.setVerifyAttemptCount(0);
        record.setMaxVerifyAttempts(verificationProperties.getMaxVerifyAttempts());
        record.setStatus(STATUS_PENDING);
        record.setExpiresAt(now.plus(verificationProperties.getExpiresIn()));
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        try {
            emailVerificationCodeRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Verification code already issued", ex);
        }

        verificationEmailSender.sendVerificationCode(email, scenario, code, verificationProperties.getExpiresIn());
        return new IdentityApiDtos.Response.EmailCodeSendResponse(
                email,
                purpose,
                record.getExpiresAt(),
                verificationProperties.getResendInterval().toSeconds()
        );
    }

    private void validateCode(Long tenantId, String username, String email, String purpose, String inputCode) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        EmailVerificationCode latest = emailVerificationCodeRepository
                .findLatestPending(tenantId, purpose, email, username, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is invalid or expired"));

        if (!passwordEncoder.matches(inputCode, latest.getCodeHash())) {
            int nextAttempt = (latest.getVerifyAttemptCount() == null ? 0 : latest.getVerifyAttemptCount()) + 1;
            int maxAttempts = latest.getMaxVerifyAttempts() == null
                    ? verificationProperties.getMaxVerifyAttempts()
                    : latest.getMaxVerifyAttempts();
            boolean locked = nextAttempt >= maxAttempts;
            emailVerificationCodeRepository.incrementVerifyAttempt(
                    tenantId,
                    latest.getId(),
                    nextAttempt,
                    locked ? STATUS_LOCKED : STATUS_PENDING,
                    now
            );
            if (locked) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code exceeded max retry attempts");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is incorrect");
        }

        boolean consumed = emailVerificationCodeRepository.consumePendingCode(tenantId, latest.getId(), now);
        if (!consumed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is invalid or expired");
        }
    }

    private void bindDefaultRoleIfPresent(Long tenantId, Long userId, String userType) {
        Set<String> candidateRoleCodes = new LinkedHashSet<>();
        if (hasText(userType)) {
            candidateRoleCodes.add(userType);
        }
        candidateRoleCodes.add("DOCTOR");

        Optional<Role> role = candidateRoleCodes.stream()
                .map(code -> roleRepository.findByRoleCode(tenantId, code))
                .flatMap(Optional::stream)
                .findFirst();
        if (role.isEmpty()) {
            return;
        }
        if (userRoleRelRepository.findByUserIdAndRoleId(tenantId, userId, role.get().getId()).isPresent()) {
            return;
        }
        UserRoleRel relation = new UserRoleRel();
        relation.setTenantId(tenantId);
        relation.setUserId(userId);
        relation.setRoleId(role.get().getId());
        relation.setGrantedBy(userId);
        try {
            userRoleRelRepository.save(relation);
        } catch (DataIntegrityViolationException ignored) {
            // Idempotent under concurrent grant/register calls.
        }
    }

    private String generateUserNo(Long tenantId) {
        for (int i = 0; i < 5; i++) {
            String candidate = "USR" + System.currentTimeMillis() + randomDigits(4);
            if (userAccountRepository.findByUserNo(tenantId, candidate).isEmpty()) {
                return candidate;
            }
        }
        return "USR" + System.nanoTime() + randomDigits(6);
    }

    private String generateCode() {
        int codeLength = verificationProperties.getCodeLength();
        int upperExclusive = (int) Math.pow(10, codeLength);
        int value = secureRandom.nextInt(upperExclusive);
        return String.format("%0" + codeLength + "d", value);
    }

    private String randomDigits(int length) {
        return secureRandom.ints(length, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private void checkResendInterval(EmailVerificationCode latest, OffsetDateTime now) {
        if (latest.getCreatedAt() == null) {
            return;
        }
        OffsetDateTime allowedAt = latest.getCreatedAt().plus(verificationProperties.getResendInterval());
        if (allowedAt.isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please retry after " + verificationProperties.getResendInterval().toSeconds() + " seconds");
        }
    }

    private Tenant resolveTenant(String tenantCode) {
        if (hasText(tenantCode)) {
            return tenantRepository.findByTenantCode(tenantCode.trim())
                    .filter(t -> STATUS_ACTIVE.equalsIgnoreCase(t.getStatus()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));
        }
        return tenantRepository.listAll().stream()
                .filter(t -> STATUS_ACTIVE.equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active tenant available"));
    }

    private UserAccount requireActiveUser(Long tenantId, String username) {
        return userAccountRepository.findByUsername(tenantId, username)
                .filter(u -> STATUS_ACTIVE.equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found or inactive"));
    }

    private void validateVerificationConfig() {
        if (verificationProperties.getCodeLength() < 4 || verificationProperties.getCodeLength() > 8) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Verification code length must be between 4 and 8");
        }
        if (verificationProperties.getMaxVerifyAttempts() <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "maxVerifyAttempts must be greater than 0");
        }
        if (verificationProperties.getExpiresIn() == null || verificationProperties.getExpiresIn().isNegative() || verificationProperties.getExpiresIn().isZero()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "expiresIn must be greater than 0");
        }
        if (verificationProperties.getResendInterval() == null || verificationProperties.getResendInterval().isNegative()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "resendInterval must not be negative");
        }
    }

    private boolean emailEquals(String a, String b) {
        return normalizeOptional(a, "").equalsIgnoreCase(normalizeOptional(b, ""));
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeRequired(email, "email").toLowerCase(java.util.Locale.ROOT);
        if (!normalized.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email format is invalid");
        }
        return normalized;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeOptional(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
