package xenosoft.imldintelligence.module.identity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import xenosoft.imldintelligence.module.identity.internal.model.ConsentRecord;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.ConsentRecordRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.GuardianRelationRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientExternalIdRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.security.IdentityRequestAuthorizationCustomizer;
import xenosoft.imldintelligence.module.identity.internal.security.IdentitySecurityConfiguration;
import xenosoft.imldintelligence.module.identity.internal.service.AuthService;
import xenosoft.imldintelligence.module.identity.internal.service.ConsentRecordService;
import xenosoft.imldintelligence.module.identity.internal.service.PatientService;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.service.UserManagementService;
import xenosoft.imldintelligence.module.identity.internal.service.impl.AesSensitiveDataEncryptor;
import xenosoft.imldintelligence.module.identity.internal.service.impl.AuthServiceImpl;
import xenosoft.imldintelligence.module.identity.internal.service.impl.ConsentRecordServiceImpl;
import xenosoft.imldintelligence.module.identity.internal.service.impl.PatientServiceImpl;
import xenosoft.imldintelligence.module.identity.internal.service.impl.PermissionServiceImpl;
import xenosoft.imldintelligence.module.identity.internal.service.impl.UserManagementServiceImpl;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = IdentityControllerIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisabledInAotMode
@TestPropertySource(properties = {
        "imld.security.enabled=true",
        "imld.security.public-paths[0]=/error",
        "imld.security.public-paths[1]=/api/v1/identity/auth/**",
        "imld.security.jwt.issuer=imld-test",
        "imld.security.jwt.secret=01234567890123456789012345678901",
        "imld.security.jwt.access-token-ttl=15m",
        "imld.security.jwt.refresh-token-ttl=7d",
        "imld.security.jwt.clock-skew=0s",
        "imld.security.data-encryption-key=01234567890123456789012345678901"
})
class IdentityControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserAccountRepository userAccountRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String doctorToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateAccessToken(
                new UserSubject(1L, 1L, "ADMIN", "HQ", Set.of("SYSTEM_ADMIN")));
        doctorToken = jwtUtil.generateAccessToken(
                new UserSubject(2L, 1L, "DOCTOR", "ICU", Set.of("DOCTOR")));
    }

    @Test
    void loginEndpointIsPublic() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantCode("HOSP_A");
        tenant.setStatus("ACTIVE");

        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setTenantId(1L);
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setDisplayName("Admin");
        user.setUserType("ADMIN");
        user.setStatus("ACTIVE");

        when(tenantRepository.findByTenantCode("HOSP_A")).thenReturn(Optional.of(tenant));
        when(tenantRepository.listAll()).thenReturn(List.of(tenant));
        when(userAccountRepository.findByUsername(1L, "admin")).thenReturn(Optional.of(user));
        when(userAccountRepository.update(any())).thenReturn(user);

        mockMvc.perform(post("/api/v1/identity/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"password123","tenantCode":"HOSP_A"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void protectedEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/identity/patients")
                        .header("X-Tenant-Id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPatientsWithValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/identity/patients")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void createPatientWithValidToken() throws Exception {
        mockMvc.perform(post("/api/v1/identity/patients")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "patientNo": "P-TEST-001",
                                    "patientName": "Test Patient",
                                    "gender": "MALE",
                                    "patientType": "INPATIENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.patientNo").value("P-TEST-001"));
    }

    @Test
    void listUsersRequiresSystemAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/identity/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + doctorToken)
                        .header("X-Tenant-Id", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsersSucceedsForSystemAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/identity/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void listConsentsWithValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/identity/consents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .header("X-Tenant-Id", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void logoutEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/identity/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"some-token"}
                                """))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class,
            MybatisPlusAutoConfiguration.class,
            RedisAutoConfiguration.class,
            RedisReactiveAutoConfiguration.class,
            RedisRepositoriesAutoConfiguration.class,
            KafkaAutoConfiguration.class
    })
    @Import({
            IdentitySecurityConfiguration.class,
            JwtUtil.class,
            IdentityRequestAuthorizationCustomizer.class,
            IdentityController.class,
            IdentityResponseAssembler.class,
            xenosoft.imldintelligence.module.identity.internal.aop.PermissionAspect.class,
            xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider.class
    })
    static class TestApplication {

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        SensitiveDataEncryptor sensitiveDataEncryptor() {
            return new AesSensitiveDataEncryptor("01234567890123456789012345678901");
        }

        @Bean
        TokenBlacklistService tokenBlacklistService() {
            TokenBlacklistService svc = mock(TokenBlacklistService.class);
            when(svc.isBlacklisted(anyString())).thenReturn(false);
            return svc;
        }

        @Bean
        TenantRepository tenantRepository() {
            return mock(TenantRepository.class);
        }

        @Bean
        UserAccountRepository userAccountRepository() {
            UserAccountRepository repo = mock(UserAccountRepository.class);
            when(repo.countByCondition(anyLong(), any(), any(), any(), any())).thenReturn(0L);
            when(repo.listByCondition(anyLong(), any(), any(), any(), any(), anyLong(), anyInt()))
                    .thenReturn(List.of());

            // Stub findById for PermissionServiceImpl.loadSubject() used by @CheckPermission
            UserAccount admin = new UserAccount();
            admin.setId(1L);
            admin.setTenantId(1L);
            admin.setUsername("admin");
            admin.setUserType("ADMIN");
            admin.setStatus("ACTIVE");
            when(repo.findById(1L, 1L)).thenReturn(Optional.of(admin));

            UserAccount doctor = new UserAccount();
            doctor.setId(2L);
            doctor.setTenantId(1L);
            doctor.setUsername("doctor");
            doctor.setUserType("DOCTOR");
            doctor.setStatus("ACTIVE");
            when(repo.findById(1L, 2L)).thenReturn(Optional.of(doctor));

            return repo;
        }

        @Bean
        PatientRepository patientRepository() {
            PatientRepository repo = mock(PatientRepository.class);
            when(repo.countByCondition(anyLong(), any(), any(), any(), any())).thenReturn(0L);
            when(repo.listByCondition(anyLong(), any(), any(), any(), any(), anyLong(), anyInt()))
                    .thenReturn(List.of());
            when(repo.save(any())).thenAnswer(invocation -> {
                Patient p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });
            return repo;
        }

        @Bean
        PatientExternalIdRepository patientExternalIdRepository() {
            PatientExternalIdRepository repo = mock(PatientExternalIdRepository.class);
            when(repo.listByPatientId(anyLong(), anyLong())).thenReturn(List.of());
            return repo;
        }

        @Bean
        GuardianRelationRepository guardianRelationRepository() {
            GuardianRelationRepository repo = mock(GuardianRelationRepository.class);
            when(repo.listByPatientId(anyLong(), anyLong())).thenReturn(List.of());
            return repo;
        }

        @Bean
        ConsentRecordRepository consentRecordRepository() {
            ConsentRecordRepository repo = mock(ConsentRecordRepository.class);
            when(repo.countByCondition(anyLong(), any(), any(), any())).thenReturn(0L);
            when(repo.listByCondition(anyLong(), any(), any(), any(), anyLong(), anyInt()))
                    .thenReturn(List.of());
            return repo;
        }

        @Bean
        RoleRepository roleRepository() {
            RoleRepository repo = mock(RoleRepository.class);
            Role sysAdminRole = new Role();
            sysAdminRole.setId(1L);
            sysAdminRole.setTenantId(1L);
            sysAdminRole.setRoleCode("SYSTEM_ADMIN");
            sysAdminRole.setStatus("ACTIVE");
            when(repo.findById(1L, 1L)).thenReturn(Optional.of(sysAdminRole));
            return repo;
        }

        @Bean
        UserRoleRelRepository userRoleRelRepository() {
            UserRoleRelRepository repo = mock(UserRoleRelRepository.class);
            // Default: no roles
            when(repo.listByUserId(anyLong(), anyLong())).thenReturn(List.of());
            // Admin user (userId=1) has SYSTEM_ADMIN role (roleId=1)
            UserRoleRel adminRoleRel = new UserRoleRel();
            adminRoleRel.setUserId(1L);
            adminRoleRel.setRoleId(1L);
            when(repo.listByUserId(1L, 1L)).thenReturn(List.of(adminRoleRel));
            return repo;
        }

        @Bean
        AbacPolicyRepository abacPolicyRepository() {
            AbacPolicyRepository repo = mock(AbacPolicyRepository.class);
            when(repo.listByTenantId(anyLong())).thenReturn(List.of());
            return repo;
        }

        // Service beans
        @Bean
        PermissionService permissionService(UserAccountRepository userAccountRepository,
                                             UserRoleRelRepository userRoleRelRepository,
                                             RoleRepository roleRepository,
                                             AbacPolicyRepository abacPolicyRepository) {
            return new PermissionServiceImpl(userAccountRepository, userRoleRelRepository,
                    roleRepository, abacPolicyRepository);
        }

        @Bean
        AuthService authService(UserAccountRepository userAccountRepository,
                                 TenantRepository tenantRepository,
                                 PermissionService permissionService,
                                 JwtUtil jwtUtil,
                                 PasswordEncoder passwordEncoder,
                                 TokenBlacklistService tokenBlacklistService) {
            return new AuthServiceImpl(userAccountRepository, tenantRepository,
                    permissionService, jwtUtil, passwordEncoder, tokenBlacklistService);
        }

        @Bean
        PatientService patientService(PatientRepository patientRepository,
                                       PatientExternalIdRepository patientExternalIdRepository,
                                       SensitiveDataEncryptor sensitiveDataEncryptor) {
            return new PatientServiceImpl(patientRepository, patientExternalIdRepository,
                    sensitiveDataEncryptor);
        }

        @Bean
        UserManagementService userManagementService(UserAccountRepository userAccountRepository,
                                                     UserRoleRelRepository userRoleRelRepository,
                                                     RoleRepository roleRepository) {
            return new UserManagementServiceImpl(userAccountRepository, userRoleRelRepository,
                    roleRepository);
        }

        @Bean
        ConsentRecordService consentRecordService(ConsentRecordRepository consentRecordRepository) {
            return new ConsentRecordServiceImpl(consentRecordRepository);
        }
    }
}
