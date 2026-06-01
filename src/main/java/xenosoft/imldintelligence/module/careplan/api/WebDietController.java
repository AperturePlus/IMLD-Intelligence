package xenosoft.imldintelligence.module.careplan.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.careplan.api.dto.DietApiDtos;
import xenosoft.imldintelligence.module.clinical.internal.model.ClinicalHistoryEntry;
import xenosoft.imldintelligence.module.clinical.internal.repository.ClinicalHistoryEntryRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisResult;
import xenosoft.imldintelligence.module.diagnoses.internal.model.DiagnosisSession;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisResultRepository;
import xenosoft.imldintelligence.module.diagnoses.internal.repository.DiagnosisSessionRepository;
import xenosoft.imldintelligence.module.identity.internal.model.Patient;
import xenosoft.imldintelligence.module.identity.internal.repository.PatientRepository;
import xenosoft.imldintelligence.module.notify.internal.model.NotificationMessage;
import xenosoft.imldintelligence.module.notify.internal.repository.NotificationMessageRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class WebDietController implements WebDietControllerContract {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String HISTORY_CLINICAL_DECISION = "CLINICAL_DECISION";
    private static final String DEFAULT_DISEASE = "遗传代谢性肝病风险提示";
    private static final String DEFAULT_COMPLIANCE = "一般";

    private static final DietPlanRule WILSON_RULE = new DietPlanRule(
            List.of(
                    target("每日铜摄入量", "< 1.0", "mg/日", "#f56c6c", "绝对核心指标，超量将加重肝脑损伤"),
                    target("每日蛋白质摄入", "1.5-2.0", "g/kg", "#409EFF", "促进铜排泄与肝细胞修复"),
                    target("每日饮水量", "> 2000", "ml", "#67c23a", "建议饮用纯净水")
            ),
            new DietApiDtos.Response.DietFoodsItem(
                    List.of("猪肝", "牛羊内脏", "巧克力", "花生", "核桃", "牡蛎"),
                    List.of("牛肉", "羊肉", "燕麦", "黄豆"),
                    List.of("精白米面", "鸡蛋清", "瘦猪肉", "牛奶", "白菜", "苹果")
            ),
            List.of(
                    meal("success", "早餐", "牛奶 250ml，白面馒头 1个，鸡蛋白 2个", "含铜量约 0.12mg"),
                    meal("warning", "午餐", "白米饭，清蒸鱼肉，蒜蓉白菜", "含铜量约 0.25mg"),
                    meal("info", "晚餐", "白米粥，青椒肉丝，凉拌黄瓜", "含铜量约 0.18mg")
            )
    );

    private static final DietPlanRule HEMOCHROMATOSIS_RULE = new DietPlanRule(
            List.of(
                    target("每日铁摄入量", "极低", "控制", "#f56c6c", "严格控制富含血红素铁的食物"),
                    target("维生素C摄入", "避免", "随餐", "#e6a23c", "维C会显著增加铁吸收率"),
                    target("每日饮茶量", "推荐", "随餐", "#67c23a", "茶多酚可抑制铁吸收")
            ),
            new DietApiDtos.Response.DietFoodsItem(
                    List.of("猪血", "鸭血", "动物内脏", "牛排", "铁强化谷物", "维生素C补剂"),
                    List.of("鸡鸭肉", "深绿色蔬菜", "柑橘类水果"),
                    List.of("精制谷物", "鸡蛋", "奶制品", "根茎类蔬菜", "红茶")
            ),
            List.of(
                    meal("success", "早餐", "白米粥，水煮鸡蛋，热红茶", "随餐茶饮抑制铁吸收"),
                    meal("warning", "午餐", "素炒西葫芦，清炖豆腐，白米饭", "极低血红素铁"),
                    meal("info", "晚餐", "鸡胸肉沙拉，全麦面包，脱脂牛奶", "避免维C同餐")
            )
    );

    private static final DietPlanRule AAT_RULE = new DietPlanRule(
            List.of(
                    target("每日蛋白质摄入", "1.2-1.5", "g/kg", "#409EFF", "支持肝细胞修复与肌量维持"),
                    target("饱和脂肪摄入", "低", "控制", "#e6a23c", "减轻肝脏代谢负担"),
                    target("酒精摄入", "0", "ml", "#f56c6c", "避免加重肝肺联合损伤")
            ),
            new DietApiDtos.Response.DietFoodsItem(
                    List.of("酒精", "油炸食品", "肥肉", "奶油甜点"),
                    List.of("动物肝脏", "加工肉制品", "高盐腌制品"),
                    List.of("鱼肉", "鸡胸肉", "豆腐", "燕麦", "绿叶蔬菜", "低脂牛奶")
            ),
            List.of(
                    meal("success", "早餐", "燕麦粥，低脂牛奶，水煮蛋", "高蛋白低脂组合"),
                    meal("warning", "午餐", "清蒸鱼，杂粮饭，西兰花", "优质蛋白与膳食纤维"),
                    meal("info", "晚餐", "鸡胸肉蔬菜汤，豆腐，少量主食", "减轻夜间肝脏负担")
            )
    );

    private static final DietPlanRule MASLD_RULE = new DietPlanRule(
            List.of(
                    target("每日总能量", "-10%", "控制", "#409EFF", "在营养充足前提下温和减重"),
                    target("精制糖摄入", "低", "控制", "#f56c6c", "减少肝内脂肪生成"),
                    target("膳食纤维", "> 25", "g/日", "#67c23a", "改善代谢与饱腹感")
            ),
            new DietApiDtos.Response.DietFoodsItem(
                    List.of("含糖饮料", "甜点", "油炸食品", "肥肉"),
                    List.of("白米粥", "精制面点", "高糖水果"),
                    List.of("全谷物", "深色蔬菜", "鱼肉", "豆制品", "坚果少量")
            ),
            List.of(
                    meal("success", "早餐", "全麦面包，鸡蛋，低脂牛奶", "低糖高饱腹"),
                    meal("warning", "午餐", "糙米饭，清蒸鱼，双份绿叶菜", "控制主食比例"),
                    meal("info", "晚餐", "豆腐菌菇汤，凉拌蔬菜，少量杂粮", "高纤维低脂")
            )
    );

    private static final DietPlanRule GENERIC_RULE = new DietPlanRule(
            List.of(
                    target("酒精摄入", "0", "ml", "#f56c6c", "避免进一步增加肝脏负担"),
                    target("优质蛋白", "适量", "补充", "#409EFF", "结合肝功能状态个体化调整"),
                    target("新鲜蔬果", "每日", "摄入", "#67c23a", "补充维生素与膳食纤维")
            ),
            new DietApiDtos.Response.DietFoodsItem(
                    List.of("酒精", "霉变食物", "高油炸食品"),
                    List.of("高盐腌制品", "动物内脏", "甜饮料"),
                    List.of("全谷物", "新鲜蔬菜", "优质蛋白", "低脂奶制品")
            ),
            List.of(
                    meal("success", "早餐", "低脂牛奶，鸡蛋，全麦主食", "均衡能量供应"),
                    meal("warning", "午餐", "清蒸鱼，杂粮饭，绿叶蔬菜", "优质蛋白与低油烹调"),
                    meal("info", "晚餐", "豆腐蔬菜汤，少量主食", "清淡易消化")
            )
    );

    private final PatientRepository patientRepository;
    private final ClinicalHistoryEntryRepository clinicalHistoryEntryRepository;
    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final NotificationMessageRepository notificationMessageRepository;

    @Override
    public ApiResponse<DietApiDtos.Response.DietPatientsResponse> listDietPatients(Long tenantId, String keyword) {
        String normalizedKeyword = trimToNull(keyword);
        List<DietApiDtos.Response.DietPatientItem> items = patientRepository.listByTenantId(tenantId).stream()
                .filter(patient -> STATUS_ACTIVE.equalsIgnoreCase(patient.getStatus()))
                .filter(patient -> matchesKeyword(patient, normalizedKeyword))
                .sorted(Comparator.comparing(Patient::getPatientNo, Comparator.nullsLast(String::compareTo)))
                .map(patient -> toDietPatientItem(tenantId, patient))
                .toList();
        return ApiResponse.success(new DietApiDtos.Response.DietPatientsResponse(items));
    }

    @Override
    public ApiResponse<DietApiDtos.Response.DietPlanResponse> getDietPlan(Long tenantId, String patientNo) {
        Patient patient = requireActivePatient(tenantId, patientNo);
        PatientDietProfile profile = resolveDietProfile(tenantId, patient);
        DietPlanRule rule = ruleFor(profile.disease());
        return ApiResponse.success(toDietPlanResponse(rule, rule.mealPlan()));
    }

    @Override
    public ApiResponse<DietApiDtos.Response.RegenerateDietPlanResponse> regenerateDietPlan(Long tenantId, String patientNo) {
        Patient patient = requireActivePatient(tenantId, patientNo);
        PatientDietProfile profile = resolveDietProfile(tenantId, patient);
        DietPlanRule rule = ruleFor(profile.disease());
        List<DietApiDtos.Response.MealPlanItem> regenerated = rotatedMealPlan(rule.mealPlan(), patient.getPatientNo());
        return ApiResponse.success(new DietApiDtos.Response.RegenerateDietPlanResponse(regenerated, nowUtc()));
    }

    @Override
    public ApiResponse<DietApiDtos.Response.PushDietPlanResponse> pushDietPlan(Long tenantId, String patientNo) {
        Patient patient = requireActivePatient(tenantId, patientNo);
        OffsetDateTime deliveredAt = nowUtc();

        NotificationMessage message = new NotificationMessage();
        message.setTenantId(tenantId);
        message.setBizType("DIET_PLAN");
        message.setBizId(patient.getPatientNo());
        message.setReceiverType("PATIENT");
        message.setReceiverRefId(patient.getId());
        message.setTitle("个性化膳食处方已更新");
        message.setContent("您的个性化膳食处方已更新，请在患者端查看。");
        message.setChannel("APP");
        message.setStatus("SENT");
        message.setScheduledAt(deliveredAt);
        message.setSentAt(deliveredAt);
        message.setCreatedAt(deliveredAt);
        notificationMessageRepository.save(message);

        return ApiResponse.success(new DietApiDtos.Response.PushDietPlanResponse(true, patient.getPatientNo(), deliveredAt));
    }

    private DietApiDtos.Response.DietPatientItem toDietPatientItem(Long tenantId, Patient patient) {
        PatientDietProfile profile = resolveDietProfile(tenantId, patient);
        return new DietApiDtos.Response.DietPatientItem(
                patient.getPatientNo(),
                patient.getPatientName(),
                normalizeGender(patient.getGender()),
                ageOf(patient),
                "",
                profile.disease(),
                profile.compliance()
        );
    }

    private Patient requireActivePatient(Long tenantId, String patientNo) {
        return patientRepository.findByPatientNo(tenantId, patientNo)
                .filter(patient -> STATUS_ACTIVE.equalsIgnoreCase(patient.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "患者不存在"));
    }

    private PatientDietProfile resolveDietProfile(Long tenantId, Patient patient) {
        ClinicalDecision decision = latestClinicalDecision(tenantId, patient.getId()).orElse(ClinicalDecision.EMPTY);
        String disease = trimToNull(decision.diagnosis());
        if (disease == null) {
            disease = latestDiagnosisResult(tenantId, patient.getId())
                    .map(DiagnosisResult::getDiseaseName)
                    .map(this::trimToNull)
                    .orElse(DEFAULT_DISEASE);
        }
        String compliance = Optional.ofNullable(trimToNull(decision.compliance())).orElse(DEFAULT_COMPLIANCE);
        return new PatientDietProfile(disease, compliance);
    }

    private Optional<ClinicalDecision> latestClinicalDecision(Long tenantId, Long patientId) {
        return clinicalHistoryEntryRepository.listByPatientId(tenantId, patientId).stream()
                .filter(entry -> HISTORY_CLINICAL_DECISION.equalsIgnoreCase(entry.getHistoryType()))
                .sorted(Comparator.comparing(this::historySortTime).thenComparing(ClinicalHistoryEntry::getId).reversed())
                .map(this::toClinicalDecision)
                .filter(decision -> trimToNull(decision.diagnosis()) != null || trimToNull(decision.compliance()) != null)
                .findFirst();
    }

    private Optional<DiagnosisResult> latestDiagnosisResult(Long tenantId, Long patientId) {
        return diagnosisSessionRepository.listByPatientId(tenantId, patientId).stream()
                .sorted(Comparator.comparing(this::sessionSortTime).thenComparing(DiagnosisSession::getId).reversed())
                .flatMap(session -> diagnosisResultRepository.listBySessionId(tenantId, session.getId()).stream()
                        .sorted(Comparator
                                .comparing(DiagnosisResult::getRankNo, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(DiagnosisResult::getId, Comparator.nullsLast(Long::compareTo))))
                .findFirst();
    }

    private ClinicalDecision toClinicalDecision(ClinicalHistoryEntry entry) {
        JsonNode content = entry.getContentJson();
        if (content == null || !content.isObject()) {
            return ClinicalDecision.EMPTY;
        }
        return new ClinicalDecision(textField(content, "diagnosis"), textField(content, "compliance"));
    }

    private DietApiDtos.Response.DietPlanResponse toDietPlanResponse(DietPlanRule rule,
                                                                     List<DietApiDtos.Response.MealPlanItem> mealPlan) {
        return new DietApiDtos.Response.DietPlanResponse(rule.targets(), rule.foods(), mealPlan);
    }

    private DietPlanRule ruleFor(String disease) {
        String normalized = disease == null ? "" : disease.toLowerCase(Locale.ROOT);
        if (normalized.contains("wilson") || diseaseContains(disease, "肝豆")) {
            return WILSON_RULE;
        }
        if (diseaseContains(disease, "血色")) {
            return HEMOCHROMATOSIS_RULE;
        }
        if (normalized.contains("aat") || diseaseContains(disease, "抗胰蛋白酶")) {
            return AAT_RULE;
        }
        if (normalized.contains("masld") || diseaseContains(disease, "脂肪") || diseaseContains(disease, "代谢相关")) {
            return MASLD_RULE;
        }
        return GENERIC_RULE;
    }

    private boolean diseaseContains(String disease, String token) {
        return disease != null && disease.contains(token);
    }

    private List<DietApiDtos.Response.MealPlanItem> rotatedMealPlan(List<DietApiDtos.Response.MealPlanItem> mealPlan, String patientNo) {
        if (mealPlan.isEmpty()) {
            return List.of();
        }
        int offset = mealPlan.size() == 1
                ? 0
                : Math.floorMod(Optional.ofNullable(patientNo).orElse("").hashCode(), mealPlan.size() - 1) + 1;
        List<DietApiDtos.Response.MealPlanItem> rotated = new ArrayList<>(mealPlan.size());
        for (int i = 0; i < mealPlan.size(); i++) {
            DietApiDtos.Response.MealPlanItem source = mealPlan.get((i + offset) % mealPlan.size());
            rotated.add(new DietApiDtos.Response.MealPlanItem(
                    source.type(),
                    source.time(),
                    source.menu(),
                    source.nutrition() + " · 方案版本 " + (i + 1)
            ));
        }
        return rotated;
    }

    private boolean matchesKeyword(Patient patient, String keyword) {
        if (keyword == null) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return contains(patient.getPatientNo(), normalized) || contains(patient.getPatientName(), normalized);
    }

    private boolean contains(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private int ageOf(Patient patient) {
        LocalDate birthDate = patient.getBirthDate();
        if (birthDate == null) {
            return 0;
        }
        return Math.max(0, (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now(ZoneOffset.UTC)));
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return "--";
        }
        String normalized = gender.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("male") || normalized.equals("m")) {
            return "男";
        }
        if (normalized.equals("female") || normalized.equals("f")) {
            return "女";
        }
        return gender.trim();
    }

    private OffsetDateTime historySortTime(ClinicalHistoryEntry entry) {
        if (entry.getRecordedAt() != null) {
            return entry.getRecordedAt();
        }
        if (entry.getCreatedAt() != null) {
            return entry.getCreatedAt();
        }
        return OffsetDateTime.MIN;
    }

    private OffsetDateTime sessionSortTime(DiagnosisSession session) {
        if (session.getCompletedAt() != null) {
            return session.getCompletedAt();
        }
        if (session.getStartedAt() != null) {
            return session.getStartedAt();
        }
        if (session.getCreatedAt() != null) {
            return session.getCreatedAt();
        }
        return OffsetDateTime.MIN;
    }

    private String textField(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText(null);
        return trimToNull(text);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private static DietApiDtos.Response.DietTargetItem target(String label, String value, String unit, String color, String desc) {
        return new DietApiDtos.Response.DietTargetItem(label, value, unit, color, desc);
    }

    private static DietApiDtos.Response.MealPlanItem meal(String type, String time, String menu, String nutrition) {
        return new DietApiDtos.Response.MealPlanItem(type, time, menu, nutrition);
    }

    private record DietPlanRule(
            List<DietApiDtos.Response.DietTargetItem> targets,
            DietApiDtos.Response.DietFoodsItem foods,
            List<DietApiDtos.Response.MealPlanItem> mealPlan
    ) {
    }

    private record ClinicalDecision(String diagnosis, String compliance) {
        private static final ClinicalDecision EMPTY = new ClinicalDecision(null, null);
    }

    private record PatientDietProfile(String disease, String compliance) {
    }
}
