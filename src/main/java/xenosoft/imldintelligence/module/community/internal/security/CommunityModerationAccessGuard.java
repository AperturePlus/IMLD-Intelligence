package xenosoft.imldintelligence.module.community.internal.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社群内容审核权限守卫。
 *
 * <p>优先使用 Spring Security Authorities，并在未鉴权时回退到请求头角色字段。</p>
 */
@Component
public class CommunityModerationAccessGuard {
    private static final Set<String> DEFAULT_ALLOWLIST = Set.of("SYSTEM_ADMIN");

    public void assertAllowed(HttpServletRequest request) {
        Set<String> roles = resolveRequestRoles(request);
        if (roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is required");
        }

        boolean allowed = roles.stream().anyMatch(DEFAULT_ALLOWLIST::contains);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is not allowed to moderate community content");
        }
    }

    private Set<String> resolveRequestRoles(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return RoleAuthorityUtils.normalizeRoleCodes(authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority == null ? null : grantedAuthority.getAuthority())
                    .collect(Collectors.toList()));
        }

        String roleHeader = request.getHeader(AuditHeaderNames.USER_ROLE);
        if (roleHeader == null || roleHeader.isBlank()) {
            return Set.of();
        }
        return RoleAuthorityUtils.normalizeRoleCodes(Arrays.asList(roleHeader.split(",")));
    }
}

