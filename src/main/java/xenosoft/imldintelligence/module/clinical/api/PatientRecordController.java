package xenosoft.imldintelligence.module.clinical.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.common.CheckPermission;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;
import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorDict;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticVariantRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.ImagingReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorDictRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;
import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.service.PatientService;
import xenosoft.imldintelligence.module.clinical.api.dto.PatientRecordApiDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequiredArgsConstructor
public class PatientRecordController implements PatientRecordControllerContract {
    private static final String HISTORY_TYPE_CHIEF_COMPLAINT = "CHIEF_COMPLAINT";
    private static final String HISTORY_TYPE_PRESENT_ILLNESS = "PRESENT_ILLNESS";
    private static final String HISTORY_TYPE_HISTORY = "PATIENT_HISTORY";
    private static final String HISTORY_TYPE_PHYSICAL_EXAM = "PHYSICAL_EXAM";
    private static final String HISTORY_TYPE_PATHOLOGY = "PATHOLOGY";
    private static final String HISTORY_TYPE_CLINICAL_DECISION = "CLINICAL_DECISION";

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final UserAccountRepository userAccountRepository;
    private final IndicatorDictRepository indicatorDictRepository;
    private final LabResultRepository labResultRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final GeneticReportRepository geneticReportRepository;
    private final GeneticVariantRepository geneticVariantRepository;
    private final ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CheckPermission(resource = "PATIENT", action = "CREATE")
    public ApiResponse<PatientRecordApiDtos.Response.PatientRecordCreateResponse> createPatientRecord(
            Long tenantId,
            PatientRecordApiDtos.Request.CreatePatientRecordRequest request) {
        validateRequest(request);

        Patient patient = patientRepository.findByPatientNo(tenantId, request.patientNo())
                .orElseGet(() -> createPatient(tenantId, request));
        Long doctorId = resolveDoctorId(tenantId);
        Encounter encounter = resolveEncounter(tenantId, patient, request, doctorId);

        saveNarrativeEntries(tenantId, patient.getId(), encounter.getId(), doctorId, request);
        saveLabResults(tenantId, patient.getId(), encounter.getId(), request.laboratoryScreening(), sourceTypeOf(request.importMeta()), request.visitDate());
        saveImagingReports(tenantId, patient.getId(), encounter.getId(), request.imagingReports());
        savePathology(tenantId, patient.getId(), encounter.getId(), doctorId, request.pathology());
        saveGeneticSequencing(tenantId, patient.getId(), encounter.getId(), request.geneticSequencing());

        String recordId = "REC-" + System.currentTimeMillis();
        String visitId = encounter.getEncounterNo();
        return ApiResponse.success(new PatientRecordApiDtos.Response.PatientRecordCreateResponse(recordId, visitId, "病历归档成功"));
    }

    private void validateRequest(PatientRecordApiDtos.Request.CreatePatientRecordRequest request) {
        if (request.clinicalDecision() == null || isBlank(request.clinicalDecision().diagnosis())) {
            throw new ResponseStatusException(BAD_REQUEST, "clinicalDecision.diagnosis is required");
        }
        if (request.pathology() != null && Boolean.TRUE.equals(request.pathology().performed()) && isBlank(request.pathology().reportText())) {
            throw new ResponseStatusException(BAD_REQUEST, "pathology.reportText is required when pathology.performed=true");
        }
        if (request.geneticSequencing() != null && Boolean.TRUE.equals(request.geneticSequencing().tested()) && isBlank(request.geneticSequencing().method())) {
            throw new ResponseStatusException(BAD_REQUEST, "geneticSequencing.method is required when geneticSequencing.tested=true");
        }
    }

    private Patient createPatient(Long tenantId, PatientRecordApiDtos.Request.CreatePatientRecordRequest request) {
        IdentityApiDtos.Request.CreatePatientRequest createPatientRequest =
                new IdentityApiDtos.Request.CreatePatientRequest(
                        request.patientNo(),
                        request.name(),
                        request.gender(),
                        null,
                        request.idCard(),
                        request.phone(),
                        normalizeEncounterType(request.encounterType()),
                        normalizeSourceChannel(request.importMeta())
                );
        return patientService.createPatient(tenantId, createPatientRequest);
    }

