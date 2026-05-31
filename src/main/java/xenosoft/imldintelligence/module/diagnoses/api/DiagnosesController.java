package xenosoft.imldintelligence.module.diagnoses.api;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticVariantRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;
import xenosoft.imldintelligence.module.diagnoses.api.dto.DiagnosesApiDtos;
import xenosoft.imldintelligence.module.diagnoses.api.dto.ImldInferenceApiDtos;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.model.ModelRegistry;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisRecommendationRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DoctorFeedbackRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.ModelRegistryRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.service.DiagnosesCommandService;
import xenosoft.imldintelligence.module.diagnoses.internal.service.ImldInferenceService;
import xenosoft.imldintelligence.module.identity.internal.model.Encounter;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.repository.EncounterRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;

@RestController
@RequiredArgsConstructor
public class DiagnosesController implements DiagnosesControllerContract {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private final DiagnosisSessionRepository sessionRepository;
    private final DiagnosisResultRepository resultRepository;
    private final DiagnosisRecommendationRepository recommendationRepository;
    private final DoctorFeedbackRepository feedbackRepository;
    private final ModelRegistryRepository modelRegistryRepository;
    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final UserAccountRepository userAccountRepository;
    private final LabResultRepository labResultRepository;
    private final ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;
    private final GeneticReportRepository geneticReportRepository;
    private final GeneticVariantRepository geneticVariantRepository;
    private final DiagnosesCommandService diagnosesCommandService;
    private final ImldInferenceService inferenceService;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<PagedResultResponse<DiagnosesApiDtos.Response.DiagnosisSessionResponse>> listSessions(
            Long tenantId,
            DiagnosesApiDtos.Query.SessionPageQuery query,
            PageQueryRequest pageQuery) {
        DiagnosesApiDtos.Query.SessionPageQuery q = query == null
                ? new DiagnosesApiDtos.Query.SessionPageQuery(null, null, null, null, null, null, null)
                : query;
        List<DiagnosisSession> sessions = pickSessions(tenantId, q).stream()
                .filter(s -> q.doctorId() == null || Objects.equals(q.doctorId(), s.getDoctorId()))
                .filter(s -> q.triggeredBy() == null || eqIgnoreCase(q.triggeredBy(), s.getTriggeredBy()))
                .filter(s -> q.status() == null || eqIgnoreCase(q.status(), s.getStatus()))
                .filter(s -> q.startedFrom() == null || !sortTime(s).isBefore(q.startedFrom()))
                .filter(s -> q.startedTo() == null || !sortTime(s).isAfter(q.startedTo()))
                .sorted(Comparator
                        .comparing(this::sortTime, Comparator.reverseOrder())
                        .thenComparing(DiagnosisSession::getId, Comparator.reverseOrder()))
                .toList();

        int page = pageQuery == null || pageQuery.page() == null ? DEFAULT_PAGE : Math.max(pageQuery.page(), 0);
        int size = pageQuery == null || pageQuery.size() == null ? DEFAULT_SIZE : Math.min(Math.max(pageQuery.size(), 1), 200);
        int from = Math.min(page * size, sessions.size());
        int to = Math.min(from + size, sessions.size());
        List<DiagnosesApiDtos.Response.DiagnosisSessionResponse> items = sessions.subList(from, to).stream()
                .map(s -> toSessionResponse(tenantId, s))
                .toList();
        return ApiResponse.success(new PagedResultResponse<>(page, size, sessions.size(), items));
    }

