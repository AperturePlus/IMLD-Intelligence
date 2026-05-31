<template>
  <div class="record-view" v-loading="loading">
    <div class="view-header">
      <el-page-header :content="headerContent" @back="goBack" />
    </div>

    <el-empty v-if="!loading && !record" description="该患者暂无电子病历" />

    <template v-else-if="record">
      <el-card shadow="never" class="section">
        <template #header><span class="section-title">基础档案</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="病人ID号">{{ display(record.patientNo) }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ display(record.name) }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ display(record.gender) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ display(record.age) }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ display(record.visitDate) }}</el-descriptions-item>
          <el-descriptions-item label="就诊方式">{{ encounterLabel(record.encounterType) }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ display(record.department) }}</el-descriptions-item>
          <el-descriptions-item label="职业">{{ display(record.occupation) }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ display(record.phone) }}</el-descriptions-item>
          <el-descriptions-item label="现住址" :span="2">{{ display(record.currentAddress) }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ display(record.nativePlace) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">主诉与现病史</span></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="主诉">{{ display(record.chiefComplaint) }}</el-descriptions-item>
          <el-descriptions-item label="现病史">{{ display(record.presentIllness) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">既往史</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="吸烟史">{{ flagLabel(record.history.diseaseHistory.smokingHistory) }}</el-descriptions-item>
          <el-descriptions-item label="饮酒史">{{ flagLabel(record.history.diseaseHistory.drinkingHistory) }}</el-descriptions-item>
          <el-descriptions-item label="糖尿病史">{{ flagLabel(record.history.diseaseHistory.diabetesHistory) }}</el-descriptions-item>
          <el-descriptions-item label="高血压史">{{ flagLabel(record.history.diseaseHistory.hypertensionHistory) }}</el-descriptions-item>
          <el-descriptions-item label="高尿酸血症史">{{ flagLabel(record.history.diseaseHistory.hyperuricemiaHistory) }}</el-descriptions-item>
          <el-descriptions-item label="高脂血症史">{{ flagLabel(record.history.diseaseHistory.hyperlipidemiaHistory) }}</el-descriptions-item>
          <el-descriptions-item label="冠心病史">{{ flagLabel(record.history.diseaseHistory.coronaryHeartDiseaseHistory) }}</el-descriptions-item>
          <el-descriptions-item label="乙肝病史">{{ flagLabel(record.history.diseaseHistory.hepatitisBHistory) }}</el-descriptions-item>
          <el-descriptions-item label="手术史">{{ conditionalLabel(record.history.surgeryHistory) }}</el-descriptions-item>
          <el-descriptions-item label="输血史">{{ conditionalLabel(record.history.transfusionHistory) }}</el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="3">{{ display(record.history.allergyHistory) }}</el-descriptions-item>
          <el-descriptions-item label="用药史" :span="3">{{ display(record.history.medicationHistory) }}</el-descriptions-item>
          <el-descriptions-item label="家族史" :span="3">{{ display(record.history.familyHistory) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">体格检查</span></template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="身高(cm)">{{ display(record.physicalExam.heightCm) }}</el-descriptions-item>
          <el-descriptions-item label="体重(kg)">{{ display(record.physicalExam.weightKg) }}</el-descriptions-item>
          <el-descriptions-item label="BMI">{{ display(record.physicalExam.bmi) }}</el-descriptions-item>
          <el-descriptions-item label="收缩压">{{ display(record.physicalExam.bloodPressureSystolic) }}</el-descriptions-item>
          <el-descriptions-item label="舒张压">{{ display(record.physicalExam.bloodPressureDiastolic) }}</el-descriptions-item>
          <el-descriptions-item label="心率">{{ display(record.physicalExam.heartRate) }}</el-descriptions-item>
          <el-descriptions-item label="肝纤维化">{{ flagLabel(record.physicalExam.liverFibrosis) }}</el-descriptions-item>
          <el-descriptions-item label="肝硬化">{{ flagLabel(record.physicalExam.cirrhosis) }}</el-descriptions-item>
          <el-descriptions-item label="脂肪肝">{{ flagLabel(record.physicalExam.fattyLiver) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">实验室检查</span></template>
        <template v-for="group in labConfig" :key="group.key">
          <div class="lab-group-title">{{ group.title }}</div>
          <template v-for="sec in group.sections" :key="sec.key">
            <div v-if="sec.items.length" class="lab-section-title">{{ sec.title }}</div>
            <el-descriptions v-if="sec.items.length" :column="4" border size="small" class="lab-desc">
              <el-descriptions-item
                v-for="item in sec.items"
                :key="item.key"
                :label="item.unit ? `${item.label}(${item.unit})` : item.label"
              >
                {{ labValue(group.key, sec.key, item.key) }}
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </template>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">病理 / 影像 / 基因</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="肝穿刺活检">{{ record.pathology.performed ? '已执行' : '未执行' }}</el-descriptions-item>
          <el-descriptions-item label="NAS 评分">{{ display(record.pathology.nasScore) }}</el-descriptions-item>
          <el-descriptions-item label="病理描述" :span="2">{{ display(record.pathology.reportText) }}</el-descriptions-item>
          <el-descriptions-item label="影像报告" :span="2">{{ imagingText }}</el-descriptions-item>
          <el-descriptions-item label="基因检测" :span="2">{{ geneticText }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="section">
        <template #header><span class="section-title">临床决策</span></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="初步诊断">{{ display(record.clinicalDecision.diagnosis) }}</el-descriptions-item>
          <el-descriptions-item label="干预计划">{{ display(record.clinicalDecision.treatmentPlan) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import patientApi from '@/api/patient'
import type {
  EncounterType,
  PatientRecordConditionalHistory,
  PatientRecordPayload,
  TernaryFlag
} from '@/types/patient'
import { LABORATORY_SCREENING_CONFIG } from '@/features/patient-record/constants/laboratoryScreening'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const record = ref<PatientRecordPayload | null>(null)
const labConfig = LABORATORY_SCREENING_CONFIG

const patientNo = computed(() => String(route.params.patientNo || ''))
const headerContent = computed(() =>
  record.value ? `电子病历 - ${record.value.name}（${patientNo.value}）` : `电子病历 - ${patientNo.value}`
)

const display = (value: unknown): string => {
  if (value === null || value === undefined || value === '') {
    return '—'
  }
  return String(value)
}

const FLAG_LABELS: Record<TernaryFlag, string> = { YES: '有', NO: '无', UNKNOWN: '未查' }
const flagLabel = (value: TernaryFlag): string => FLAG_LABELS[value] || '未查'

const ENCOUNTER_LABELS: Record<EncounterType, string> = {
  OUTPATIENT: '门诊',
  EMERGENCY: '急诊',
  INPATIENT: '住院'
}
const encounterLabel = (value: EncounterType): string => ENCOUNTER_LABELS[value] || display(value)

const conditionalLabel = (value: PatientRecordConditionalHistory): string => {
  if (value.status !== 'YES') {
    return flagLabel(value.status)
  }
  return value.detail ? `有：${value.detail}` : '有'
}

const labValue = (groupKey: string, sectionKey: string, fieldKey: string): string => {
  const screening = record.value?.laboratoryScreening as unknown as
    | Record<string, Record<string, Record<string, string>>>
    | undefined
  return display(screening?.[groupKey]?.[sectionKey]?.[fieldKey])
}

const imagingText = computed(() => {
  const reports = record.value?.imagingReports || []
  if (!reports.length) {
    return '—'
  }
  return reports.map((item) => `【${item.modality}】${item.reportText}`).join('；')
})

const geneticText = computed(() => {
  const g = record.value?.geneticSequencing
  if (!g || !g.tested) {
    return '未检测'
  }
  const variants = (g.variants || []).map((v) => v.gene).filter(Boolean).join('、')
  return `${g.method || ''} ${g.conclusion || ''}${variants ? `（${variants}）` : ''}`.trim() || '已检测'
})

const goBack = (): void => {
  router.push({ name: 'patient-list' })
}

const fetchRecord = async (): Promise<void> => {
  if (!patientNo.value) {
    return
  }
  loading.value = true
  try {
    const res = await patientApi.getRecord(patientNo.value)
    record.value = res.data
  } catch {
    record.value = null
    ElMessage.warning('未找到该患者的电子病历')
  } finally {
    loading.value = false
  }
}

onMounted(fetchRecord)
</script>

<style scoped>
.record-view {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

.view-header {
  margin-bottom: 16px;
}

.section {
  margin-bottom: 20px;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
  font-weight: 700;
  color: #303133;
}

.lab-group-title {
  font-weight: 700;
  color: #409eff;
  margin: 8px 0;
}

.lab-section-title {
  color: #606266;
  margin: 10px 0 6px;
}

.lab-desc {
  margin-bottom: 12px;
}
</style>
