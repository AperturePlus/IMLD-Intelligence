package xenosoft.imldintelligence.module.identity.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserRoleRelRepository userRoleRelRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AbacPolicyRepository abacPolicyRepository;

    private PermissionServiceImpl permissionServiceImpl;

    @BeforeEach
    void setUp() {
        permissionServiceImpl = new PermissionServiceImpl(userAccountRepository, userRoleRelRepository, roleRepository, abacPolicyRepository);
    }

    @Test
    void shouldReturnEffectiveRoleCodesFromRepositories() {
        when(userRoleRelRepository.listByUserId(1L, 2L)).thenReturn(List.of(userRoleRel(10L), userRoleRel(11L)));
        when(roleRepository.findById(1L, 10L)).thenReturn(Optional.of(role(10L, "doctor")));
        when(roleRepository.findById(1L, 11L)).thenReturn(Optional.of(role(11L, "ROLE_case_manager")));

        assertThat(permissionServiceImpl.getEffectiveRoleCodes(1L, 2L))
                .containsExactlyInAnyOrder("DOCTOR", "CASE_MANAGER");
    }

    @Test
    void shouldLoadSubjectFromRepositories() {
        when(userAccountRepository.findById(1L, 2L)).thenReturn(Optional.of(userAccount(1L, 2L, "doctor", "Oncology")));
        when(userRoleRelRepository.listByUserId(1L, 2L)).thenReturn(List.of(userRoleRel(10L)));
        when(roleRepository.findById(1L, 10L)).thenReturn(Optional.of(role(10L, "doctor")));

        UserSubject subject = permissionServiceImpl.loadSubject(1L, 2L);

        assertThat(subject).isEqualTo(new UserSubject(2L, 1L, "DOCTOR", "Oncology", java.util.Set.of("DOCTOR")));
    }

    @Test
    void shouldAllowSystemAdminWithoutPolicy() {
        when(userAccountRepository.findById(1L, 2L)).thenReturn(Optional.of(userAccount(1L, 2L, "admin", "HQ")));
        when(userRoleRelRepository.listByUserId(1L, 2L)).thenReturn(List.of(userRoleRel(99L)));
        when(roleRepository.findById(1L, 99L)).thenReturn(Optional.of(role(99L, "SYSTEM_ADMIN")));


        boolean allowed = permissionServiceImpl.isAllowed(1L, 2L, "PATIENT", "READ", Map.of("patientId", 1001L));

        assertThat(allowed).isTrue();
    }

    @Test
    void shouldAllowWhenMatchingAbacPolicyExists() throws Exception {
        when(userAccountRepository.findById(1L, 2L)).thenReturn(Optional.of(userAccount(1L, 2L, "doctor", "ICU")));
        when(userRoleRelRepository.listByUserId(1L, 2L)).thenReturn(List.of(userRoleRel(10L)));
        when(roleRepository.findById(1L, 10L)).thenReturn(Optional.of(role(10L, "DOCTOR")));

        AbacPolicy allowPolicy = new AbacPolicy();
        allowPolicy.setTenantId(1L);
        allowPolicy.setPolicyCode("ALLOW_READ_OWN_PATIENT");
        allowPolicy.setPolicyName("allow own patient read");
        allowPolicy.setStatus("ACTIVE");
        allowPolicy.setEffect("ALLOW");
        allowPolicy.setPriority(10);
        allowPolicy.setSubjectExpr(OBJECT_MAPPER.readTree("{\"role\":[\"DOCTOR\"],\"deptName\":\"ICU\"}"));
        allowPolicy.setResourceExpr(OBJECT_MAPPER.readTree("{\"resource\":\"PATIENT\",\"ownerUserId\":\"$subject.userId\"}"));
        allowPolicy.setActionExpr(OBJECT_MAPPER.readTree("{\"actions\":[\"READ\",\"UPDATE\"]}"));
        when(abacPolicyRepository.listByTenantId(1L)).thenReturn(List.of(allowPolicy));

        boolean allowed = permissionServiceImpl.isAllowed(1L, 2L, "PATIENT", "READ", Map.of("ownerUserId", 2L));

        assertThat(allowed).isTrue();
    }

    @Test
    void shouldDenyWhenHigherPriorityDenyPolicyMatches() throws Exception {
        when(userAccountRepository.findById(1L, 2L)).thenReturn(Optional.of(userAccount(1L, 2L, "doctor", "ICU")));
        when(userRoleRelRepository.listByUserId(1L, 2L)).thenReturn(List.of(userRoleRel(10L)));
        when(roleRepository.findById(1L, 10L)).thenReturn(Optional.of(role(10L, "DOCTOR")));

        AbacPolicy denyPolicy = new AbacPolicy();
        denyPolicy.setTenantId(1L);
        denyPolicy.setPolicyCode("DENY_EXPORT");
        denyPolicy.setPolicyName("deny export");
        denyPolicy.setStatus("ACTIVE");
        denyPolicy.setEffect("DENY");
        denyPolicy.setPriority(1);
        denyPolicy.setSubjectExpr(OBJECT_MAPPER.readTree("{\"role\":\"DOCTOR\"}"));
        denyPolicy.setResourceExpr(OBJECT_MAPPER.readTree("{\"resource\":\"PATIENT\"}"));
        denyPolicy.setActionExpr(OBJECT_MAPPER.readTree("{\"action\":\"EXPORT\"}"));

        AbacPolicy allowPolicy = new AbacPolicy();
        allowPolicy.setTenantId(1L);
        allowPolicy.setPolicyCode("ALLOW_EXPORT");
        allowPolicy.setPolicyName("allow export");
        allowPolicy.setStatus("ACTIVE");
        allowPolicy.setEffect("ALLOW");
        allowPolicy.setPriority(10);
        allowPolicy.setSubjectExpr(OBJECT_MAPPER.readTree("{\"role\":\"DOCTOR\"}"));
        allowPolicy.setResourceExpr(OBJECT_MAPPER.readTree("{\"resource\":\"PATIENT\"}"));
        allowPolicy.setActionExpr(OBJECT_MAPPER.readTree("{\"action\":\"EXPORT\"}"));
        when(abacPolicyRepository.listByTenantId(1L)).thenReturn(List.of(allowPolicy, denyPolicy));

        boolean allowed = permissionServiceImpl.isAllowed(1L, 2L, "PATIENT", "EXPORT", Map.of("ownerUserId", 2L));

        assertThat(allowed).isFalse();
    }

    private UserAccount userAccount(Long tenantId, Long userId, String userType, String deptName) {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(userId);
        userAccount.setTenantId(tenantId);
        userAccount.setStatus("ACTIVE");
        userAccount.setUserType(userType);
        userAccount.setDeptName(deptName);
        return userAccount;
    }

    private UserRoleRel userRoleRel(Long roleId) {
        UserRoleRel userRoleRel = new UserRoleRel();
        userRoleRel.setRoleId(roleId);
        return userRoleRel;
    }

    private Role role(Long roleId, String roleCode) {
        Role role = new Role();
        role.setId(roleId);
        role.setStatus("ACTIVE");
        role.setRoleCode(roleCode);
        return role;
    }
}