    private Encounter resolveEncounter(
            Long tenantId,
            Patient patient,
            PatientRecordApiDtos.Request.CreatePatientRecordRequest request,
            Long doctorId) {
        String encounterNo = isBlank(request.visitId()) ? "VISIT-" + System.currentTimeMillis() : request.visitId().trim();
        Optional<Encounter> existing = encounterRepository.findByEncounterNo(tenantId, encounterNo);
        if (existing.isPresent()) {
            return existing.get();
        }

        Encounter encounter = new Encounter();
        encounter.setTenantId(tenantId);
        encounter.setPatientId(patient.getId());
        encounter.setEncounterNo(encounterNo);
        encounter.setEncounterType(normalizeEncounterType(request.encounterType()));
        encounter.setDeptName(trimToNull(request.department()));
        encounter.setAttendingDoctorId(doctorId);
        encounter.setStartAt(atStartOfDay(request.visitDate()));
        encounter.setSourceSystem(normalizeEncounterSourceSystem(request.importMeta()));
        encounterRepository.save(encounter);
        return encounter;
    }

    private void saveNarrativeEntries(
            Long tenantId,
            Long patientId,
            Long encounterId,
            Long doctorId,
            PatientRecordApiDtos.Request.CreatePatientRecordRequest request) {
        saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, HISTORY_TYPE_CHIEF_COMPLAINT, textNode("text", request.chiefComplaint()), sourceTypeOf(request.importMeta()), atStartOfDay(request.visitDate()));
        saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, HISTORY_TYPE_PRESENT_ILLNESS, textNode("text", request.presentIllness()), sourceTypeOf(request.importMeta()), atStartOfDay(request.visitDate()));
        if (request.history() != null && !request.history().isNull()) {
            saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, HISTORY_TYPE_HISTORY, request.history(), sourceTypeOf(request.importMeta()), atStartOfDay(request.visitDate()));
        }
        if (request.physicalExam() != null && !request.physicalExam().isNull()) {
            saveClinicalHistoryEntry(tenantId, patientId, encounterId, doctorId, HISTORY_TYPE_PHYSICAL_EXAM, request.physicalExam(), sourceTypeOf(request.importMeta()), atStartOfDay(request.visitDate()));
        }
        saveClinicalHistoryEntry(
                tenantId,
                patientId,
                encounterId,
                doctorId,
                HISTORY_TYPE_CLINICAL_DECISION,
                objectMapper.valueToTree(request.clinicalDecision()),
                sourceTypeOf(request.importMeta()),
                atStartOfDay(request.visitDate())
        );
    }

    private void saveLabResults(
            Long tenantId,
            Long patientId,
            Long encounterId,
            JsonNode laboratoryScreening,
            String sourceType,
            LocalDate visitDate) {
        if (laboratoryScreening == null || laboratoryScreening.isNull()) {
            return;
        }
        persistLabResultsRecursive(tenantId, patientId, encounterId, laboratoryScreening, sourceType, visitDate, "");
    }

    private void persistLabResultsRecursive(
            Long tenantId,
            Long patientId,
            Long encounterId,
            JsonNode node,
            String sourceType,
            LocalDate visitDate,
            String path) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String nextPath = path.isEmpty() ? field.getKey() : path + "." + field.getKey();
                persistLabResultsRecursive(tenantId, patientId, encounterId, field.getValue(), sourceType, visitDate, nextPath);
            }
            return;
        }
        if (node.isArray()) {
            return;
        }

        String valueText = node.asText();
        if (valueText == null || valueText.isBlank()) {
            return;
        }
        String indicatorCode = resolveIndicatorCode(path);
        ensureIndicatorDict(indicatorCode);

        LabResult labResult = new LabResult();
        labResult.setTenantId(tenantId);
        labResult.setPatientId(patientId);
        labResult.setEncounterId(encounterId);
        labResult.setIndicatorCode(indicatorCode);
        labResult.setValueNumeric(parseDouble(valueText));
        labResult.setValueText(valueText.trim());
        labResult.setSourceType(sourceType);
        labResult.setCollectedAt(atStartOfDay(visitDate));
        labResultRepository.save(labResult);
    }

    private void saveImagingReports(
            Long tenantId,
            Long patientId,
            Long encounterId,
            List<PatientRecordApiDtos.Request.ImagingReportItem> imagingReports) {
        List<PatientRecordApiDtos.Request.ImagingReportItem> reports = imagingReports == null ? List.of() : imagingReports;
        for (PatientRecordApiDtos.Request.ImagingReportItem report : reports) {
            if (isBlank(report.reportText())) {
                continue;
            }
            ImagingReport imagingReport = new ImagingReport();
            imagingReport.setTenantId(tenantId);
            imagingReport.setPatientId(patientId);
            imagingReport.setEncounterId(encounterId);
            imagingReport.setModality(normalizeImagingModality(report.modality()));
            imagingReport.setReportText(report.reportText().trim());
            imagingReport.setFileId(parseLong(report.fileId()));
            imagingReport.setSourceSystem(normalizeImagingSourceSystem(report.sourceType()));
            imagingReport.setExaminedAt(atStartOfDay(report.examinedAt()));
            imagingReportRepository.save(imagingReport);
        }
    }

    private void savePathology(
            Long tenantId,
            Long patientId,
            Long encounterId,
            Long doctorId,
            PatientRecordApiDtos.Request.PathologyRecord pathology) {
        if (pathology == null || !Boolean.TRUE.equals(pathology.performed()) || isBlank(pathology.reportText())) {
            return;
        }
        ObjectNode content = objectMapper.createObjectNode();
        content.put("reportText", pathology.reportText().trim());
        if (pathology.nasScore() != null) {
            content.put("nasScore", pathology.nasScore());
        } else {
            content.putNull("nasScore");
        }
        if (parseLong(pathology.fileId()) != null) {
            content.put("fileId", parseLong(pathology.fileId()));
        } else {
            content.putNull("fileId");
        }
        content.put("sourceType", normalizeClinicalSourceType(pathology.sourceType()));
        if (pathology.reportedAt() != null) {
            content.put("reportedAt", atStartOfDay(pathology.reportedAt()).toString());
        }
        saveClinicalHistoryEntry(
                tenantId,
                patientId,
                encounterId,
                doctorId,
                HISTORY_TYPE_PATHOLOGY,
                content,
                normalizeClinicalSourceType(pathology.sourceType()),
                atStartOfDay(pathology.reportedAt())
        );
    }

    private void saveGeneticSequencing(
            Long tenantId,
            Long patientId,
            Long encounterId,
            PatientRecordApiDtos.Request.GeneticSequencingRecord geneticSequencing) {
        if (geneticSequencing == null || !Boolean.TRUE.equals(geneticSequencing.tested())) {
            return;
        }
        GeneticReport report = new GeneticReport();
        report.setTenantId(tenantId);
        report.setPatientId(patientId);
        report.setEncounterId(encounterId);
        report.setReportSource(trimToNull(geneticSequencing.reportSource()));
        report.setTestMethod(trimToNull(geneticSequencing.method()));
        report.setReportDate(geneticSequencing.reportDate());
        report.setFileId(parseLong(geneticSequencing.fileId()));
        report.setParseStatus(determineGeneticParseStatus(geneticSequencing));
        report.setSummary(trimToNull(geneticSequencing.summary()));
        report.setConclusion(trimToNull(geneticSequencing.conclusion()));
        geneticReportRepository.save(report);

        List<PatientRecordApiDtos.Request.GeneticVariantItem> variants = geneticSequencing.variants() == null
                ? List.of()
                : geneticSequencing.variants();
        for (PatientRecordApiDtos.Request.GeneticVariantItem item : variants) {
            if (isBlank(item.gene())) {
                continue;
            }
            GeneticVariant variant = new GeneticVariant();
            variant.setTenantId(tenantId);
            variant.setReportId(report.getId());
            variant.setGene(item.gene().trim());
            variant.setHgvsC(trimToNull(item.hgvsC()));
            variant.setHgvsP(trimToNull(item.hgvsP()));
            variant.setVariantType(trimToNull(item.variantType()));
            variant.setZygosity(trimToNull(item.zygosity()));
            variant.setClassification(trimToNull(item.classification()));
            variant.setEvidence(trimToNull(item.evidence()));
            variant.setSourceType(normalizeClinicalSourceType(geneticSequencing.sourceType()));
            geneticVariantRepository.save(variant);
        }
    }

    private void saveClinicalHistoryEntry(
            Long tenantId,
            Long patientId,
            Long encounterId,
            Long doctorId,
            String historyType,
            JsonNode contentJson,
            String sourceType,
            OffsetDateTime recordedAt) {
        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenantId);
        entry.setPatientId(patientId);
        entry.setEncounterId(encounterId);
        entry.setHistoryType(historyType);
        entry.setTemplateCode(null);
        entry.setContentJson(contentJson);
        entry.setSourceType(sourceType);
        entry.setRecordedBy(doctorId);
        entry.setRecordedAt(recordedAt == null ? now() : recordedAt);
        clinicalHistoryEntryRepository.save(entry);
    }

    private ObjectNode textNode(String field, String value) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put(field, value == null ? "" : value.trim());
        return node;
    }

    private void ensureIndicatorDict(String indicatorCode) {
        if (indicatorDictRepository.findByCode(indicatorCode).isPresent()) {
            return;
        }
        IndicatorDict indicatorDict = new IndicatorDict();
        indicatorDict.setCode(indicatorCode);
        indicatorDict.setIndicatorName(indicatorCode);
        indicatorDict.setCategory("IMLD_RECORD");
        indicatorDict.setDataType("NUMERIC");
        indicatorDict.setDefaultUnit(null);
        indicatorDict.setNormalLow((BigDecimal) null);
        indicatorDict.setNormalHigh((BigDecimal) null);
        indicatorDict.setStatus("ACTIVE");
        indicatorDictRepository.save(indicatorDict);
    }

    private String resolveIndicatorCode(String path) {
        String key = path.substring(path.lastIndexOf('.') + 1);
        return switch (key) {
            case "alt" -> "ALT";
            case "ast" -> "AST";
            case "tbil" -> "TBIL";
            case "dbil" -> "DBIL";
            case "ibil" -> "IBIL";
            case "cer" -> "CERULOPLASMIN";
            case "aat" -> "AAT";
            case "afp" -> "AFP";
            case "cea" -> "CEA";
            case "ca19_9" -> "CA19_9";
            case "crp" -> "CRP";
            case "pct" -> "PCT";
            default -> sanitizeIndicatorCode(key);
        };
    }

    private String sanitizeIndicatorCode(String key) {
        return key.replaceAll("([a-z])([A-Z])", "$1_$2").replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private String determineGeneticParseStatus(PatientRecordApiDtos.Request.GeneticSequencingRecord record) {
        boolean hasVariants = record.variants() != null && !record.variants().isEmpty();
        boolean hasNarrative = !isBlank(record.summary()) || !isBlank(record.conclusion());
        return hasVariants || hasNarrative ? "PARSED" : "PENDING";
    }

    private Long resolveDoctorId(Long tenantId) {
        return userAccountRepository.listByTenantId(tenantId).stream()
                .filter(user -> "ACTIVE".equalsIgnoreCase(user.getStatus()))
                .findFirst()
                .map(UserAccount::getId)
                .orElse(null);
    }

    private String normalizeEncounterType(String encounterType) {
        String normalized = trimToNull(encounterType);
        if (normalized == null) {
            return "OUTPATIENT";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeSourceChannel(JsonNode importMeta) {
        String sourceType = sourceTypeOf(importMeta);
        return switch (sourceType) {
            case "HIS_LIS" -> "HIS_LIS";
            case "IMAGE_OCR", "PDF_OCR" -> "OCR";
            default -> "HOSPITAL";
        };
    }

    private String normalizeEncounterSourceSystem(JsonNode importMeta) {
        String sourceType = sourceTypeOf(importMeta);
        return switch (sourceType) {
            case "HIS_LIS" -> "HIS";
            case "IMAGE_OCR" -> "IMAGE_OCR";
            case "PDF_OCR" -> "PDF_OCR";
            default -> "MANUAL";
        };
    }

    private String normalizeClinicalSourceType(String sourceType) {
        String normalized = trimToNull(sourceType);
        return normalized == null ? "MANUAL" : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalizeImagingModality(String modality) {
        String normalized = trimToNull(modality);
        if (normalized == null) {
            return "OTHER";
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "ULTRASOUND", "US" -> "ULTRASOUND";
            case "CT" -> "CT";
            case "MRI" -> "MRI";
            default -> "OTHER";
        };
    }

    private String normalizeImagingSourceSystem(String sourceType) {
        String normalized = normalizeClinicalSourceType(sourceType);
        return "PACS_IMPORT".equals(normalized) ? "PACS" : normalized;
    }

    private String sourceTypeOf(JsonNode importMeta) {
        if (importMeta == null || importMeta.isNull() || importMeta.get("sourceType") == null) {
            return "MANUAL";
        }
        String sourceType = importMeta.get("sourceType").asText();
        return sourceType == null || sourceType.isBlank() ? "MANUAL" : sourceType.trim().toUpperCase(Locale.ROOT);
    }

    private OffsetDateTime atStartOfDay(LocalDate date) {
        LocalDate value = date == null ? LocalDate.now(ZoneOffset.UTC) : date;
        return value.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? null : Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
