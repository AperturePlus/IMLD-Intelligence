package xenosoft.imldintelligence.module.identity.internal.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.util.Optional;

/**
 * 当前用户主体提供器，用于从安全上下文读取当前登录用户。
 */
@Component
public class CurrentUserSubjectProvider {

    public Optional<UserSubject> getCurrentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserSubject userSubject)) {
            return Optional.empty();
        }
        return Optional.of(new UserSubject(
                userSubject.userId(),
                userSubject.tenantId(),
                userSubject.userType(),
                userSubject.deptName(),
                RoleAuthorityUtils.normalizeRoleCodes(userSubject.roleCodes())
        ));
    }

    public UserSubject requireCurrentSubject() {
        return getCurrentSubject().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Authenticated user context is required."));
    }
}
