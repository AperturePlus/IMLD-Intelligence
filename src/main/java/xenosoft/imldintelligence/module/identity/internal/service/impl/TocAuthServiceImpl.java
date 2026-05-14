package xenosoft.imldintelligence.module.identity.internal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.identity.api.toc.dto.TocAuthApiDtos;
import xenosoft.imldintelligence.module.identity.internal.config.IdentityVerificationProperties;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.SmsVerificationCodeRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;
import xenosoft.imldintelligence.module.identity.internal.service.TocAuthService;
import xenosoft.imldintelligence.module.identity.internal.service.TocAuthTenantResolver;
import xenosoft.imldintelligence.module.identity.internal.service.TocUidFactory;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.service.VerificationSmsSender;
import xenosoft.imldintelligence.module.identity.internal.service.WechatMiniappClient;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public class TocAuthServiceImpl implements TocAuthService {

    private static final String USER_TYPE_TOC = "TOC";
    private static final String ROLE_TOC_USER = "TOC_USER";
    private static final String PURPOSE_LOGIN = "LOGIN";

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final TocAuthTenantResolver tenantResolver;
    private final TocUidFactory tocUidFactory;
    private final TocUserRepository tocUserRepository;
    private final SmsVerificationCodeRepository smsVerificationCodeRepository;
    private final SensitiveDataEncryptor sensitiveDataEncryptor;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final IdentityVerificationProperties verificationProperties;
    private final WechatMiniappClient wechatMiniappClient;
    private final ObjectProvider<VerificationSmsSender> smsSenderProvider;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TocAuthApiDtos.Response.TocAuthSessionResponse wechatLogin(TocAuthApiDtos.Request.WechatLoginRequest request) {
        long tenantId = tenantResolver.requireGlobalTenantId();

        WechatMiniappClient.WechatSession session = wechatMiniappClient.code2Session(request.jsCode());
        String tocUid = tocUidFactory.wechatTocUid(session.unionid(), session.openid());

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        TocUser user = tocUserRepository.findByTocUid(tenantId, tocUid).orElseGet(() -> {
            TocUser created = new TocUser();
            created.setTenantId(tenantId);
            created.setTocUid(tocUid);
            created.setNickname(trimToNull(request.nickname()));
            created.setMobileEncrypted(null);
            created.setOpenid(session.openid());
            created.setUnionid(session.unionid());
            created.setVipStatus("NORMAL");
            created.setStatus("ACTIVE");
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            try {
                return tocUserRepository.save(created);
            } catch (DataIntegrityViolationException ex) {
                return tocUserRepository.findByTocUid(tenantId, tocUid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "ToC user creation conflict", ex));
            }
        });

        boolean needUpdate = false;
        if (!Objects.equals(trimToNull(request.nickname()), trimToNull(user.getNickname()))
                && hasText(request.nickname())) {
            user.setNickname(trimToNull(request.nickname()));
            needUpdate = true;
        }
        if (!Objects.equals(session.openid(), trimToNull(user.getOpenid()))) {
            user.setOpenid(session.openid());
            needUpdate = true;
        }
        if (!Objects.equals(session.unionid(), trimToNull(user.getUnionid()))) {
            user.setUnionid(session.unionid());
            needUpdate = true;
        }
        if (needUpdate) {
            tocUserRepository.update(user);
        }

        return issueSession(tenantId, user);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TocAuthApiDtos.Response.PhoneCodeSendResponse sendPhoneLoginCode(TocAuthApiDtos.Request.SendPhoneCodeRequest request) {
        long tenantId = tenantResolver.requireGlobalTenantId();

        String mobile = normalizeMobile(request.mobile());
        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile");
        }

        String mobileHash = tocUidFactory.mobileHash(mobile);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        smsVerificationCodeRepository.findLatestByCondition(tenantId, PURPOSE_LOGIN, mobileHash)
                .ifPresent(latest -> {
                    if (latest.getCreatedAt() == null) {
                        return;
                    }
                    OffsetDateTime allowedAt = latest.getCreatedAt().plus(verificationProperties.getResendInterval());
                    if (allowedAt.isAfter(now)) {
                        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                                "Please retry after " + verificationProperties.getResendInterval().toSeconds() + " seconds");
                    }
                });

        smsVerificationCodeRepository.replacePendingCodes(tenantId, PURPOSE_LOGIN, mobileHash, now);

        String code = generateCode();
        xenosoft.imldintelligence.module.identity.internal.model.SmsVerificationCode record =
                new xenosoft.imldintelligence.module.identity.internal.model.SmsVerificationCode();
        record.setTenantId(tenantId);
        record.setPurpose(PURPOSE_LOGIN);
        record.setMobileHash(mobileHash);
        record.setMobileEncrypted(sensitiveDataEncryptor.encrypt(mobile));
        record.setCodeHash(passwordEncoder.encode(code));
        record.setVerifyAttemptCount(0);
        record.setMaxVerifyAttempts(verificationProperties.getMaxVerifyAttempts());
        record.setStatus("PENDING");
        record.setExpiresAt(now.plus(verificationProperties.getExpiresIn()));
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        smsVerificationCodeRepository.save(record);

        VerificationSmsSender smsSender = smsSenderProvider.getIfAvailable();
        if (smsSender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS service is not configured");
        }
        smsSender.sendVerificationCode(mobile, "login", code, verificationProperties.getExpiresIn());

        return new TocAuthApiDtos.Response.PhoneCodeSendResponse(
                PURPOSE_LOGIN,
                record.getExpiresAt(),
                verificationProperties.getResendInterval().toSeconds()
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TocAuthApiDtos.Response.TocAuthSessionResponse phoneLogin(TocAuthApiDtos.Request.PhoneLoginRequest request) {
        long tenantId = tenantResolver.requireGlobalTenantId();

        String mobile = normalizeMobile(request.mobile());
        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile");
        }
        String code = normalizeRequired(request.code(), "code");

        String mobileHash = tocUidFactory.mobileHash(mobile);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        var latest = smsVerificationCodeRepository.findLatestPending(tenantId, PURPOSE_LOGIN, mobileHash, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is invalid or expired"));

        if (!passwordEncoder.matches(code, latest.getCodeHash())) {
            int nextAttempt = (latest.getVerifyAttemptCount() == null ? 0 : latest.getVerifyAttemptCount()) + 1;
            int maxAttempts = latest.getMaxVerifyAttempts() == null
                    ? verificationProperties.getMaxVerifyAttempts()
                    : latest.getMaxVerifyAttempts();
            boolean locked = nextAttempt >= maxAttempts;
            smsVerificationCodeRepository.incrementVerifyAttempt(
                    tenantId,
                    latest.getId(),
                    nextAttempt,
                    locked ? "LOCKED" : "PENDING",
                    now
            );
            if (locked) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code exceeded max retry attempts");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is incorrect");
        }

        boolean consumed = smsVerificationCodeRepository.consumePendingCode(tenantId, latest.getId(), now);
        if (!consumed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification code is invalid or expired");
        }

        String tocUid = tocUidFactory.mobileTocUid(mobile);
        TocUser user = tocUserRepository.findByTocUid(tenantId, tocUid).orElseGet(() -> {
            TocUser created = new TocUser();
            created.setTenantId(tenantId);
            created.setTocUid(tocUid);
            created.setNickname(null);
            created.setMobileEncrypted(sensitiveDataEncryptor.encrypt(mobile));
            created.setOpenid(null);
            created.setUnionid(null);
            created.setVipStatus("NORMAL");
            created.setStatus("ACTIVE");
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            try {
                return tocUserRepository.save(created);
            } catch (DataIntegrityViolationException ex) {
                return tocUserRepository.findByTocUid(tenantId, tocUid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "ToC user creation conflict", ex));
            }
        });

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ToC user is inactive");
        }

        // refresh encrypted mobile snapshot
        user.setMobileEncrypted(sensitiveDataEncryptor.encrypt(mobile));
        tocUserRepository.update(user);

        return issueSession(tenantId, user);
    }

    @Override
    public TocAuthApiDtos.Response.TocAuthSessionResponse refresh(TocAuthApiDtos.Request.RefreshRequest request) {
        String refreshToken = normalizeRequired(request.refreshToken(), "refreshToken");

        RefreshTokenSubject refreshSubject = jwtUtil.parseRefreshToken(refreshToken);
        if (!USER_TYPE_TOC.equalsIgnoreCase(refreshSubject.userType())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is not valid for ToC session");
        }

        String jti = jwtUtil.extractJti(refreshToken);
        if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked");
        }

        TocUser user = tocUserRepository.findById(refreshSubject.tenantId(), refreshSubject.userId())
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found or inactive"));

        return issueSession(refreshSubject.tenantId(), user, refreshToken);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void logout(TocAuthApiDtos.Request.LogoutRequest request) {
        blacklistToken(request.refreshToken());
    }

    private TocAuthApiDtos.Response.TocAuthSessionResponse issueSession(long tenantId, TocUser user) {
        String refreshToken = jwtUtil.generateRefreshToken(new RefreshTokenSubject(user.getId(), tenantId, USER_TYPE_TOC));
        return issueSession(tenantId, user, refreshToken);
    }

    private TocAuthApiDtos.Response.TocAuthSessionResponse issueSession(long tenantId, TocUser user, String refreshToken) {
        UserSubject subject = new UserSubject(
                user.getId(),
                tenantId,
                USER_TYPE_TOC,
                null,
                Set.of(ROLE_TOC_USER)
        );

        String accessToken = jwtUtil.generateAccessToken(subject);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(jwtUtil.getAccessTokenExpiresInSeconds());

        return new TocAuthApiDtos.Response.TocAuthSessionResponse(
                accessToken,
                refreshToken,
                expiresAt,
                tenantId,
                user.getId(),
                trimToNull(user.getNickname())
        );
    }

    private void blacklistToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String jti = jwtUtil.extractJti(token);
        if (jti == null) {
            return;
        }
        Duration ttl = jwtUtil.getRemainingTtl(token);
        if (!ttl.isZero()) {
            tokenBlacklistService.blacklist(jti, ttl);
        }
    }

    private String generateCode() {
        int codeLength = verificationProperties.getCodeLength();
        int upperExclusive = (int) Math.pow(10, codeLength);
        int value = secureRandom.nextInt(upperExclusive);
        return String.format("%0" + codeLength + "d", value);
    }

    private String normalizeMobile(String value) {
        return normalizeRequired(value, "mobile");
    }

    private String normalizeRequired(String value, String fieldName) {
        String trimmed = value == null ? null : value.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

