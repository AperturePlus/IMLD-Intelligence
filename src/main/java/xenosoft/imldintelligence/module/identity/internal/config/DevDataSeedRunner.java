package xenosoft.imldintelligence.module.identity.internal.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticVariantRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorDictRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;
import xenosoft.imldintelligence.module.community.internal.model.CommunityBoard;
import xenosoft.imldintelligence.module.community.internal.repository.CommunityBoardRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisRecommendation;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DoctorFeedback;
import xenosoft.imldintelligence.module.diagnoses.internal.model.ModelRegistry;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisRecommendationRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DoctorFeedbackRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.ModelRegistryRepository;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.TocUser;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TocUserRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Dev 环境数据种子，启动时自动创建测试账号、患者、临床证据与诊断会话。
 * 仅在 spring profile = dev 时生效，生产环境不会加载此 Bean。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeedRunner implements CommandLineRunner {

    public static final String TENANT_CODE = "IMLD_COMMUNITY";
    public static final String DOCTOR_USERNAME = "doctor";
    public static final String DOCTOR_PASSWORD = "123456";
    private static final String TOC_UID_LIXIAOHUA = "dev_toc_001";
    private static final String ADMIN_USERNAME = "admin";
    private static final String DEV_MODEL_CODE = "IMLD_XGBOOST";
    private static final String DEV_MODEL_VERSION = "v2_gene_clinical_fusion";
    private static final String DEV_SEED_SOURCE = "DEV_SEED";

    private static final List<PatientSeed> PATIENT_SEEDS = List.of(
            new PatientSeed("P001", "林建国", "男", 58, "高", "遗传性血色病", "一般", "MZ8849201", LocalDate.of(2023, 11, 20)),
            new PatientSeed("P002", "陈婉婷", "女", 32, "低", "肝豆状核变性 (Wilson病)", "极佳", "MZ8849205", LocalDate.of(2023, 11, 21)),
            new PatientSeed("P003", "张明远", "男", 45, "中", "α1-抗胰蛋白酶缺乏症", "良好", "MZ8849212", LocalDate.of(2023, 11, 22)),
            new PatientSeed("P004", "王淑芬", "女", 62, "高", "代谢相关脂肪性肝病", "良好", "MZ8849218", LocalDate.of(2023, 11, 23)),
            new PatientSeed("P005", "李浩宇", "男", 28, "低", "Gilbert综合征", "良好", "MZ8849220", LocalDate.of(2023, 11, 24)),
            new PatientSeed("P006", "赵雪梅", "女", 51, "中", "遗传性血色病", "一般", "MZ8849227", LocalDate.of(2023, 11, 25)),
            new PatientSeed("P007", "刘振华", "男", 66, "高", "肝豆状核变性 (Wilson病)", "差", "MZ8849231", LocalDate.of(2023, 11, 26)),
            new PatientSeed("P008", "周小雅", "女", 24, "低", "肝豆状核变性 (Wilson病)", "良好", "MZ8849238", LocalDate.of(2023, 11, 27)),
            new PatientSeed("P009", "吴建强", "男", 53, "高", "遗传性血色病", "一般", "MZ8849242", LocalDate.of(2023, 11, 28)),
            new PatientSeed("P010", "郑丽丽", "女", 38, "中", "肝豆状核变性 (Wilson病)", "良好", "MZ8849248", LocalDate.of(2023, 11, 29)),
            new PatientSeed("P011", "孙立军", "男", 41, "低", "遗传性血色病", "一般", "MZ8849254", LocalDate.of(2023, 11, 30)),
            new PatientSeed("P012", "马桂英", "女", 71, "高", "代谢相关脂肪性肝病", "一般", "MZ8849260", LocalDate.of(2023, 12, 1))
    );

    private static final List<DiagnosisSeed> DIAGNOSIS_SEEDS = List.of(
            new DiagnosisSeed("REP-202311-001", "P001", "MZ8849201", "COMPLETED", null,
                    "HEMOCHROMATOSIS", "遗传性血色病", 0.89D, "高风险",
                    "血清铁蛋白 850 ng/mL (显著升高), 转铁蛋白饱和度 65% (异常)。",
                    "皮肤色素沉着伴轻度肝肿大，无角膜 K-F 环。",
                    "同意 AI 辅助诊断意见。患者铁代谢指标显著异常，结合临床表型，考虑遗传性血色病可能性大。",
                    "建议：完善 HFE 基因检测，评估后考虑启动静脉放血治疗，并严格限制高铁饮食摄入。"),
            new DiagnosisSeed("REP-202311-002", "P002", "MZ8849205", "REVIEWED", "MODIFY",
                    "WILSON_DISEASE", "肝豆状核变性 (Wilson病)", 0.96D, "高风险",
                    "铜蓝蛋白 0.08 g/L (极低), 24h尿铜 215 μg (升高), ALT 125 U/L。",
                    "双眼角膜 K-F 环 (+)，伴有轻微非对称性手部震颤。",
                    "根据生化指标及裂隙灯检查结果（K-F环阳性），Wilson病诊断明确。",
                    "立即启动青霉胺驱铜治疗，严格低铜饮食，并建议一级亲属开展 ATP7B 基因筛查。"),
            new DiagnosisSeed("REP-202311-003", "P003", "MZ8849212", "COMPLETED", null,
                    "AAT_DEFICIENCY", "α1-抗胰蛋白酶缺乏症", 0.85D, "高风险",
                    "血清 α1-抗胰蛋白酶水平 < 0.5 g/L (显著降低)。",
                    "早期肺气肿改变，伴有不明原因肝硬化。", "", "")
    );

    private final TenantRepository tenantRepository;
    private final TocUserRepository tocUserRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final AbacPolicyRepository abacPolicyRepository;
    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final IndicatorDictRepository indicatorDictRepository;
    private final LabResultRepository labResultRepository;
    private final ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;
    private final GeneticReportRepository geneticReportRepository;
    private final GeneticVariantRepository geneticVariantRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final ModelRegistryRepository modelRegistryRepository;
    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final DiagnosisRecommendationRepository diagnosisRecommendationRepository;
    private final DoctorFeedbackRepository doctorFeedbackRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(String... args) {
        log.info("[DevDataSeed] Starting dev data seeding...");

        Tenant tenant = seedTenant();
        long tenantId = tenant.getId();

        seedTocUser(tenantId);
        UserAccount admin = seedAdminUser(tenantId);
        UserAccount doctor = seedDoctorUser(tenantId);
        Role adminRole = seedRole(tenantId, "SYSTEM_ADMIN", "系统管理员");
        Role doctorRole = seedRole(tenantId, "DOCTOR", "医生");
        seedRole(tenantId, "TOC_USER", "普通用户");
        bindUserRole(tenantId, admin.getId(), adminRole.getId());
        bindUserRole(tenantId, doctor.getId(), doctorRole.getId());
        seedDoctorPatientPolicy(tenantId);
        seedCommunityBoards(tenantId);
        ModelRegistry model = seedModelRegistry(tenantId);
        seedPatientsAndClinicalData(tenantId, doctor.getId());
        seedDiagnosisData(tenantId, doctor.getId(), model.getId());

        log.info("[DevDataSeed] Completed. tenantId={}, tenantCode={}", tenantId, tenant.getTenantCode());
    }

    private Tenant seedTenant() {
        return tenantRepository.findByTenantCode(TENANT_CODE).orElseGet(() -> {
            Tenant t = new Tenant();
            t.setTenantCode(TENANT_CODE);
            t.setTenantName("IMLD 医生工作站测试租户");
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
        tocUserRepository.findByTocUid(tenantId, TOC_UID_LIXIAOHUA).ifPresentOrElse(
                u -> log.debug("[DevDataSeed] ToC user already exists: {}", u.getTocUid()),
                () -> {
                    TocUser user = new TocUser();
                    user.setTenantId(tenantId);
                    user.setTocUid(TOC_UID_LIXIAOHUA);
                    user.setNickname("李晓华");
                    user.setVipStatus("NORMAL");
                    user.setStatus("ACTIVE");
                    user.setCreatedAt(now());
                    user.setUpdatedAt(now());
                    tocUserRepository.save(user);
                });
    }

    private UserAccount seedAdminUser(long tenantId) {
        return userAccountRepository.findByUsername(tenantId, ADMIN_USERNAME).orElseGet(() -> {
            UserAccount account = new UserAccount();
            account.setTenantId(tenantId);
            account.setUserNo("dev_admin_001");
            account.setUsername(ADMIN_USERNAME);
            account.setPasswordHash(passwordEncoder.encode("admin123"));
            account.setDisplayName("系统管理员");
            account.setUserType("ADMIN");
            account.setDeptName("信息科");
            account.setEmail("admin@imld.test");
            account.setStatus("ACTIVE");
            account.setCreatedAt(now());
            account.setUpdatedAt(now());
            return userAccountRepository.save(account);
        });
    }

    private UserAccount seedDoctorUser(long tenantId) {
        Optional<UserAccount> existing = userAccountRepository.findByUsername(tenantId, DOCTOR_USERNAME);
        if (existing.isPresent()) {
            UserAccount account = existing.get();
            account.setUserNo(defaultIfBlank(account.getUserNo(), "dev_doctor_001"));
            account.setPasswordHash(passwordEncoder.encode(DOCTOR_PASSWORD));
            account.setDisplayName("张医生");
            account.setUserType("DOCTOR");
            account.setDeptName("肝病医学科");
            account.setEmail("doctor@imld.local");
            account.setStatus("ACTIVE");
            userAccountRepository.update(account);
            return account;
        }

        UserAccount account = new UserAccount();
        account.setTenantId(tenantId);
        account.setUserNo("dev_doctor_001");
        account.setUsername(DOCTOR_USERNAME);
        account.setPasswordHash(passwordEncoder.encode(DOCTOR_PASSWORD));
        account.setDisplayName("张医生");
        account.setUserType("DOCTOR");
        account.setDeptName("肝病医学科");
        account.setEmail("doctor@imld.local");
        account.setStatus("ACTIVE");
        account.setCreatedAt(now());
        account.setUpdatedAt(now());
        return userAccountRepository.save(account);
    }

    private Role seedRole(long tenantId, String roleCode, String roleName) {
        return roleRepository.findByRoleCode(tenantId, roleCode).orElseGet(() -> {
            Role role = new Role();
            role.setTenantId(tenantId);
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setDescription("Dev seed role: " + roleCode);
            role.setStatus("ACTIVE");
            role.setCreatedAt(now());
            return roleRepository.save(role);
        });
    }

    private void bindUserRole(long tenantId, long userId, long roleId) {
        userRoleRelRepository.findByUserIdAndRoleId(tenantId, userId, roleId).ifPresentOrElse(
                rel -> log.debug("[DevDataSeed] UserRoleRel already exists: userId={}, roleId={}", userId, roleId),
                () -> {
                    UserRoleRel rel = new UserRoleRel();
                    rel.setTenantId(tenantId);
                    rel.setUserId(userId);
                    rel.setRoleId(roleId);
                    rel.setGrantedAt(now());
                    userRoleRelRepository.save(rel);
                });
    }

    private void seedDoctorPatientPolicy(long tenantId) {
        String policyCode = "DEV_DOCTOR_PATIENT_RW";
        abacPolicyRepository.findByPolicyCode(tenantId, policyCode).ifPresentOrElse(
                policy -> log.debug("[DevDataSeed] ABAC policy already exists: {}", policyCode),
                () -> {
                    ObjectNode actionExpr = objectMapper.createObjectNode();
                    actionExpr.putArray("actions").add("READ").add("CREATE").add("UPDATE");

                    AbacPolicy policy = new AbacPolicy();
                    policy.setTenantId(tenantId);
                    policy.setPolicyCode(policyCode);
                    policy.setPolicyName("Dev doctor patient read-write");
                    policy.setSubjectExpr(objectMapper.createObjectNode().put("role", "DOCTOR"));
                    policy.setResourceExpr(objectMapper.createObjectNode().put("resource", "PATIENT"));
                    policy.setActionExpr(actionExpr);
                    policy.setEffect("ALLOW");
                    policy.setPriority(100);
                    policy.setStatus("ACTIVE");
                    policy.setCreatedAt(now());
                    abacPolicyRepository.save(policy);
                });
    }

    private void seedCommunityBoards(long tenantId) {
        seedBoard(tenantId, "general", "综合交流", "IMLD 患者互助、日常交流", 0);
        seedBoard(tenantId, "experience", "经验分享", "治疗经验、康复心得分享", 1);
        seedBoard(tenantId, "diet", "饮食交流", "肝病患者饮食管理与食谱分享", 2);
        seedBoard(tenantId, "qa", "医患问答", "向医生提问、获取专业建议", 3);
    }

    private void seedBoard(long tenantId, String code, String name, String desc, int sort) {
        communityBoardRepository.findByBoardCode(tenantId, code).ifPresentOrElse(
                b -> log.debug("[DevDataSeed] Board already exists: {}", code),
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
                });
    }

    private ModelRegistry seedModelRegistry(long tenantId) {
        return modelRegistryRepository.findByModelCodeAndModelVersion(tenantId, DEV_MODEL_CODE, DEV_MODEL_VERSION)
                .map(model -> {
                    if (!"ACTIVE".equalsIgnoreCase(model.getStatus())) {
                        model.setStatus("ACTIVE");
                        modelRegistryRepository.update(model);
                    }
                    return model;
                }).orElseGet(() -> {
                    ModelRegistry model = new ModelRegistry();
                    model.setTenantId(tenantId);
                    model.setModelCode(DEV_MODEL_CODE);
                    model.setModelName("IMLD XGBoost Inference");
                    model.setModelType("ML");
                    model.setModelVersion(DEV_MODEL_VERSION);
                    model.setProvider("LOCAL");
                    model.setStatus("ACTIVE");
                    model.setReleasedAt(now());
                    return modelRegistryRepository.save(model);
                });
    }

    private void seedPatientsAndClinicalData(long tenantId, long doctorId) {
        for (PatientSeed seed : PATIENT_SEEDS) {
            Patient patient = seedPatient(tenantId, seed);
            Encounter encounter = seedEncounter(tenantId, patient.getId(), doctorId, seed);
            seedClinicalDecision(tenantId, patient.getId(), encounter.getId(), doctorId, seed);
            seedLabs(tenantId, patient.getId(), encounter.getId(), seed);
            seedPathology(tenantId, patient.getId(), encounter.getId(), doctorId, seed);
            seedGenetics(tenantId, patient.getId(), encounter.getId(), seed);
        }
    }

    private Patient seedPatient(long tenantId, PatientSeed seed) {
        return patientRepository.findByPatientNo(tenantId, seed.patientNo()).orElseGet(() -> {
            Patient patient = new Patient();
            patient.setTenantId(tenantId);
            patient.setPatientNo(seed.patientNo());
            patient.setPatientName(seed.name());
            patient.setGender(seed.gender());
            patient.setBirthDate(LocalDate.now(ZoneOffset.UTC).minusYears(seed.age()).withMonth(1).withDayOfMonth(1));
            patient.setPatientType("OUTPATIENT");
            patient.setStatus("ACTIVE");
            patient.setSourceChannel("HOSPITAL");
            patient.setCreatedAt(now());
            patient.setUpdatedAt(now());
            return patientRepository.save(patient);
        });
    }

    private Encounter seedEncounter(long tenantId, long patientId, long doctorId, PatientSeed seed) {
        return encounterRepository.findByEncounterNo(tenantId, seed.encounterNo()).orElseGet(() -> {
            Encounter encounter = new Encounter();
            encounter.setTenantId(tenantId);
            encounter.setPatientId(patientId);
            encounter.setEncounterNo(seed.encounterNo());
            encounter.setEncounterType("OUTPATIENT");
            encounter.setDeptName("肝病医学科");
            encounter.setAttendingDoctorId(doctorId);
            encounter.setStartAt(seed.visitDate().atTime(8, 0).atOffset(ZoneOffset.UTC));
            encounter.setSourceSystem(DEV_SEED_SOURCE);
            return encounterRepository.save(encounter);
        });
    }

    private void seedClinicalDecision(long tenantId, long patientId, long encounterId, long doctorId, PatientSeed seed) {
        boolean exists = clinicalHistoryEntryRepository.listByEncounterId(tenantId, encounterId).stream()
                .anyMatch(entry -> "CLINICAL_DECISION".equalsIgnoreCase(entry.getHistoryType()));
        if (exists) {
            return;
        }
        ObjectNode decision = objectMapper.createObjectNode();
        decision.put("diagnosis", seed.disease());
        decision.put("riskLevel", seed.riskLevel());
        decision.put("compliance", seed.compliance());
        decision.put("treatmentPlan", defaultTreatmentPlan(seed.disease()));
        saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, "CLINICAL_DECISION", decision, seed.visitDate());
    }

    private void seedLabs(long tenantId, long patientId, long encounterId, PatientSeed seed) {
        List<LabResult> existingLabs = labResultRepository.listByEncounterId(tenantId, encounterId);
        for (LabSeed lab : labsFor(seed)) {
            if (existingLabs.stream().anyMatch(existing -> lab.code().equalsIgnoreCase(existing.getIndicatorCode()))) {
                continue;
            }
            ensureIndicatorDict(lab);
            LabResult result = new LabResult();
            result.setTenantId(tenantId);
            result.setPatientId(patientId);
            result.setEncounterId(encounterId);
            result.setIndicatorCode(lab.code());
            result.setValueNumeric(lab.value());
            result.setValueText(String.valueOf(lab.value()));
            result.setUnit(lab.unit());
            result.setReferenceLow(lab.referenceLow());
            result.setReferenceHigh(lab.referenceHigh());
            result.setAbnormalFlag(lab.abnormalFlag());
            result.setSourceType(DEV_SEED_SOURCE);
            ObjectNode rawData = objectMapper.createObjectNode();
            rawData.put("devSeed", true);
            rawData.put("patientNo", seed.patientNo());
            result.setRawData(rawData);
            result.setCollectedAt(seed.visitDate().atTime(8, 15).atOffset(ZoneOffset.UTC));
            labResultRepository.save(result);
        }
    }

    private void ensureIndicatorDict(LabSeed lab) {
        if (indicatorDictRepository.findByCode(lab.code()).isPresent()) {
            return;
        }
        IndicatorDict dict = new IndicatorDict();
        dict.setCode(lab.code());
        dict.setIndicatorName(lab.name());
        dict.setCategory("IMLD_DEV_SEED");
        dict.setDataType("NUMERIC");
        dict.setDefaultUnit(lab.unit());
        dict.setNormalLow(lab.referenceLow() == null ? null : BigDecimal.valueOf(lab.referenceLow()));
        dict.setNormalHigh(lab.referenceHigh() == null ? null : BigDecimal.valueOf(lab.referenceHigh()));
        dict.setStatus("ACTIVE");
        dict.setCreatedAt(now());
        indicatorDictRepository.save(dict);
    }

    private void seedPathology(long tenantId, long patientId, long encounterId, long doctorId, PatientSeed seed) {
        if (!List.of("P001", "P002", "P003").contains(seed.patientNo())) {
            return;
        }
        boolean exists = clinicalHistoryEntryRepository.listByEncounterId(tenantId, encounterId).stream()
                .anyMatch(entry -> "PATHOLOGY".equalsIgnoreCase(entry.getHistoryType()));
        if (exists) {
            return;
        }
        ObjectNode pathology = objectMapper.createObjectNode();
        pathology.put("reportText", pathologyText(seed));
        pathology.put("nasScore", "P003".equals(seed.patientNo()) ? 5 : 3);
        pathology.put("reportedAt", seed.visitDate().toString());
        pathology.put("sourceType", DEV_SEED_SOURCE);
        saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, "PATHOLOGY", pathology, seed.visitDate());
    }

    private void saveClinicalHistoryEntry(long tenantId, long patientId, long encounterId, long doctorId,
                                          String historyType, JsonNode content, LocalDate recordedDate) {
        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenantId);
        entry.setPatientId(patientId);
        entry.setEncounterId(encounterId);
        entry.setHistoryType(historyType);
        entry.setContentJson(content);
        entry.setSourceType(DEV_SEED_SOURCE);
        entry.setRecordedBy(doctorId);
        entry.setRecordedAt(recordedDate.atTime(8, 20).atOffset(ZoneOffset.UTC));
        clinicalHistoryEntryRepository.save(entry);
    }

    private void seedGenetics(long tenantId, long patientId, long encounterId, PatientSeed seed) {
        GeneticSpec spec = geneticSpec(seed);
        if (spec == null || !geneticReportRepository.listByEncounterId(tenantId, encounterId).isEmpty()) {
            return;
        }
        GeneticReport report = new GeneticReport();
        report.setTenantId(tenantId);
        report.setPatientId(patientId);
        report.setEncounterId(encounterId);
        report.setReportSource("Dev seed genetic lab");
        report.setTestMethod("PANEL");
        report.setReportDate(seed.visitDate());
        report.setParseStatus("PARSED");
        report.setSummary(spec.summary());
        report.setConclusion(spec.gene());
        geneticReportRepository.save(report);

        GeneticVariant variant = new GeneticVariant();
        variant.setTenantId(tenantId);
        variant.setReportId(report.getId());
        variant.setGene(spec.gene());
        variant.setHgvsC(spec.hgvsC());
        variant.setHgvsP(spec.hgvsP());
        variant.setVariantType("SNV");
        variant.setZygosity("HETEROZYGOUS");
        variant.setClassification("pathogenic");
        variant.setEvidence(spec.summary());
        variant.setSourceType(DEV_SEED_SOURCE);
        geneticVariantRepository.save(variant);
    }

    private void seedDiagnosisData(long tenantId, long doctorId, long modelRegistryId) {
        for (DiagnosisSeed seed : DIAGNOSIS_SEEDS) {
            if (findDiagnosisSessionBySeedKey(tenantId, seed.seedKey()).isPresent()) {
                continue;
            }
            Patient patient = patientRepository.findByPatientNo(tenantId, seed.patientNo()).orElse(null);
            if (patient == null) {
                continue;
            }
            Encounter encounter = encounterRepository.findByEncounterNo(tenantId, seed.encounterNo()).orElse(null);
            OffsetDateTime startedAt = patientSeed(seed.patientNo()).visitDate().atTime(8, 0).atOffset(ZoneOffset.UTC);
            DiagnosisSession session = new DiagnosisSession();
            session.setTenantId(tenantId);
            session.setPatientId(patient.getId());
            session.setEncounterId(encounter == null ? null : encounter.getId());
            session.setDoctorId(doctorId);
            session.setTriggeredBy("MANUAL");
            session.setModelRegistryId(modelRegistryId);
            session.setInputSnapshot(buildInputSnapshot(seed));
            session.setStatus(seed.status());
            session.setStartedAt(startedAt);
            session.setCompletedAt(startedAt.plusMinutes(30));
            diagnosisSessionRepository.save(session);

            DiagnosisResult result = seedDiagnosisResult(tenantId, session.getId(), seed);
            seedDiagnosisRecommendations(tenantId, session.getId(), seed);
            if (seed.feedbackAction() != null) {
                seedDoctorFeedback(tenantId, session.getId(), result.getId(), doctorId, seed);
            }
        }
    }

    private Optional<DiagnosisSession> findDiagnosisSessionBySeedKey(long tenantId, String seedKey) {
        return diagnosisSessionRepository.listByTenantId(tenantId).stream()
                .filter(session -> session.getInputSnapshot() != null
                        && session.getInputSnapshot().has("devSeedKey")
                        && seedKey.equals(session.getInputSnapshot().get("devSeedKey").asText()))
                .findFirst();
    }

    private JsonNode buildInputSnapshot(DiagnosisSeed seed) {
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.put("devSeedKey", seed.seedKey());
        ObjectNode source = snapshot.putObject("source_snapshot");
        source.put("patientNo", seed.patientNo());
        source.put("encounterNo", seed.encounterNo());
        source.put("biochemical", seed.biochemical());
        source.put("clinical", seed.clinical());
        ObjectNode inferenceInput = snapshot.putObject("inference_input");
        inferenceInput.put("patientId", seed.patientNo());
        inferenceInput.put("riskProbability", seed.confidence());
        return snapshot;
    }

    private DiagnosisResult seedDiagnosisResult(long tenantId, long sessionId, DiagnosisSeed seed) {
        DiagnosisResult result = new DiagnosisResult();
        result.setTenantId(tenantId);
        result.setSessionId(sessionId);
        result.setDiseaseCode(seed.diseaseCode());
        result.setDiseaseName(seed.diseaseName());
        result.setConfidence(seed.confidence());
        result.setRankNo(1);
        result.setRiskLevel(seed.riskLevel());
        result.setEvidenceJson(buildEvidence(seed));
        result.setIsDisplayToPatient(Boolean.FALSE);
        return diagnosisResultRepository.save(result);
    }

    private JsonNode buildEvidence(DiagnosisSeed seed) {
        ObjectNode evidence = objectMapper.createObjectNode();
        ObjectNode inference = evidence.putObject("inference");
        inference.put("risk_probability", seed.confidence());
        inference.putArray("suggestions").add(defaultTreatmentPlan(seed.diseaseName())).add(defaultGeneticSuggestion(seed.diseaseName()));
        ObjectNode clinical = inference.putArray("clinical_abnormalities").addObject();
        clinical.put("feature", seed.biochemical());
        clinical.put("value", 1D);
        clinical.putArray("normal_range").add(0D).add(1D);
        clinical.put("direction", "high");
        clinical.put("severity", "高");
        inference.putArray("gene_abnormalities").addObject().put("gene", geneForDisease(seed.diseaseName()));
        evidence.put("generated_at", now().toString());
        evidence.put("dev_seed", true);
        return evidence;
    }

    private void seedDiagnosisRecommendations(long tenantId, long sessionId, DiagnosisSeed seed) {
        saveRecommendation(tenantId, sessionId, "DIET", defaultTreatmentPlan(seed.diseaseName()), 10);
        saveRecommendation(tenantId, sessionId, "GENETIC", defaultGeneticSuggestion(seed.diseaseName()), 20);
    }

    private void saveRecommendation(long tenantId, long sessionId, String type, String content, int priority) {
        DiagnosisRecommendation rec = new DiagnosisRecommendation();
        rec.setTenantId(tenantId);
        rec.setSessionId(sessionId);
        rec.setRecType(type);
        rec.setContent(content);
        rec.setPriority(priority);
        rec.setReason("DEV_SEED");
        diagnosisRecommendationRepository.save(rec);
    }

    private void seedDoctorFeedback(long tenantId, long sessionId, long resultId, long doctorId, DiagnosisSeed seed) {
        DoctorFeedback feedback = new DoctorFeedback();
        feedback.setTenantId(tenantId);
        feedback.setSessionId(sessionId);
        feedback.setResultId(resultId);
        feedback.setDoctorId(doctorId);
        feedback.setAction(seed.feedbackAction());
        ObjectNode modified = objectMapper.createObjectNode();
        modified.put("expertConclusion", seed.expertConclusion());
        modified.put("treatmentPlan", seed.treatmentPlan());
        feedback.setModifiedValue(modified);
        doctorFeedbackRepository.save(feedback);
    }

    private PatientSeed patientSeed(String patientNo) {
        return PATIENT_SEEDS.stream()
                .filter(seed -> seed.patientNo().equals(patientNo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing seed patient: " + patientNo));
    }

    private List<LabSeed> labsFor(PatientSeed seed) {
        if (seed.disease().contains("血色")) {
            return List.of(
                    new LabSeed("FERRITIN", "血清铁蛋白", 850D, "ng/mL", 30D, 300D, "HIGH"),
                    new LabSeed("TRANSFERRIN_SAT", "转铁蛋白饱和度", 65D, "%", 20D, 45D, "HIGH"),
                    new LabSeed("ALT", "谷丙转氨酶", 92D, "U/L", 9D, 50D, "HIGH"));
        }
        if (seed.disease().contains("抗胰蛋白酶")) {
            return List.of(
                    new LabSeed("AAT", "α1-抗胰蛋白酶", 0.45D, "g/L", 0.9D, 2.0D, "LOW"),
                    new LabSeed("ALT", "谷丙转氨酶", 78D, "U/L", 9D, 50D, "HIGH"),
                    new LabSeed("AST", "谷草转氨酶", 64D, "U/L", 15D, 40D, "HIGH"));
        }
        return List.of(
                new LabSeed("CERULOPLASMIN", "铜蓝蛋白", 80D, "mg/L", 200D, 600D, "LOW"),
                new LabSeed("URINE_COPPER_24H", "24h尿铜", 215D, "μg/24h", null, 100D, "HIGH"),
                new LabSeed("ALT", "谷丙转氨酶", 125D, "U/L", 9D, 50D, "HIGH"));
    }

    private GeneticSpec geneticSpec(PatientSeed seed) {
        if (seed.disease().contains("Wilson") || seed.disease().contains("肝豆")) {
            return new GeneticSpec("ATP7B", "c.2333G>T", "p.Arg778Leu", "ATP7B 致病变异，支持 Wilson 病诊断。");
        }
        if (seed.disease().contains("血色")) {
            return new GeneticSpec("HFE", "c.845G>A", "p.Cys282Tyr", "HFE 致病变异，支持遗传性血色病风险评估。");
        }
        if (seed.disease().contains("抗胰蛋白酶")) {
            return new GeneticSpec("SERPINA1", "c.1096G>A", "p.Glu366Lys", "SERPINA1 Pi*Z 相关变异，提示 AAT 缺乏风险。");
        }
        return null;
    }

    private String pathologyText(PatientSeed seed) {
        if (seed.disease().contains("血色")) {
            return "肝细胞内铁沉积增多，汇管区轻度纤维化。";
        }
        if (seed.disease().contains("抗胰蛋白酶")) {
            return "PAS-D 阳性包涵体可见，伴慢性肝损伤表现。";
        }
        return "肝细胞变性及轻度炎症，结合铜代谢异常建议进一步遗传学确认。";
    }

    private String geneForDisease(String diseaseName) {
        if (diseaseName.contains("血色")) {
            return "HFE";
        }
        if (diseaseName.contains("抗胰蛋白酶")) {
            return "SERPINA1";
        }
        return "ATP7B";
    }

    private String defaultTreatmentPlan(String diseaseName) {
        if (diseaseName.contains("血色")) {
            return "建议严格限制红肉和动物内脏，避免随餐维生素C补充，餐后可饮茶抑制铁吸收。";
        }
        if (diseaseName.contains("抗胰蛋白酶")) {
            return "建议高蛋白、低脂饮食，减少酒精摄入，配合呼吸系统评估。";
        }
        return "建议立即启动低铜饮食，禁食坚果、巧克力和动物内脏。";
    }

    private String defaultGeneticSuggestion(String diseaseName) {
        if (diseaseName.contains("血色")) {
            return "建议进行 HFE 基因检测，并对一级亲属开展家系筛查。";
        }
        if (diseaseName.contains("抗胰蛋白酶")) {
            return "建议进行 SERPINA1 基因分型，并评估肝肺联合受累风险。";
        }
        return "建议 ATP7B 靶向测序，并开展一级亲属筛查。";
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private record PatientSeed(String patientNo, String name, String gender, int age, String riskLevel,
                               String disease, String compliance, String encounterNo, LocalDate visitDate) {
    }

    private record LabSeed(String code, String name, Double value, String unit, Double referenceLow,
                           Double referenceHigh, String abnormalFlag) {
    }

    private record GeneticSpec(String gene, String hgvsC, String hgvsP, String summary) {
    }

    private record DiagnosisSeed(String seedKey, String patientNo, String encounterNo, String status, String feedbackAction,
                                 String diseaseCode, String diseaseName, Double confidence, String riskLevel,
                                 String biochemical, String clinical, String expertConclusion, String treatmentPlan) {
    }
}
