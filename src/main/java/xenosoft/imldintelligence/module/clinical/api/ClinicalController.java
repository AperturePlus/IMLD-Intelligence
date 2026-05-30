package xenosoft.imldintelligence.module.clinical.api;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.common.dto.PageQueryRequest;
import xenosoft.imldintelligence.common.dto.PagedResultResponse;
import xenosoft.imldintelligence.module.clinical.api.dto.ClinicalApiDtos;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticReport;
import xenosoft.imldintelligence.module.clinical.internal.model.GeneticVariant;
import xenosoft.imldintelligence.module.clinical.internal.model.ImagingReport;
import xenosoft.imldintelligence.module.clinical.internal.model.IndicatorMapping;
import xenosoft.imldintelligence.module.clinical.internal.model.LabResult;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.GeneticVariantRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.ImagingReportRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.IndicatorMappingRepository;
import xenosoft.imldintelligence.module.clinical.internal.repository.LabResultRepository;

@RestController
@RequiredArgsConstructor
public class ClinicalController implements ClinicalControllerContract {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String HISTORY_TYPE_PATHOLOGY = "PATHOLOGY";

    private final LabResultRepository labResultRepository;
    private final GeneticReportRepository geneticReportRepository;
    private final GeneticVariantRepository geneticVariantRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;
    private final IndicatorMappingRepository indicatorMappingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<PagedResultResponse<ClinicalApiDtos.Response.LabResultResponse>> listLabResults(
            Long tenantId,
            ClinicalApiDtos.Query.LabResultPageQuery query,
            PageQueryRequest pageQuery) {
        ClinicalApiDtos.Query.LabResultPageQuery q = query == null
                ? new ClinicalApiDtos.Query.LabResultPageQuery(null, null, null, null, null, null)
                : query;
        List<LabResult> filtered = pickLabResults(tenantId, q).stream()
                .filter(item -> q.indicatorCode() == null || eqIgnoreCase(item.getIndicatorCode(), q.indicatorCode()))
                .filter(item -> q.abnormalFlag() == null || eqIgnoreCase(item.getAbnormalFlag(), q.abnormalFlag()))
                .filter(item -> q.collectedFrom() == null || !timeOf(item.getCollectedAt(), item.getCreatedAt()).isBefore(q.collectedFrom()))
                .filter(item -> q.collectedTo() == null || !timeOf(item.getCollectedAt(), item.getCreatedAt()).isAfter(q.collectedTo()))
                .sorted(Comparator.comparing((LabResult item) -> timeOf(item.getCollectedAt(), item.getCreatedAt())).reversed()
                        .thenComparing(LabResult::getId, Comparator.reverseOrder()))
                .toList();
        return ApiResponse.success(page(filtered, pageQuery, this::toLabResultResponse));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.LabResultResponse> upsertLabResult(
            Long tenantId,
            ClinicalApiDtos.Request.UpsertLabResultRequest request) {
        LabResult labResult = new LabResult();
        labResult.setTenantId(tenantId);
        labResult.setPatientId(request.patientId());
        labResult.setEncounterId(request.encounterId());
        labResult.setIndicatorCode(trimToNull(request.indicatorCode()));
        labResult.setValueNumeric(request.valueNumeric());
        labResult.setValueText(trimToNull(request.valueText()));
        labResult.setUnit(trimToNull(request.unit()));
        labResult.setReferenceLow(request.referenceLow());
        labResult.setReferenceHigh(request.referenceHigh());
        labResult.setAbnormalFlag(trimToNull(request.abnormalFlag()));
        labResult.setSourceType(normalize(request.sourceType(), "MANUAL"));
        labResult.setRawData(request.rawData());
        labResult.setCollectedAt(request.collectedAt());
        labResultRepository.save(labResult);
        return ApiResponse.success(toLabResultResponse(labResult));
    }

    @Override
    public ApiResponse<PagedResultResponse<ClinicalApiDtos.Response.GeneticReportResponse>> listGeneticReports(
            Long tenantId,
            ClinicalApiDtos.Query.GeneticReportPageQuery query,
            PageQueryRequest pageQuery) {
        ClinicalApiDtos.Query.GeneticReportPageQuery q = query == null
                ? new ClinicalApiDtos.Query.GeneticReportPageQuery(null, null, null, null)
                : query;
        List<GeneticReport> filtered = pickGeneticReports(tenantId, q.patientId(), q.encounterId()).stream()
                .filter(item -> q.parseStatus() == null || eqIgnoreCase(item.getParseStatus(), q.parseStatus()))
                .filter(item -> q.conclusion() == null || eqIgnoreCase(item.getConclusion(), q.conclusion()))
                .sorted(Comparator.comparing((GeneticReport item) -> timeOf(item.getCreatedAt(), null)).reversed()
                        .thenComparing(GeneticReport::getId, Comparator.reverseOrder()))
                .toList();
        return ApiResponse.success(page(filtered, pageQuery, item -> toGeneticReportResponse(tenantId, item)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.GeneticReportResponse> registerGeneticReport(
            Long tenantId,
            ClinicalApiDtos.Request.RegisterGeneticReportRequest request) {
        GeneticReport report = new GeneticReport();
        report.setTenantId(tenantId);
        report.setPatientId(request.patientId());
        report.setEncounterId(request.encounterId());
        report.setReportSource(trimToNull(request.reportSource()));
        report.setTestMethod(trimToNull(request.testMethod()));
        report.setReportDate(request.reportDate());
        report.setFileId(request.fileId());
        report.setParseStatus(determineGeneticParseStatus(request));
        report.setSummary(trimToNull(request.summary()));
        report.setConclusion(trimToNull(request.conclusion()));
        geneticReportRepository.save(report);

        List<ClinicalApiDtos.Request.GeneticVariantWriteItem> variants =
                request.variants() == null ? List.of() : request.variants();
        for (ClinicalApiDtos.Request.GeneticVariantWriteItem variantRequest : variants) {
            GeneticVariant variant = new GeneticVariant();
            variant.setTenantId(tenantId);
            variant.setReportId(report.getId());
            variant.setGene(trimToNull(variantRequest.gene()));
            variant.setChromosome(trimToNull(variantRequest.chromosome()));
            variant.setPosition(variantRequest.position());
            variant.setRefAllele(trimToNull(variantRequest.refAllele()));
            variant.setAltAllele(trimToNull(variantRequest.altAllele()));
            variant.setVariantType(trimToNull(variantRequest.variantType()));
            variant.setZygosity(trimToNull(variantRequest.zygosity()));
            variant.setClassification(trimToNull(variantRequest.classification()));
            variant.setHgvsC(trimToNull(variantRequest.hgvsC()));
            variant.setHgvsP(trimToNull(variantRequest.hgvsP()));
            variant.setEvidence(trimToNull(variantRequest.evidence()));
            variant.setSourceType(normalize(variantRequest.sourceType(), "MANUAL"));
            geneticVariantRepository.save(variant);
        }

        return ApiResponse.success(toGeneticReportResponse(tenantId, report));
    }

    @Override
    public ApiResponse<PagedResultResponse<ClinicalApiDtos.Response.ImagingReportResponse>> listImagingReports(
            Long tenantId,
            ClinicalApiDtos.Query.ImagingReportPageQuery query,
            PageQueryRequest pageQuery) {
        ClinicalApiDtos.Query.ImagingReportPageQuery q = query == null
                ? new ClinicalApiDtos.Query.ImagingReportPageQuery(null, null, null, null, null)
                : query;
        List<ImagingReport> filtered = pickImagingReports(tenantId, q.patientId(), q.encounterId()).stream()
                .filter(item -> q.modality() == null || eqIgnoreCase(item.getModality(), q.modality()))
                .filter(item -> q.examinedFrom() == null || !timeOf(item.getExaminedAt(), item.getCreatedAt()).isBefore(q.examinedFrom()))
                .filter(item -> q.examinedTo() == null || !timeOf(item.getExaminedAt(), item.getCreatedAt()).isAfter(q.examinedTo()))
                .sorted(Comparator.comparing((ImagingReport item) -> timeOf(item.getExaminedAt(), item.getCreatedAt())).reversed()
                        .thenComparing(ImagingReport::getId, Comparator.reverseOrder()))
                .toList();
        return ApiResponse.success(page(filtered, pageQuery, this::toImagingReportResponse));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.ImagingReportResponse> upsertImagingReport(
            Long tenantId,
            ClinicalApiDtos.Request.UpsertImagingReportRequest request) {
        ImagingReport imagingReport = new ImagingReport();
        imagingReport.setTenantId(tenantId);
        imagingReport.setPatientId(request.patientId());
        imagingReport.setEncounterId(request.encounterId());
        imagingReport.setModality(trimToNull(request.modality()));
        imagingReport.setReportText(trimToNull(request.reportText()));
        imagingReport.setFileId(request.fileId());
        imagingReport.setSourceSystem(normalize(request.sourceSystem(), "PACS"));
        imagingReport.setExaminedAt(request.examinedAt());
        imagingReportRepository.save(imagingReport);
        return ApiResponse.success(toImagingReportResponse(imagingReport));
    }

    @Override
    public ApiResponse<PagedResultResponse<ClinicalApiDtos.Response.PathologyReportResponse>> listPathologyReports(
            Long tenantId,
            ClinicalApiDtos.Query.PathologyReportPageQuery query,
            PageQueryRequest pageQuery) {
        ClinicalApiDtos.Query.PathologyReportPageQuery q = query == null
                ? new ClinicalApiDtos.Query.PathologyReportPageQuery(null, null, null, null, null)
                : query;
        List<ClinicalHistoryEntry> filtered = pickClinicalEntries(tenantId, q.patientId(), q.encounterId()).stream()
                .filter(item -> HISTORY_TYPE_PATHOLOGY.equalsIgnoreCase(item.getHistoryType()))
                .filter(item -> q.sourceType() == null || eqIgnoreCase(item.getSourceType(), q.sourceType()))
                .filter(item -> q.reportedFrom() == null || !pathologyReportedAt(item).isBefore(q.reportedFrom()))
                .filter(item -> q.reportedTo() == null || !pathologyReportedAt(item).isAfter(q.reportedTo()))
                .sorted(Comparator.comparing(this::pathologyReportedAt).reversed()
                        .thenComparing(ClinicalHistoryEntry::getId, Comparator.reverseOrder()))
                .toList();
        return ApiResponse.success(page(filtered, pageQuery, this::toPathologyReportResponse));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.PathologyReportResponse> recordPathologyReport(
            Long tenantId,
            ClinicalApiDtos.Request.RecordPathologyReportRequest request) {
        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenantId);
        entry.setPatientId(request.patientId());
        entry.setEncounterId(request.encounterId());
        entry.setHistoryType(HISTORY_TYPE_PATHOLOGY);
        entry.setTemplateCode(null);
        entry.setContentJson(toPathologyContent(request));
        entry.setSourceType(normalize(request.sourceType(), "MANUAL"));
        entry.setRecordedBy(request.recordedBy());
        entry.setRecordedAt(request.reportedAt() == null ? now() : request.reportedAt());
        clinicalHistoryEntryRepository.save(entry);
        return ApiResponse.success(toPathologyReportResponse(entry));
    }

    @Override
    public ApiResponse<PagedResultResponse<ClinicalApiDtos.Response.ClinicalHistoryEntryResponse>> listClinicalHistoryEntries(
            Long tenantId,
            ClinicalApiDtos.Query.ClinicalHistoryPageQuery query,
            PageQueryRequest pageQuery) {
        ClinicalApiDtos.Query.ClinicalHistoryPageQuery q = query == null
                ? new ClinicalApiDtos.Query.ClinicalHistoryPageQuery(null, null, null, null, null)
                : query;
        List<ClinicalHistoryEntry> filtered = pickClinicalEntries(tenantId, q.patientId(), q.encounterId()).stream()
                .filter(item -> q.historyType() == null || eqIgnoreCase(item.getHistoryType(), q.historyType()))
                .filter(item -> q.recordedFrom() == null || !timeOf(item.getRecordedAt(), item.getCreatedAt()).isBefore(q.recordedFrom()))
                .filter(item -> q.recordedTo() == null || !timeOf(item.getRecordedAt(), item.getCreatedAt()).isAfter(q.recordedTo()))
                .sorted(Comparator.comparing((ClinicalHistoryEntry item) -> timeOf(item.getRecordedAt(), item.getCreatedAt())).reversed()
                        .thenComparing(ClinicalHistoryEntry::getId, Comparator.reverseOrder()))
                .toList();
        return ApiResponse.success(page(filtered, pageQuery, this::toClinicalHistoryResponse));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.ClinicalHistoryEntryResponse> recordClinicalHistory(
            Long tenantId,
            ClinicalApiDtos.Request.RecordClinicalHistoryRequest request) {
        ClinicalHistoryEntry entry = new ClinicalHistoryEntry();
        entry.setTenantId(tenantId);
        entry.setPatientId(request.patientId());
        entry.setEncounterId(request.encounterId());
        entry.setHistoryType(trimToNull(request.historyType()));
        entry.setTemplateCode(trimToNull(request.templateCode()));
        entry.setContentJson(request.contentJson());
        entry.setSourceType(normalize(request.sourceType(), "MANUAL"));
        entry.setRecordedBy(request.recordedBy());
        entry.setRecordedAt(request.recordedAt() == null ? now() : request.recordedAt());
        clinicalHistoryEntryRepository.save(entry);
        return ApiResponse.success(toClinicalHistoryResponse(entry));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApiResponse<ClinicalApiDtos.Response.IndicatorMappingResponse> upsertIndicatorMapping(
            Long tenantId,
            ClinicalApiDtos.Request.UpsertIndicatorMappingRequest request) {
        IndicatorMapping mapping = new IndicatorMapping();
        mapping.setTenantId(tenantId);
        mapping.setSourceSystem(trimToNull(request.sourceSystem()));
        mapping.setSourceCode(trimToNull(request.sourceCode()));
        mapping.setSourceName(trimToNull(request.sourceName()));
        mapping.setTargetIndicatorCode(trimToNull(request.targetIndicatorCode()));
        mapping.setUnitConversionExpr(trimToNull(request.unitConversionExpr()));
        mapping.setQualityRule(request.qualityRule());
        mapping.setStatus(normalize(request.status(), "ACTIVE"));
        mapping = indicatorMappingRepository.upsertByNaturalKey(mapping);
        return ApiResponse.success(toIndicatorMappingResponse(mapping));
    }

    private List<LabResult> pickLabResults(Long tenantId, ClinicalApiDtos.Query.LabResultPageQuery query) {
        if (query.patientId() != null) {
            return labResultRepository.listByPatientId(tenantId, query.patientId());
        }
        if (query.encounterId() != null) {
            return labResultRepository.listByEncounterId(tenantId, query.encounterId());
        }
        return labResultRepository.listByTenantId(tenantId);
    }

    private List<GeneticReport> pickGeneticReports(Long tenantId, Long patientId, Long encounterId) {
        if (patientId != null) {
            return geneticReportRepository.listByPatientId(tenantId, patientId);
        }
        if (encounterId != null) {
            return geneticReportRepository.listByEncounterId(tenantId, encounterId);
        }
        return geneticReportRepository.listByTenantId(tenantId);
    }

    private List<ImagingReport> pickImagingReports(Long tenantId, Long patientId, Long encounterId) {
        if (patientId != null) {
            return imagingReportRepository.listByPatientId(tenantId, patientId);
        }
        if (encounterId != null) {
            return imagingReportRepository.listByEncounterId(tenantId, encounterId);
        }
        return imagingReportRepository.listByTenantId(tenantId);
    }

    private List<ClinicalHistoryEntry> pickClinicalEntries(Long tenantId, Long patientId, Long encounterId) {
        if (patientId != null) {
            return clinicalHistoryEntryRepository.listByPatientId(tenantId, patientId);
        }
        if (encounterId != null) {
            return clinicalHistoryEntryRepository.listByEncounterId(tenantId, encounterId);
        }
        return clinicalHistoryEntryRepository.listByTenantId(tenantId);
    }

    private ClinicalApiDtos.Response.LabResultResponse toLabResultResponse(LabResult item) {
        return new ClinicalApiDtos.Response.LabResultResponse(
                item.getId(), item.getPatientId(), item.getEncounterId(), item.getIndicatorCode(), item.getValueNumeric(),
                item.getValueText(), item.getUnit(), item.getReferenceLow(), item.getReferenceHigh(), item.getAbnormalFlag(),
                item.getSourceType(), item.getCollectedAt(), item.getCreatedAt()
        );
    }

    private ClinicalApiDtos.Response.GeneticReportResponse toGeneticReportResponse(Long tenantId, GeneticReport item) {
        List<ClinicalApiDtos.Shared.GeneticVariantItem> variants = geneticVariantRepository.listByReportId(tenantId, item.getId()).stream()
                .map(variant -> new ClinicalApiDtos.Shared.GeneticVariantItem(
                        variant.getId(), variant.getGene(), variant.getChromosome(), variant.getPosition(), variant.getRefAllele(),
                        variant.getAltAllele(), variant.getVariantType(), variant.getZygosity(), variant.getClassification(),
                        variant.getHgvsC(), variant.getHgvsP(), variant.getEvidence(), variant.getSourceType()))
                .toList();
        return new ClinicalApiDtos.Response.GeneticReportResponse(
                item.getId(), item.getPatientId(), item.getEncounterId(), item.getReportSource(), item.getTestMethod(),
                item.getReportDate(), item.getFileId(), item.getParseStatus(), item.getSummary(), item.getConclusion(),
                item.getCreatedAt(), variants
        );
    }

    private ClinicalApiDtos.Response.ImagingReportResponse toImagingReportResponse(ImagingReport item) {
        return new ClinicalApiDtos.Response.ImagingReportResponse(
                item.getId(), item.getPatientId(), item.getEncounterId(), item.getModality(), item.getReportText(),
                item.getFileId(), item.getSourceSystem(), item.getExaminedAt(), item.getCreatedAt()
        );
    }

    private ClinicalApiDtos.Response.PathologyReportResponse toPathologyReportResponse(ClinicalHistoryEntry entry) {
        JsonNode content = entry.getContentJson();
        return new ClinicalApiDtos.Response.PathologyReportResponse(
                entry.getId(),
                entry.getPatientId(),
                entry.getEncounterId(),
                textAt(content, "reportText"),
                intAt(content, "nasScore"),
                longAt(content, "fileId"),
                entry.getSourceType(),
                entry.getRecordedBy(),
                pathologyReportedAt(entry),
                entry.getCreatedAt()
        );
    }

    private ClinicalApiDtos.Response.ClinicalHistoryEntryResponse toClinicalHistoryResponse(ClinicalHistoryEntry entry) {
        return new ClinicalApiDtos.Response.ClinicalHistoryEntryResponse(
                entry.getId(), entry.getPatientId(), entry.getEncounterId(), entry.getHistoryType(), entry.getTemplateCode(),
                entry.getContentJson(), entry.getSourceType(), entry.getRecordedBy(), entry.getRecordedAt(), entry.getCreatedAt()
        );
    }

    private ClinicalApiDtos.Response.IndicatorMappingResponse toIndicatorMappingResponse(IndicatorMapping item) {
        return new ClinicalApiDtos.Response.IndicatorMappingResponse(
                item.getId(), item.getSourceSystem(), item.getSourceCode(), item.getSourceName(), item.getTargetIndicatorCode(),
                item.getUnitConversionExpr(), item.getQualityRule(), item.getStatus(), item.getCreatedAt()
        );
    }

    private <T, R> PagedResultResponse<R> page(List<T> items, PageQueryRequest pageQuery, Function<T, R> mapper) {
        int page = pageQuery == null || pageQuery.page() == null ? DEFAULT_PAGE : Math.max(pageQuery.page(), 0);
        int size = pageQuery == null || pageQuery.size() == null ? DEFAULT_SIZE : Math.min(Math.max(pageQuery.size(), 1), 200);
        int from = Math.min(page * size, items.size());
        int to = Math.min(from + size, items.size());
        List<R> pageItems = items.subList(from, to).stream().map(mapper).toList();
        return new PagedResultResponse<>(page, size, items.size(), pageItems);
    }

    private String determineGeneticParseStatus(ClinicalApiDtos.Request.RegisterGeneticReportRequest request) {
        boolean hasStructuredVariants = request.variants() != null && !request.variants().isEmpty();
        boolean hasNarrative = trimToNull(request.summary()) != null || trimToNull(request.conclusion()) != null;
        return hasStructuredVariants || hasNarrative ? "PARSED" : "PENDING";
    }

    private ObjectNode toPathologyContent(ClinicalApiDtos.Request.RecordPathologyReportRequest request) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("reportText", request.reportText());
        if (request.nasScore() != null) {
            node.put("nasScore", request.nasScore());
        } else {
            node.putNull("nasScore");
        }
        if (request.fileId() != null) {
            node.put("fileId", request.fileId());
        } else {
            node.putNull("fileId");
        }
        node.put("sourceType", normalize(request.sourceType(), "MANUAL"));
        node.put("reportedAt", (request.reportedAt() == null ? now() : request.reportedAt()).toString());
        return node;
    }

    private OffsetDateTime pathologyReportedAt(ClinicalHistoryEntry entry) {
        JsonNode content = entry.getContentJson();
        String reportedAt = textAt(content, "reportedAt");
        if (reportedAt != null) {
            try {
                return OffsetDateTime.parse(reportedAt);
            } catch (RuntimeException ignored) {
                // Fall through to recordedAt.
            }
        }
        return timeOf(entry.getRecordedAt(), entry.getCreatedAt());
    }

    private OffsetDateTime timeOf(OffsetDateTime primary, OffsetDateTime fallback) {
        if (primary != null) {
            return primary;
        }
        if (fallback != null) {
            return fallback;
        }
        return now();
    }

    private String textAt(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Integer intAt(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).isInt() ? node.get(field).asInt() : Integer.valueOf(node.get(field).asText());
    }

    private Long longAt(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).isLong() ? node.get(field).asLong() : Long.valueOf(node.get(field).asText());
    }

    private boolean eqIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private String normalize(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
