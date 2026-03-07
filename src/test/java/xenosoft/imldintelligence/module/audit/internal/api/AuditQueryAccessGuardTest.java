package xenosoft.imldintelligence.module.audit.internal.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AuditQueryAccessGuardTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowConfiguredRole() {
        AuditProperties properties = new AuditProperties();
        properties.setQueryRoleAllowlist(List.of("SYSTEM_ADMIN", "COMPLIANCE_AUDITOR"));
        AuditQueryAccessGuard guard = new AuditQueryAccessGuard(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuditHeaderNames.USER_ROLE, "system_admin");

        assertDoesNotThrow(() -> guard.assertAllowed(request));
    }

    @Test
    void shouldAllowConfiguredRoleFromSecurityContext() {
        AuditProperties properties = new AuditProperties();
        properties.setQueryRoleAllowlist(List.of("SYSTEM_ADMIN", "COMPLIANCE_AUDITOR"));
        AuditQueryAccessGuard guard = new AuditQueryAccessGuard(properties);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "user",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertDoesNotThrow(() -> guard.assertAllowed(request));
    }

    @Test
    void shouldPreferSecurityContextAuthoritiesOverSpoofedHeader() {
        AuditProperties properties = new AuditProperties();
        properties.setQueryRoleAllowlist(List.of("SYSTEM_ADMIN"));
        AuditQueryAccessGuard guard = new AuditQueryAccessGuard(properties);

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "user",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"))
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuditHeaderNames.USER_ROLE, "SYSTEM_ADMIN");

        assertThatThrownBy(() -> guard.assertAllowed(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void shouldDenyWhenRoleMissing() {
        AuditProperties properties = new AuditProperties();
        AuditQueryAccessGuard guard = new AuditQueryAccessGuard(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> guard.assertAllowed(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void shouldDenyWhenRoleNotInAllowlist() {
        AuditProperties properties = new AuditProperties();
        properties.setQueryRoleAllowlist(List.of("SYSTEM_ADMIN"));
        AuditQueryAccessGuard guard = new AuditQueryAccessGuard(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuditHeaderNames.USER_ROLE, "DOCTOR");

        assertThatThrownBy(() -> guard.assertAllowed(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }
}
