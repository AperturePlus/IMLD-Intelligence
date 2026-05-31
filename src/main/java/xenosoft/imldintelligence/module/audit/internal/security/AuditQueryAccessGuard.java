package xenosoft.imldintelligence.module.audit.internal.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces role-based access to the audit query API.
 *
 * <p>The guard first prefers authenticated Spring Security authorities and falls back to the
 * request role header when security is not fully initialized for the request.</p>
 */
@Component
public class AuditQueryAccessGuard {
    private final AuditProperties properties;

    public AuditQueryAccessGuard(AuditProperties properties) {
        this.properties = properties;
    }

    /**
     * Rejects requests that do not carry at least one role from the configured audit query allowlist.
     *
     * @param request current HTTP request used to resolve fallback role headers
     * @throws ResponseStatusException with status {@link HttpStatus#FORBIDDEN} if the caller is not allowed
     */
    public void assertAllowed(HttpServletRequest request) {
        Set<String> requestRoles = resolveRequestRoles(request);
        if (requestRoles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is required");
        }

        Set<String> allowlist = RoleAuthorityUtils.normalizeRoleCodes(properties.getQueryRoleAllowlist());
        boolean allowed = requestRoles.stream().anyMatch(allowlist::contains);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is not allowed to query audit logs");
        }
    }

    private Set<String> resolveRequestRoles(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
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
