package xenosoft.imldintelligence.module.audit.internal.api;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AuditQueryAccessGuardTest {

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
