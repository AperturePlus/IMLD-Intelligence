package xenosoft.imldintelligence.module.identity.api.toc;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.identity.api.toc.dto.TocAuthApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.security.RefreshTokenSubject;
import xenosoft.imldintelligence.module.identity.internal.service.TocAuthTenantResolver;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Dev 环境免登录控制器，仅在 spring profile = dev 时加载。
 * 提供一键登录接口，自动为预设测试用户（李晓华）签发有效 Token。
 */
@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/api/v1/app/toc/auth")
public class DevAuthController {

    private static final String DEV_TOC_UID = "dev_toc_001";
    private static final String USER_TYPE_TOC = "TOC";
    private static final String ROLE_TOC_USER = "TOC_USER";

    private final TenantRepository tenantRepository;
    private final TocUserRepository tocUserRepository;
    private final TocAuthTenantResolver tenantResolver;
    private final JwtUtil jwtUtil;

    @PostMapping("/dev/login")
    public ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> devLogin() {
        long tenantId = tenantResolver.requireGlobalTenantId();

        TocUser user = tocUserRepository.findByTocUid(tenantId, DEV_TOC_UID)
                .orElseThrow(() -> new IllegalStateException(
                        "Dev user not found: " + DEV_TOC_UID + ". Please ensure DevDataSeedRunner has executed."));

        UserSubject subject = new UserSubject(
                user.getId(),
                tenantId,
                USER_TYPE_TOC,
                null,
                Set.of(ROLE_TOC_USER)
        );

        String accessToken = jwtUtil.generateAccessToken(subject);
        String refreshToken = jwtUtil.generateRefreshToken(
                new RefreshTokenSubject(user.getId(), tenantId, USER_TYPE_TOC));

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC)
                .plusSeconds(jwtUtil.getAccessTokenExpiresInSeconds());

        TocAuthApiDtos.Response.TocAuthSessionResponse session =
                new TocAuthApiDtos.Response.TocAuthSessionResponse(
                        accessToken,
                        refreshToken,
                        expiresAt,
                        tenantId,
                        user.getId(),
                        user.getNickname()
                );

        return ApiResponse.success(session);
    }
}
