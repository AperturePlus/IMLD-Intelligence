package xenosoft.imldintelligence.module.identity.aop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import xenosoft.imldintelligence.common.CheckPermission;
import xenosoft.imldintelligence.common.RequireAnyRole;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAspectTest {

    @Mock
    private PermissionService permissionService;

    private ProtectedService protectedService;

    @BeforeEach
    void setUp() {
        PermissionAspect permissionAspect = new PermissionAspect(permissionService, new CurrentUserSubjectProvider());
        AspectJProxyFactory factory = new AspectJProxyFactory(new ProtectedService());
        factory.addAspect(permissionAspect);
        protectedService = factory.getProxy();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectCheckPermissionWhenAuthenticationMissing() {
        assertThatThrownBy(() -> protectedService.readPatient(Map.of("patientId", 1001L)))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void shouldDelegateCheckPermissionToPermissionService() {
        authenticate(new UserSubject(11L, 22L, "DOCTOR", "ICU", Set.of("DOCTOR")));
        when(permissionService.isAllowed(
                eq(22L),
                eq(11L),
                eq("PATIENT"),
                eq("READ"),
                argThat(attributes -> attributes != null
                        && attributes.get("patientId").equals(1001L)
                        && attributes.get("scope").equals("FULL")
                        && attributes.get("tenantId").equals(22L)
                        && attributes.get("userId").equals(11L))
        )).thenReturn(true);

        assertThat(protectedService.readPatient(Map.of("patientId", 1001L, "scope", "FULL"))).isEqualTo("ok");
        verify(permissionService).isAllowed(
                eq(22L),
                eq(11L),
                eq("PATIENT"),
                eq("READ"),
                argThat(attributes -> attributes != null && attributes.get("patientId").equals(1001L))
        );
    }

    @Test
    void shouldThrowAccessDeniedWhenPermissionServiceRejects() {
        authenticate(new UserSubject(11L, 22L, "DOCTOR", "ICU", Set.of("DOCTOR")));
        when(permissionService.isAllowed(eq(22L), eq(11L), eq("PATIENT"), eq("READ"), argThat(attributes -> true)))
                .thenReturn(false);

        assertThatThrownBy(() -> protectedService.readPatient(Map.of("patientId", 1001L)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Patient access denied");
    }

    @Test
    void shouldAllowRequireAnyRoleWhenRoleMatches() {
        authenticate(new UserSubject(11L, 22L, "ADMIN", "HQ", Set.of("compliance_auditor")));

        assertThat(protectedService.complianceOnly()).isEqualTo("audit");
    }

    @Test
    void shouldDenyRequireAnyRoleWhenRoleDoesNotMatch() {
        authenticate(new UserSubject(11L, 22L, "DOCTOR", "ICU", Set.of("DOCTOR")));

        assertThatThrownBy(() -> protectedService.complianceOnly())
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Compliance role required");
    }

    private void authenticate(UserSubject userSubject) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        userSubject,
                        "N/A",
                        userSubject.roleCodes().stream().map(SimpleGrantedAuthority::new).toList()
                )
        );
    }

    static class ProtectedService {
        @CheckPermission(resource = "PATIENT", action = "READ", message = "Patient access denied")
        String readPatient(Map<String, Object> resourceAttributes) {
            return "ok";
        }

        @RequireAnyRole(value = {"SYSTEM_ADMIN", "COMPLIANCE_AUDITOR"}, message = "Compliance role required")
        String complianceOnly() {
            return "audit";
        }
    }
}