    @Override
    public ApiResponse<DiagnosesApiDtos.Response.DiagnosisSessionResponse> getSession(Long tenantId, Long sessionId) {
        DiagnosisSession session = sessionRepository.findById(tenantId, sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diagnosis session not found"));
        return ApiResponse.success(toSessionResponse(tenantId, session));
    }

    @Override
    public ApiResponse<DiagnosesApiDtos.Response.DiagnosisSessionResponse> startSession(
            Long tenantId,
            DiagnosesApiDtos.Request.StartDiagnosisSessionRequest request) {
        if (request == null || request.patientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId is required");
        }
        Patient patient = patientRepository.findById(tenantId, request.patientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "patientId does not exist"));
        Encounter encounter = resolveEncounter(tenantId, request.patientId(), request.encounterId());
        Long doctorId = resolveDoctorId(tenantId, request.doctorId(), encounter);
        ModelRegistry model = diagnosesCommandService.resolveModel(tenantId, request.modelRegistryId());
        ObjectNode derivedSourceSnapshot = buildDerivedSourceSnapshot(tenantId, patient, encounter);
        ImldInferenceApiDtos.Request.ImldPredictRequest inferenceRequest =
                buildInferenceRequest(tenantId, patient, encounter, request.inputSnapshot(), derivedSourceSnapshot);
        DiagnosisSession session = diagnosesCommandService.createRunningSession(
                tenantId,
                patient.getId(),
                encounter == null ? request.encounterId() : encounter.getId(),
                doctorId,
                model.getId(),
                request.triggeredBy(),
                buildSnapshot(request.inputSnapshot(), inferenceRequest, derivedSourceSnapshot)
        );

        try {
            ImldInferenceApiDtos.Response.PredictData prediction = inferenceService.predict(inferenceRequest);
            session = diagnosesCommandService.completeSession(tenantId, session.getId(), prediction);
        } catch (RuntimeException ex) {
            session = diagnosesCommandService.failSession(tenantId, session.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IMLD inference failed", ex);
        }
        return ApiResponse.success(toSessionResponse(tenantId, session));
    }

    @Override
    public ApiResponse<DiagnosesApiDtos.Response.DiagnosisSessionResponse> submitDoctorFeedback(
            Long tenantId,
            DiagnosesApiDtos.Request.SubmitDoctorFeedbackRequest request) {
        DiagnosisSession session = diagnosesCommandService.submitDoctorFeedback(tenantId, request);
        return ApiResponse.success(toSessionResponse(tenantId, session));
    }

    @Override
    public ApiResponse<PagedResultResponse<DiagnosesApiDtos.Response.ModelRegistryResponse>> listModels(
            Long tenantId,
            DiagnosesApiDtos.Query.ModelRegistryPageQuery query,
            PageQueryRequest pageQuery) {
        DiagnosesApiDtos.Query.ModelRegistryPageQuery q = query == null
                ? new DiagnosesApiDtos.Query.ModelRegistryPageQuery(null, null, null)
                : query;
        List<ModelRegistry> filtered = modelRegistryRepository.listByTenantId(tenantId).stream()
                .filter(m -> q.provider() == null || eqIgnoreCase(q.provider(), m.getProvider()))
                .filter(m -> q.modelType() == null || eqIgnoreCase(q.modelType(), m.getModelType()))
                .filter(m -> q.status() == null || eqIgnoreCase(q.status(), m.getStatus()))
                .sorted(Comparator.comparing(ModelRegistry::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        int page = pageQuery == null || pageQuery.page() == null ? DEFAULT_PAGE : Math.max(pageQuery.page(), 0);
        int size = pageQuery == null || pageQuery.size() == null ? DEFAULT_SIZE : Math.min(Math.max(pageQuery.size(), 1), 200);
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<DiagnosesApiDtos.Response.ModelRegistryResponse> items = filtered.subList(from, to).stream()
                .map(this::toModelResponse)
                .toList();
        return ApiResponse.success(new PagedResultResponse<>(page, size, filtered.size(), items));
    }

    @Override
    public ApiResponse<DiagnosesApiDtos.Response.ModelRegistryResponse> registerModel(
            Long tenantId,
            DiagnosesApiDtos.Request.RegisterModelRequest request) {
        ModelRegistry model = diagnosesCommandService.registerModel(tenantId, request);
        return ApiResponse.success(toModelResponse(model));
    }

    private List<DiagnosisSession> pickSessions(Long tenantId, DiagnosesApiDtos.Query.SessionPageQuery q) {
        if (q.patientId() != null) return sessionRepository.listByPatientId(tenantId, q.patientId());
        if (q.encounterId() != null) return sessionRepository.listByEncounterId(tenantId, q.encounterId());
        return sessionRepository.listByTenantId(tenantId);
    }

    private DiagnosesApiDtos.Response.DiagnosisSessionResponse toSessionResponse(Long tenantId, DiagnosisSession s) {
        return new DiagnosesApiDtos.Response.DiagnosisSessionResponse(
                s.getId(), s.getPatientId(), s.getEncounterId(), s.getDoctorId(), s.getTriggeredBy(), s.getModelRegistryId(),
                s.getStatus(), s.getStartedAt(), s.getCompletedAt(),
                resultRepository.listBySessionId(tenantId, s.getId()).stream().map(r -> new DiagnosesApiDtos.Shared.DiagnosisResultItem(
                        r.getId(), r.getDiseaseCode(), r.getDiseaseName(), r.getConfidence(), r.getRankNo(), r.getRiskLevel(),
                        r.getEvidenceJson(), r.getIsDisplayToPatient(), r.getCreatedAt())).toList(),
                recommendationRepository.listBySessionId(tenantId, s.getId()).stream().map(r -> new DiagnosesApiDtos.Shared.DiagnosisRecommendationItem(
                        r.getId(), r.getRecType(), r.getContent(), r.getPriority(), r.getReason(), r.getCreatedAt())).toList(),
                feedbackRepository.listBySessionId(tenantId, s.getId()).stream().map(f -> new DiagnosesApiDtos.Shared.DoctorFeedbackItem(
                        f.getId(), f.getResultId(), f.getDoctorId(), f.getAction(), f.getModifiedValue(), f.getRejectReason(), f.getCreatedAt())).toList()
        );
    }

    private DiagnosesApiDtos.Response.ModelRegistryResponse toModelResponse(ModelRegistry m) {
        return new DiagnosesApiDtos.Response.ModelRegistryResponse(
                m.getId(), m.getModelCode(), m.getModelName(), m.getModelType(), m.getModelVersion(),
                m.getProvider(), m.getStatus(), m.getReleasedAt(), m.getCreatedAt()
        );
    }

    private Encounter resolveEncounter(Long tenantId, Long patientId, Long encounterId) {
        if (encounterId != null) {
            return encounterRepository.findById(tenantId, encounterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "encounterId does not exist"));
        }
        return encounterRepository.listByPatientId(tenantId, patientId).stream()
                .sorted(Comparator.comparing(Encounter::getStartAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .findFirst().orElse(null);
    }

    private Long resolveDoctorId(Long tenantId, Long doctorId, Encounter encounter) {
        if (doctorId != null) {
            return userAccountRepository.findById(tenantId, doctorId).map(UserAccount::getId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "doctorId does not exist"));
        }
        if (encounter != null && encounter.getAttendingDoctorId() != null &&
                userAccountRepository.findById(tenantId, encounter.getAttendingDoctorId()).isPresent()) {
            return encounter.getAttendingDoctorId();
        }
        return userAccountRepository.listByTenantId(tenantId).stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .findFirst().map(UserAccount::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active doctor in tenant"));
    }

    private ImldInferenceApiDtos.Request.ImldPredictRequest buildInferenceRequest(
            Long tenantId, Patient patient, Encounter encounter, JsonNode input, ObjectNode derivedSourceSnapshot) {
        JsonNode n = input != null && input.has("inference_input") ? input.get("inference_input") : input;
        List<LabResult> labs = encounter != null ? labResultRepository.listByEncounterId(tenantId, encounter.getId())
                : labResultRepository.listByPatientId(tenantId, patient.getId());
        if (labs.isEmpty()) labs = labResultRepository.listByPatientId(tenantId, patient.getId());
        int age = clamp(intOf(n, "age").orElse(ageOf(patient)), 1, 120);
        int gender = clamp(intOf(n, "gender").orElse(genderFlag(patient.getGender())), 0, 1);
        double alt = Math.max(0D, doubleOf(n, "ALT", "alt").orElse(indicatorValue(labs, Set.of("ALT", "GPT")).orElse(45D)));
        double bilirubin = Math.max(0D, doubleOf(n, "bilirubin", "TBIL").orElse(indicatorValue(labs, Set.of("TBIL", "BILIRUBIN")).orElse(21D)));
        double ceruloplasmin = Math.max(0D, doubleOf(n, "ceruloplasmin", "CP").orElse(indicatorValue(labs, Set.of("CERULOPLASMIN", "CP")).orElse(180D)));
        int jaundice = clamp(intOf(n, "jaundice").orElse(0), 0, 1);
        Integer nasScore = intOf(n, "nasScore", "nas_score").orElseGet(() -> pathologyNasScore(derivedSourceSnapshot));
        List<ImldInferenceApiDtos.Request.GeneVariant> geneVariants = geneVariants(tenantId, patient.getId(), encounter, n);
        Map<String, Double> clinicalFeatures = buildClinicalFeatures(
                tenantId, patient, encounter, n, labs, derivedSourceSnapshot, age, gender, alt, bilirubin, ceruloplasmin, nasScore
        );
        return new ImldInferenceApiDtos.Request.ImldPredictRequest(age, gender, alt, bilirubin, ceruloplasmin, jaundice, nasScore, geneVariants,
                clinicalFeatures,
                patient.getPatientNo() == null ? String.valueOf(patient.getId()) : patient.getPatientNo());
    }

    private Map<String, Double> buildClinicalFeatures(
            Long tenantId,
            Patient patient,
            Encounter encounter,
            JsonNode input,
            List<LabResult> labs,
            ObjectNode derivedSourceSnapshot,
            int age,
            int gender,
            double alt,
            double bilirubin,
            double ceruloplasmin,
            Integer nasScore) {
        Map<String, Double> features = new LinkedHashMap<>();
        putFeature(features, "age", (double) age);
        putFeature(features, "gender", (double) gender);
        putFeature(features, "ALT(U/L)", alt);
        putFeature(features, "TBIL(μmol/L)", bilirubin);
        putFeature(features, "ceruloplasmin", ceruloplasmin);
        if (nasScore != null) {
            putFeature(features, "NAS", nasScore.doubleValue());
        }

        copyClinicalFeaturesFromInput(features, input);
        copyClinicalFeaturesFromLabs(features, labs);
        copyClinicalFeaturesFromHistory(features, findLatestClinicalHistoryEntry(tenantId, patient.getId(), encounter, "PATIENT_HISTORY").map(ClinicalHistoryEntry::getContentJson).orElse(null));
        Integer derivedNas = pathologyNasScore(derivedSourceSnapshot);
        if (derivedNas != null && !features.containsKey("NAS")) {
            putFeature(features, "NAS", derivedNas.doubleValue());
        }
        return features;
    }

    private void copyClinicalFeaturesFromInput(Map<String, Double> features, JsonNode input) {
        JsonNode clinicalFeatures = input != null && input.has("clinical_features") ? input.get("clinical_features") : null;
        if (clinicalFeatures == null || !clinicalFeatures.isObject()) {
            return;
        }
        clinicalFeatures.properties().forEach(entry -> {
            JsonNode value = entry.getValue();
            if (value != null && value.isNumber()) {
                putFeature(features, entry.getKey(), value.asDouble());
            }
        });
    }

    private void copyClinicalFeaturesFromLabs(Map<String, Double> features, List<LabResult> labs) {
        putLabFeature(features, labs, "TBIL(μmol/L)", "TBIL", "BILIRUBIN");
        putLabFeature(features, labs, "DBIL(μmol/L)", "DBIL");
        putLabFeature(features, labs, "IBIL(μmol/L)", "IBIL");
        putLabFeature(features, labs, "ALT(U/L)", "ALT", "GPT");
        putLabFeature(features, labs, "AST(U/L)", "AST", "GOT");
        putLabFeature(features, labs, "TP(g/L)", "TP");
        putLabFeature(features, labs, "ALB(g/L)", "ALB");
        putLabFeature(features, labs, "GLB(g/L)", "GLB", "GLOB");
        putLabFeature(features, labs, "GLU(mmol/L)", "GLU");
        putLabFeature(features, labs, "URIC(μmol/L)", "URIC", "UA");
        putLabFeature(features, labs, "TG(mmol/L)", "TG");
        putLabFeature(features, labs, "CHOL(mmol/L)", "CHOL", "TC");
        putLabFeature(features, labs, "HDL-C(mmol/L)", "HDL_C", "HDL-C", "HDLC");
        putLabFeature(features, labs, "LDL-C(mmol/L)", "LDL_C", "LDL-C", "LDLC");
        putLabFeature(features, labs, "ALP(U/L)", "ALP");
        putLabFeature(features, labs, "GGT(U/L)", "GGT");
        putLabFeature(features, labs, "TBA(μmol/L)", "TBA");
        putLabFeature(features, labs, "NH3(μmol/L)", "NH3");
        putLabFeature(features, labs, "PLT(10^9/L)", "PLT");
        putLabFeature(features, labs, "WBC(10^9/L)", "WBC");
        putLabFeature(features, labs, "ceruloplasmin", "CERULOPLASMIN", "CER", "CP");
        putLabFeature(features, labs, "PIVKA（mAU/mL）", "PIVKA", "PIVKAII", "PIVKA_II");
        putLabFeature(features, labs, "PT(s)", "PT");
        putLabFeature(features, labs, "INR", "INR");
        putLabFeature(features, labs, "CRP(mg/L)", "CRP");
        putLabFeature(features, labs, "IgG(g/L)", "IGG");
        putLabFeature(features, labs, "IgA(g/L)", "IGA");
        putLabFeature(features, labs, "IgM(g/L)", "IGM");
    }

    private void copyClinicalFeaturesFromHistory(Map<String, Double> features, JsonNode history) {
        JsonNode diseaseHistory = history == null ? null : history.get("diseaseHistory");
        if (diseaseHistory == null || !diseaseHistory.isObject()) {
            return;
        }
        putFlagFeature(features, diseaseHistory, "Smoking", "smokingHistory");
        putFlagFeature(features, diseaseHistory, "Drinking", "drinkingHistory");
        putFlagFeature(features, diseaseHistory, "糖尿病病史", "diabetesHistory");
        putFlagFeature(features, diseaseHistory, "高血压病史", "hypertensionHistory");
        putFlagFeature(features, diseaseHistory, "高尿酸血症病史", "hyperuricemiaHistory");
        putFlagFeature(features, diseaseHistory, "高脂血症病史", "hyperlipidemiaHistory");
        putFlagFeature(features, diseaseHistory, "乙肝病史", "hepatitisBHistory");
    }

    private void putLabFeature(Map<String, Double> features, List<LabResult> labs, String feature, String... aliases) {
        indicatorValue(labs, Set.of(aliases)).ifPresent(value -> putFeature(features, feature, value));
    }

    private void putFlagFeature(Map<String, Double> features, JsonNode diseaseHistory, String feature, String key) {
        JsonNode value = diseaseHistory.get(key);
        if (value == null || value.isNull()) {
            return;
        }
        String normalized = value.asText("").trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty() || "UNKNOWN".equals(normalized)) {
            return;
        }
        putFeature(features, feature, normalized.equals("YES") || normalized.equals("Y") || normalized.equals("TRUE") || normalized.equals("1") ? 1D : 0D);
    }

    private void putFeature(Map<String, Double> features, String feature, Double value) {
        if (feature == null || feature.isBlank() || value == null || value.isNaN() || value.isInfinite()) {
            return;
        }
        features.put(feature, value);
    }

    private List<ImldInferenceApiDtos.Request.GeneVariant> geneVariants(Long tenantId, Long patientId, Encounter encounter, JsonNode n) {
        JsonNode array = n != null && n.has("gene_variants") ? n.get("gene_variants") : n != null ? n.get("geneVariants") : null;
        if (array != null && array.isArray()) {
            List<ImldInferenceApiDtos.Request.GeneVariant> variants = new ArrayList<>();
            for (JsonNode item : array) {
                variants.add(new ImldInferenceApiDtos.Request.GeneVariant(
                        strOf(item, "gene").orElse("UNKNOWN"),
                        strOf(item, "c_change", "cChange").orElse(null),
                        strOf(item, "p_change", "pChange").orElse(null),
                        strOf(item, "variant_type", "variantType").map(String::toLowerCase).orElse("unknown"),
                        strOf(item, "zygosity").map(String::toLowerCase).orElse("unknown"),
                        strOf(item, "pathogenicity", "classification").map(String::toLowerCase).orElse("unknown"),
                        doubleOf(item, "allele_frequency", "alleleFrequency").orElse(null)
                ));
            }
            return variants;
        }
        List<GeneticReport> reports = encounter != null ? geneticReportRepository.listByEncounterId(tenantId, encounter.getId())
                : geneticReportRepository.listByPatientId(tenantId, patientId);
        Optional<GeneticReport> latest = reports.stream().sorted(Comparator.comparing(GeneticReport::getCreatedAt).reversed()).findFirst();
        if (latest.isEmpty()) return List.of();
        return geneticVariantRepository.listByReportId(tenantId, latest.get().getId()).stream()
                .map(v -> new ImldInferenceApiDtos.Request.GeneVariant(
                        v.getGene(), v.getHgvsC(), v.getHgvsP(), normalize(v.getVariantType(), "unknown").toLowerCase(Locale.ROOT),
                        normalize(v.getZygosity(), "unknown").toLowerCase(Locale.ROOT),
                        normalize(v.getClassification(), "unknown").toLowerCase(Locale.ROOT), null
                )).toList();
    }

    private ObjectNode buildDerivedSourceSnapshot(Long tenantId, Patient patient, Encounter encounter) {
        ObjectNode derived = objectMapper.createObjectNode();
        findLatestPathologyEntry(tenantId, patient.getId(), encounter).ifPresent(entry -> {
            ObjectNode pathology = objectMapper.createObjectNode();
            JsonNode content = entry.getContentJson();
            if (content != null && content.get("reportText") != null && !content.get("reportText").isNull()) {
                pathology.put("reportText", content.get("reportText").asText());
            }
            if (content != null && content.get("nasScore") != null && !content.get("nasScore").isNull()) {
                pathology.put("nasScore", content.get("nasScore").asInt());
            }
            if (content != null && content.get("reportedAt") != null && !content.get("reportedAt").isNull()) {
                pathology.put("reportedAt", content.get("reportedAt").asText());
            }
            derived.set("pathology", pathology);
        });
        return derived;
    }

    private Optional<ClinicalHistoryEntry> findLatestPathologyEntry(Long tenantId, Long patientId, Encounter encounter) {
        return findLatestClinicalHistoryEntry(tenantId, patientId, encounter, "PATHOLOGY");
    }

    private Optional<ClinicalHistoryEntry> findLatestClinicalHistoryEntry(Long tenantId, Long patientId, Encounter encounter, String historyType) {
        List<ClinicalHistoryEntry> entries = encounter != null
                ? clinicalHistoryEntryRepository.listByEncounterId(tenantId, encounter.getId())
                : clinicalHistoryEntryRepository.listByPatientId(tenantId, patientId);
        return entries.stream()
                .filter(entry -> historyType.equalsIgnoreCase(entry.getHistoryType()))
                .sorted(Comparator.comparing((ClinicalHistoryEntry entry) -> entry.getRecordedAt() != null ? entry.getRecordedAt() : now()).reversed()
                        .thenComparing(ClinicalHistoryEntry::getId, Comparator.reverseOrder()))
                .findFirst();
    }

    private Integer pathologyNasScore(ObjectNode derivedSourceSnapshot) {
        if (derivedSourceSnapshot == null || derivedSourceSnapshot.get("pathology") == null) {
            return null;
        }
        JsonNode pathology = derivedSourceSnapshot.get("pathology");
        return pathology != null && pathology.get("nasScore") != null && !pathology.get("nasScore").isNull()
                ? pathology.get("nasScore").asInt()
                : null;
    }

    private void mergeSourceSnapshot(ObjectNode target, ObjectNode derived) {
        if (target == null || derived == null) {
            return;
        }

        target.setAll(derived);
    }

    private JsonNode buildSnapshot(JsonNode input,
                                   ImldInferenceApiDtos.Request.ImldPredictRequest inferenceRequest,
                                   ObjectNode derivedSourceSnapshot) {
        if (input != null && input.has("inference_input")) {
            if (input.isObject() && derivedSourceSnapshot != null && !derivedSourceSnapshot.isEmpty()) {
                ObjectNode snapshot = ((ObjectNode) input).deepCopy();
                ObjectNode sourceSnapshot = snapshot.has("source_snapshot") && snapshot.get("source_snapshot").isObject()
                        ? (ObjectNode) snapshot.get("source_snapshot")
                        : objectMapper.createObjectNode();
                mergeSourceSnapshot(sourceSnapshot, derivedSourceSnapshot);
                snapshot.set("source_snapshot", sourceSnapshot);
                return snapshot;
            }
            return input;
        }
        ObjectNode node = objectMapper.createObjectNode();
        ObjectNode sourceSnapshot = input != null && input.isObject() ? ((ObjectNode) input).deepCopy() : objectMapper.createObjectNode();
        mergeSourceSnapshot(sourceSnapshot, derivedSourceSnapshot);
        if (!sourceSnapshot.isEmpty()) {
            node.set("source_snapshot", sourceSnapshot);
        }
        node.set("inference_input", objectMapper.valueToTree(inferenceRequest));
        return node;
    }

    private int ageOf(Patient patient) {
        LocalDate birth = patient.getBirthDate();
        return birth == null ? 40 : (int) ChronoUnit.YEARS.between(birth, LocalDate.now(ZoneOffset.UTC));
    }

    private int genderFlag(String gender) {
        String n = normalize(gender, "1").toLowerCase(Locale.ROOT);
        return n.equals("0") || n.equals("f") || n.equals("female") || n.equals("女") ? 0 : 1;
    }

    private Optional<Double> indicatorValue(List<LabResult> labs, Set<String> aliases) {
        Set<String> normalizedAliases = aliases.stream().map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        return labs.stream()
                .filter(l -> l.getIndicatorCode() != null && normalizedAliases.contains(l.getIndicatorCode().toUpperCase(Locale.ROOT)))
                .map(LabResult::getValueNumeric).filter(Objects::nonNull).findFirst();
    }

    private Optional<String> strOf(JsonNode node, String... keys) {
        if (node == null) return Optional.empty();
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull() && !value.asText().isBlank()) return Optional.of(value.asText().trim());
        }
        return Optional.empty();
    }

    private Optional<Integer> intOf(JsonNode node, String... keys) {
        return strOf(node, keys).flatMap(value -> {
            try { return Optional.of(Integer.valueOf(value)); } catch (NumberFormatException ignored) { return Optional.empty(); }
        });
    }

    private Optional<Double> doubleOf(JsonNode node, String... keys) {
        return strOf(node, keys).flatMap(value -> {
            try { return Optional.of(Double.valueOf(value)); } catch (NumberFormatException ignored) { return Optional.empty(); }
        });
    }

    private String normalize(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed.toUpperCase(Locale.ROOT);
    }

    private boolean eqIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private OffsetDateTime sortTime(DiagnosisSession s) {
        return s.getStartedAt() != null ? s.getStartedAt() : (s.getCreatedAt() != null ? s.getCreatedAt() : now());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
