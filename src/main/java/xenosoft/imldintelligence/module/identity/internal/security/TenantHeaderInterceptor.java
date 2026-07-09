package xenosoft.imldintelligence.module.identity.internal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.util.Objects;
import java.util.Optional;

/**
 * Enforces that the {@code X-Tenant-Id} request header matches the authenticated subject's tenant.
 *
 * <p>Only enforces when both an authenticated {@link UserSubject} and an {@code X-Tenant-Id} header
 * are present. Unauthenticated requests, or requests without the header, are left to the standard
 * authorization rules. A mismatch throws {@link AccessDeniedException}, handled by the existing
 * {@code JsonAccessDeniedHandler} as a 403 response.</p>
 */
@Component
@RequiredArgsConstructor
public class TenantHeaderInterceptor implements HandlerInterceptor {
    static final String TENANT_HEADER = "X-Tenant-Id";

    private final CurrentUserSubjectProvider currentUserSubjectProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<UserSubject> current = currentUserSubjectProvider.getCurrentSubject();
        if (current.isEmpty()) {
            return true;
        }

        String header = request.getHeader(TENANT_HEADER);
        if (header == null || header.isBlank()) {
            return true;
        }

        Long headerTenantId;
        try {
            headerTenantId = Long.valueOf(header.trim());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("X-Tenant-Id header is invalid");
        }

        if (!Objects.equals(headerTenantId, current.get().tenantId())) {
            throw new AccessDeniedException("X-Tenant-Id header does not match authenticated tenant");
        }
        return true;
    }
}
