package xenosoft.imldintelligence.module.identity.internal.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.module.community.internal.model.CommunityBoard;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBoardRepository;
import xenosoft.imldintelligence.module.identity.internal.model.*;
import xenosoft.imldintelligence.module.identity.internal.repository.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Dev 环境数据种子，启动时自动创建测试账号与基础数据。
 * 仅在 spring profile = dev 时生效，生产环境不会加载此 Bean。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeedRunner implements CommandLineRunner {

    private static final String TENANT_CODE = "IMLD_COMMUNITY";
    private static final String TOC_UID_LIXIAOHUA = "dev_toc_001";
    private static final String ADMIN_USERNAME = "admin";

    private final TenantRepository tenantRepository;
    private final TocUserRepository tocUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(String... args) {
        log.info("[DevDataSeed] Starting dev data seeding...");

        Tenant tenant = seedTenant();
        long tenantId = tenant.getId();

        seedTocUser(tenantId);
        UserAccount admin = seedAdminUser(tenantId);
        Role adminRole = seedRole(tenantId, "SYSTEM_ADMIN", "系统管理员");
        seedRole(tenantId, "TOC_USER", "普通用户");
        bindUserRole(tenantId, admin.getId(), adminRole.getId());
        seedCommunityBoards(tenantId);

        log.info("[DevDataSeed] Completed. tenantId={}, tenantCode={}", tenantId, tenant.getTenantCode());
    }

    private Tenant seedTenant() {
        return tenantRepository.findByTenantCode(TENANT_CODE)
                .orElseGet(() -> {
                    Tenant t = new Tenant();
                    t.setTenantCode(TENANT_CODE);
                    t.setTenantName("IMLD 社群测试租户");
                    t.setDeployMode("DEVELOP");
                    t.setStatus("ACTIVE");
                    t.setCreatedAt(now());
                    t.setUpdatedAt(now());
                    Tenant saved = tenantRepository.save(t);
                    log.info("[DevDataSeed] Created tenant: {}", TENANT_CODE);
                    return saved;
                });
    }

    private void seedTocUser(long tenantId) {
        tocUserRepository.findByTocUid(tenantId, TOC_UID_LIXIAOHUA)
                .ifPresentOrElse(
                        u -> log.info("[DevDataSeed] ToC user already exists: {}", u.getTocUid()),
                        () -> {
                            TocUser user = new TocUser();
                            user.setTenantId(tenantId);
                            user.setTocUid(TOC_UID_LIXIAOHUA);
                            user.setNickname("李晓华");
                            user.setMobileEncrypted(null);
                            user.setOpenid(null);
                            user.setUnionid(null);
                            user.setVipStatus("NORMAL");
                            user.setStatus("ACTIVE");
                            user.setCreatedAt(now());
                            user.setUpdatedAt(now());
                            tocUserRepository.save(user);
                            log.info("[DevDataSeed] Created ToC user: {}", TOC_UID_LIXIAOHUA);
                        }
                );
    }

    private UserAccount seedAdminUser(long tenantId) {
        return userAccountRepository.findByUsername(tenantId, ADMIN_USERNAME)
                .orElseGet(() -> {
                    UserAccount account = new UserAccount();
                    account.setTenantId(tenantId);
                    account.setUserNo("dev_admin_001");
                    account.setUsername(ADMIN_USERNAME);
                    account.setPasswordHash(passwordEncoder.encode("admin123"));
                    account.setDisplayName("系统管理员");
                    account.setUserType("ADMIN");
                    account.setDeptName("信息科");
                    account.setMobileEncrypted(null);
                    account.setEmail("admin@imld.test");
                    account.setStatus("ACTIVE");
                    account.setCreatedAt(now());
                    account.setUpdatedAt(now());
                    UserAccount saved = userAccountRepository.save(account);
                    log.info("[DevDataSeed] Created admin user: {}", ADMIN_USERNAME);
                    return saved;
                });
    }

    private Role seedRole(long tenantId, String roleCode, String roleName) {
        return roleRepository.findByRoleCode(tenantId, roleCode)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setTenantId(tenantId);
                    role.setRoleCode(roleCode);
                    role.setRoleName(roleName);
                    role.setDescription("Dev seed role: " + roleCode);
                    role.setStatus("ACTIVE");
                    role.setCreatedAt(now());
                    Role saved = roleRepository.save(role);
                    log.info("[DevDataSeed] Created role: {}", roleCode);
                    return saved;
                });
    }

    private void bindUserRole(long tenantId, long userId, long roleId) {
        userRoleRelRepository.findByUserIdAndRoleId(tenantId, userId, roleId)
                .ifPresentOrElse(
                        rel -> log.info("[DevDataSeed] UserRoleRel already exists: userId={}, roleId={}", userId, roleId),
                        () -> {
                            UserRoleRel rel = new UserRoleRel();
                            rel.setTenantId(tenantId);
                            rel.setUserId(userId);
                            rel.setRoleId(roleId);
                            rel.setGrantedAt(now());
                            userRoleRelRepository.save(rel);
                            log.info("[DevDataSeed] Bound user {} to role {}", userId, roleId);
                        }
                );
    }

    private void seedCommunityBoards(long tenantId) {
        seedBoard(tenantId, "general", "综合交流", "IMLD 患者互助、日常交流", 0);
        seedBoard(tenantId, "experience", "经验分享", "治疗经验、康复心得分享", 1);
        seedBoard(tenantId, "diet", "饮食交流", "肝病患者饮食管理与食谱分享", 2);
        seedBoard(tenantId, "qa", "医患问答", "向医生提问、获取专业建议", 3);
    }

    private void seedBoard(long tenantId, String code, String name, String desc, int sort) {
        communityBoardRepository.findByBoardCode(tenantId, code)
                .ifPresentOrElse(
                        b -> log.info("[DevDataSeed] Board already exists: {}", code),
                        () -> {
                            CommunityBoard board = new CommunityBoard();
                            board.setTenantId(tenantId);
                            board.setBoardCode(code);
                            board.setBoardName(name);
                            board.setDescription(desc);
                            board.setDiseaseScope("IMLD");
                            board.setStatus("ACTIVE");
                            board.setSortOrder(sort);
                            board.setCreatedAt(now());
                            board.setUpdatedAt(now());
                            communityBoardRepository.save(board);
                            log.info("[DevDataSeed] Created board: {}", code);
                        }
                );
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
