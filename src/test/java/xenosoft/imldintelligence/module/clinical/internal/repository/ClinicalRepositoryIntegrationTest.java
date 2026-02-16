package xenosoft.imldintelligence.module.clinical.internal.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.transaction.annotation.Transactional;
import xenosoft.imldintelligence.AbstractPostgresIntegrationTest;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;
import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisabledInAotMode
class ClinicalRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private IndicatorDictRepository indicatorDictRepository;

    @Autowired
    private IndicatorMappingRepository indicatorMappingRepository;

    @Autowired
    private LabResultRepository labResultRepository;

    @Autowired
    private ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;

    @Autowired
    private GeneticReportRepository geneticReportRepository;

    @Autowired
    private GeneticVariantRepository geneticVariantRepository;

    @Autowired
    private ImagingReportRepository imagingReportRepository;

    @Test
    void indicatorDictCrud() {
        String code = "ALT_" + unique("code");
        IndicatorDict indicatorDict = new IndicatorDict();
        indicatorDict.setCode(code);
        indicatorDict.setIndicatorName("ALT");
        indicatorDict.setCategory("LIVER");
        indicatorDict.setDataType("NUMERIC");
        indicatorDict.setDefaultUnit("U/L");
        indicatorDict.setLoincCode("1742-6");
        indicatorDict.setNormalLow(new BigDecimal("9.000000"));
        indicatorDict.setNormalHigh(new BigDecimal("50.000000"));
        indicatorDict.setStatus("ACTIVE");
        indicatorDictRepository.save(indicatorDict);

        assertThat(indicatorDictRepository.findByCode(code)).isPresent();
        assertThat(indicatorDictRepository.listAll()).extracting(IndicatorDict::getCode).contains(code);
        assertThat(indicatorDictRepository.listByStatus("ACTIVE")).extracting(IndicatorDict::getCode).contains(code);

        indicatorDict.setIndicatorName("ALT Updated");
        indicatorDictRepository.update(indicatorDict);
        assertThat(indicatorDictRepository.findByCode(code)).get().extracting(IndicatorDict::getIndicatorName).isEqualTo("ALT Updated");

        assertThat(indicatorDictRepository.deleteByCode(code)).isTrue();
        assertThat(indicatorDictRepository.findByCode(code)).get().extracting(IndicatorDict::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void indicatorMappingCrudAndTenantIsolation() {
        createIndicatorDict("AST");
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        IndicatorMapping mapping = new IndicatorMapping();
        mapping.setTenantId(tenantA.getId());
        mapping.setSourceSystem("LIS");
        mapping.setSourceCode("AST_RAW_" + unique("code"));
        mapping.setSourceName("AST");
        mapping.setTargetIndicatorCode("AST");
        mapping.setUnitConversionExpr("x");
        mapping.setQualityRule(OBJECT_MAPPER.createObjectNode().put("range", "normal"));
        mapping.setStatus("ACTIVE");
        indicatorMappingRepository.save(mapping);

        assertThat(mapping.getId()).isNotNull();
        assertThat(indicatorMappingRepository.findById(tenantA.getId(), mapping.getId())).isPresent();
        assertThat(indicatorMappingRepository.findById(tenantB.getId(), mapping.getId())).isEmpty();
        assertThat(indicatorMappingRepository.findBySourceSystemAndSourceCode(tenantA.getId(), mapping.getSourceSystem(), mapping.getSourceCode())).isPresent();
        assertThat(indicatorMappingRepository.listByTenantId(tenantA.getId())).extracting(IndicatorMapping::getId).contains(mapping.getId());
        assertThat(indicatorMappingRepository.listByTargetIndicatorCode(tenantA.getId(), "AST")).extracting(IndicatorMapping::getId).contains(mapping.getId());

        mapping.setSourceName("AST Updated");
        mapping.setQualityRule(OBJECT_MAPPER.createObjectNode().put("range", "updated"));
        indicatorMappingRepository.update(mapping);
        assertThat(indicatorMappingRepository.findById(tenantA.getId(), mapping.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getSourceName()).isEqualTo("AST Updated");
                    assertThat(value.getQualityRule().get("range").asText()).isEqualTo("updated");
                });

        assertThat(indicatorMappingRepository.deleteById(tenantA.getId(), mapping.getId())).isTrue();
        assertThat(indicatorMappingRepository.findById(tenantA.getId(), mapping.getId())).get().extracting(IndicatorMapping::getStatus).isEqualTo("INACTIVE");
    }

    @Test
    void labResultCrudWithJsonbAndTenantIsolation() {
        createIndicatorDict("TBIL");
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();
        Patient patient = createPatient(tenantA.getId());
        Encounter encounter = createEncounter(tenantA.getId(), patient.getId());

        LabResult labResult = new LabResult();
        labResult.setTenantId(tenantA.getId());
        labResult.setPatientId(patient.getId());
        labResult.setEncounterId(encounter.getId());
        labResult.setIndicatorCode("TBIL");
        labResult.setValueNumeric(22.5D);
        labResult.setValueText("22.5");
        labResult.setUnit("umol/L");
        labResult.setReferenceLow(3.4D);
        labResult.setReferenceHigh(17.1D);
        labResult.setAbnormalFlag("H");
        labResult.setSourceType("MANUAL");
        labResult.setRawData(OBJECT_MAPPER.createObjectNode().put("origin", "manual"));
        labResult.setCollectedAt(OffsetDateTime.now().withNano(0));
        labResultRepository.save(labResult);

        assertThat(labResult.getId()).isNotNull();
        assertThat(labResultRepository.findById(tenantA.getId(), labResult.getId())).isPresent();
        assertThat(labResultRepository.findById(tenantB.getId(), labResult.getId())).isEmpty();
        assertThat(labResultRepository.listByTenantId(tenantA.getId())).extracting(LabResult::getId).contains(labResult.getId());
        assertThat(labResultRepository.listByPatientId(tenantA.getId(), patient.getId())).extracting(LabResult::getId).contains(labResult.getId());
        assertThat(labResultRepository.listByEncounterId(tenantA.getId(), encounter.getId())).extracting(LabResult::getId).contains(labResult.getId());

        labResult.setRawData(OBJECT_MAPPER.createObjectNode().put("origin", "updated"));
        labResult.setValueText("23.1");
        labResultRepository.update(labResult);
        assertThat(labResultRepository.findById(tenantA.getId(), labResult.getId())).get()
                .satisfies(value -> {
                    assertThat(value.getValueText()).isEqualTo("23.1");
                    assertThat(value.getRawData().get("origin").asText()).isEqualTo("updated");
                });

        assertThat(labResultRepository.deleteById(tenantA.getId(), labResult.getId())).isTrue();
        assertThat(labResultRepository.findById(tenantA.getId(), labResult.getId())).isEmpty();
    }

    @Test
    void clinicalHistoryEntryCrudWithJsonb() {
        Tenant tenant = createTenant();
        UserAccount user = createUserAccount(tenant.getId());
        Patient patient = createPatient(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId());

        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenant.getId());
        entry.setPatientId(patient.getId());
        entry.setEncounterId(encounter.getId());
        entry.setHistoryType("PAST_HISTORY");
        entry.setTemplateCode("TEMP_" + unique("code"));
        entry.setContentJson(OBJECT_MAPPER.createObjectNode().put("text", "none"));
        entry.setSourceType("MANUAL");
        entry.setRecordedBy(user.getId());
        entry.setRecordedAt(OffsetDateTime.now().withNano(0));
        clinicalHistoryEntryRepository.save(entry);

        assertThat(entry.getId()).isNotNull();
        assertThat(clinicalHistoryEntryRepository.findById(tenant.getId(), entry.getId())).isPresent();
        assertThat(clinicalHistoryEntryRepository.listByTenantId(tenant.getId())).extracting(ClinicalHistoryEntry::getId).contains(entry.getId());
        assertThat(clinicalHistoryEntryRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(ClinicalHistoryEntry::getId).contains(entry.getId());
        assertThat(clinicalHistoryEntryRepository.listByEncounterId(tenant.getId(), encounter.getId())).extracting(ClinicalHistoryEntry::getId).contains(entry.getId());

        entry.setContentJson(OBJECT_MAPPER.createObjectNode().put("text", "updated"));
        clinicalHistoryEntryRepository.update(entry);
        assertThat(clinicalHistoryEntryRepository.findById(tenant.getId(), entry.getId())).get()
                .satisfies(value -> assertThat(value.getContentJson().get("text").asText()).isEqualTo("updated"));

        assertThat(clinicalHistoryEntryRepository.deleteById(tenant.getId(), entry.getId())).isTrue();
        assertThat(clinicalHistoryEntryRepository.findById(tenant.getId(), entry.getId())).isEmpty();
    }

    @Test
    void geneticReportAndVariantCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId());

        GeneticReport report = new GeneticReport();
        report.setTenantId(tenant.getId());
        report.setPatientId(patient.getId());
        report.setEncounterId(encounter.getId());
        report.setReportSource("LAB");
        report.setReportDate(java.time.LocalDate.now());
        report.setParseStatus("PENDING");
        report.setSummary("sum");
        report.setConclusion("UNCERTAIN");
        geneticReportRepository.save(report);

        assertThat(report.getId()).isNotNull();
        assertThat(geneticReportRepository.findById(tenant.getId(), report.getId())).isPresent();
        assertThat(geneticReportRepository.listByTenantId(tenant.getId())).extracting(GeneticReport::getId).contains(report.getId());
        assertThat(geneticReportRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(GeneticReport::getId).contains(report.getId());
        assertThat(geneticReportRepository.listByEncounterId(tenant.getId(), encounter.getId())).extracting(GeneticReport::getId).contains(report.getId());

        report.setParseStatus("PARSED");
        geneticReportRepository.update(report);
        assertThat(geneticReportRepository.findById(tenant.getId(), report.getId())).get().extracting(GeneticReport::getParseStatus).isEqualTo("PARSED");

        GeneticVariant variant = new GeneticVariant();
        variant.setTenantId(tenant.getId());
        variant.setReportId(report.getId());
        variant.setGene("ABCB11");
        variant.setChromosome("2");
        variant.setPosition(1234L);
        variant.setRefAllele("A");
        variant.setAltAllele("G");
        variant.setVariantType("SNV");
        variant.setZygosity("HET");
        variant.setClassification("VUS");
        variant.setHgvsC("c.123A>G");
        variant.setHgvsP("p.X");
        variant.setEvidence("e");
        variant.setSourceType("PARSED");
        geneticVariantRepository.save(variant);

        assertThat(variant.getId()).isNotNull();
        assertThat(geneticVariantRepository.findById(tenant.getId(), variant.getId())).isPresent();
        assertThat(geneticVariantRepository.listByTenantId(tenant.getId())).extracting(GeneticVariant::getId).contains(variant.getId());
        assertThat(geneticVariantRepository.listByReportId(tenant.getId(), report.getId())).extracting(GeneticVariant::getId).contains(variant.getId());

        variant.setClassification("PATHOGENIC");
        geneticVariantRepository.update(variant);
        assertThat(geneticVariantRepository.findById(tenant.getId(), variant.getId())).get().extracting(GeneticVariant::getClassification).isEqualTo("PATHOGENIC");

        assertThat(geneticVariantRepository.deleteById(tenant.getId(), variant.getId())).isTrue();
        assertThat(geneticVariantRepository.findById(tenant.getId(), variant.getId())).isEmpty();
        assertThat(geneticReportRepository.deleteById(tenant.getId(), report.getId())).isTrue();
        assertThat(geneticReportRepository.findById(tenant.getId(), report.getId())).isEmpty();
    }

    @Test
    void imagingReportCrud() {
        Tenant tenant = createTenant();
        Patient patient = createPatient(tenant.getId());
        Encounter encounter = createEncounter(tenant.getId(), patient.getId());

        ImagingReport imagingReport = new ImagingReport();
        imagingReport.setTenantId(tenant.getId());
        imagingReport.setPatientId(patient.getId());
        imagingReport.setEncounterId(encounter.getId());
        imagingReport.setModality("US");
        imagingReport.setReportText("normal");
        imagingReport.setSourceSystem("PACS");
        imagingReport.setExaminedAt(OffsetDateTime.now().withNano(0));
        imagingReportRepository.save(imagingReport);

        assertThat(imagingReport.getId()).isNotNull();
        assertThat(imagingReportRepository.findById(tenant.getId(), imagingReport.getId())).isPresent();
        assertThat(imagingReportRepository.listByTenantId(tenant.getId())).extracting(ImagingReport::getId).contains(imagingReport.getId());
        assertThat(imagingReportRepository.listByPatientId(tenant.getId(), patient.getId())).extracting(ImagingReport::getId).contains(imagingReport.getId());
        assertThat(imagingReportRepository.listByEncounterId(tenant.getId(), encounter.getId())).extracting(ImagingReport::getId).contains(imagingReport.getId());

        imagingReport.setReportText("updated");
        imagingReportRepository.update(imagingReport);
        assertThat(imagingReportRepository.findById(tenant.getId(), imagingReport.getId())).get().extracting(ImagingReport::getReportText).isEqualTo("updated");

        assertThat(imagingReportRepository.deleteById(tenant.getId(), imagingReport.getId())).isTrue();
        assertThat(imagingReportRepository.findById(tenant.getId(), imagingReport.getId())).isEmpty();
    }

    private void createIndicatorDict(String code) {
        IndicatorDict indicatorDict = new IndicatorDict();
        indicatorDict.setCode(code);
        indicatorDict.setIndicatorName(code);
        indicatorDict.setCategory("GENERAL");
        indicatorDict.setDataType("NUMERIC");
        indicatorDict.setStatus("ACTIVE");
        indicatorDictRepository.save(indicatorDict);
    }

    private Tenant createTenant() {
        String suffix = unique("tenant");
        Tenant tenant = new Tenant();
        tenant.setTenantCode("TEN_" + suffix);
        tenant.setTenantName("Tenant " + suffix);
        tenant.setDeployMode("SAAS");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        return tenant;
    }

    private UserAccount createUserAccount(Long tenantId) {
        String suffix = unique("user");
        UserAccount userAccount = new UserAccount();
        userAccount.setTenantId(tenantId);
        userAccount.setUserNo("UNO_" + suffix);
        userAccount.setUsername("username_" + suffix);
        userAccount.setPasswordHash("pwd_hash");
        userAccount.setDisplayName("Display Name");
        userAccount.setUserType("DOCTOR");
        userAccount.setDeptName("Dept");
        userAccount.setMobileEncrypted("13800001111");
        userAccount.setEmail(suffix + "@mail.com");
        userAccount.setStatus("ACTIVE");
        userAccount.setLastLoginAt(OffsetDateTime.now().withNano(0));
        userAccountRepository.save(userAccount);
        return userAccount;
    }

    private Patient createPatient(Long tenantId) {
        String suffix = unique("patient");
        Patient patient = new Patient();
        patient.setTenantId(tenantId);
        patient.setPatientNo("PAT_" + suffix);
        patient.setPatientName("Patient " + suffix);
        patient.setGender("MALE");
        patient.setPatientType("OUTPATIENT");
        patient.setStatus("ACTIVE");
        patient.setSourceChannel("HOSPITAL");
        patientRepository.save(patient);
        return patient;
    }

    private Encounter createEncounter(Long tenantId, Long patientId) {
        UserAccount userAccount = createUserAccount(tenantId);
        Encounter encounter = new Encounter();
        encounter.setTenantId(tenantId);
        encounter.setPatientId(patientId);
        encounter.setEncounterNo("ENC_" + unique("enc"));
        encounter.setEncounterType("OUTPATIENT");
        encounter.setDeptName("Dept");
        encounter.setAttendingDoctorId(userAccount.getId());
        encounter.setStartAt(OffsetDateTime.now().withNano(0));
        encounter.setSourceSystem("HIS");
        encounterRepository.save(encounter);
        return encounter;
    }

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}

