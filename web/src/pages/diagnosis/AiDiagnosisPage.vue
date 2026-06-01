<template>
  <div class="ai-diagnosis-container" v-loading="loadingQueue">
    <el-row :gutter="20" class="full-height">
      
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <el-text tag="b" size="large">待诊患者队列</el-text>
              <el-tag size="small" type="info">{{ patients.length }} 人</el-tag>
            </div>
          </template>
          
          <el-scrollbar height="calc(100vh - 160px)">
            <div 
              v-for="patient in patients" 
              :key="patient.id"
              class="patient-list-item"
              :class="{ 'is-active': selectedPatient?.id === patient.id }"
              @click="handleSelectPatient(patient)"
            >
              <el-avatar :size="46" :src="patient.avatar" />
              <div class="item-info">
                <div class="item-header">
                  <div class="patient-identity">
                    <span class="patient-no-label">病号</span>
                    <span class="patient-no-value">{{ patient.id }}</span>
                    <span class="patient-name">{{ patient.name }}</span>
                  </div>
                  <el-tag 
                    v-if="patient.aiStatus === '已诊断'" 
                    size="small" type="success" effect="dark" round
                  >已出报告</el-tag>
                </div>
                <div class="item-sub">
                  {{ patient.gender }} | {{ patient.age }} 岁
                </div>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18" class="full-height">
        <el-card 
          class="right-panel" 
          shadow="never"
          v-loading="isBusy"
          :element-loading-text="loadingText"
          element-loading-background="rgba(255, 255, 255, 0.9)"
        >
          
          <div v-if="!selectedPatient" class="empty-state">
            <el-empty description="请从左侧列表选择一位患者进行 AI 辅助诊断" />
          </div>

          <div
            v-else-if="selectedPatient && !diagnosisResult && selectedPatient.aiStatus === diagnosedStatus"
            class="ready-state"
          >
            <el-avatar :size="80" :src="selectedPatient.avatar" style="margin-bottom: 20px;" />
            <el-text size="large" tag="b" style="font-size: 22px; display: block; margin-bottom: 12px;">
              {{ selectedPatient.name }} 已完成 AI 报告
            </el-text>
            <el-text type="info" style="margin-bottom: 30px; display: block;">
              已出报告患者无需再次启动 AI，系统将直接展示历史报告。
            </el-text>
            <el-button type="primary" plain size="large" :icon="Download" @click="loadSelectedPatientReport">
              重新调取已出报告
            </el-button>
          </div>

          <div v-else-if="selectedPatient && !diagnosisResult" class="ready-state">
            <el-avatar :size="80" :src="selectedPatient.avatar" style="margin-bottom: 20px;" />
            <el-text size="large" tag="b" style="font-size: 22px; display: block; margin-bottom: 12px;">
              {{ selectedPatient.name }} 的诊疗档案已就绪
            </el-text>
            <el-text type="info" style="margin-bottom: 30px; display: block;">
              系统已提取该患者的基础档案、生化指标及临床表征数据。
            </el-text>
            <el-button
              type="primary"
              size="large"
              :icon="Cpu"
              class="pulsing-btn"
              :disabled="isBusy"
              @click="startDiagnosis"
            >
              启动 AI 智能筛查
            </el-button>
          </div>

          <div v-else-if="diagnosisResult" class="result-state">
            <div class="result-header">
              <div class="title-area">
                <el-icon color="#409EFF" :size="28" style="margin-right: 12px;"><Aim /></el-icon>
                <span class="report-title">AI 辅助诊断评估报告</span>
              </div>
              <el-button type="primary" plain :icon="Download">导出报告 PDF</el-button>
            </div>

            <el-row :gutter="24" class="core-conclusion">
              <el-col :span="8" style="text-align: center; border-right: 1px solid #ebeef5;">
                <el-text type="info">AI 预测患病概率</el-text>
                <div style="margin-top: 16px;">
                  <el-progress 
                    type="dashboard" 
                    :percentage="diagnosisResult.probability" 
                    :color="customColors" 
                    :width="140"
                    :stroke-width="12"
                  >
                    <template #default="{ percentage }">
                      <span class="percentage-value" :style="{ color: riskBand.color }">{{ percentage }}%</span>
                      <br>
                      <span class="percentage-label">{{ riskBand.label }}</span>
                    </template>
                  </el-progress>
                </div>
              </el-col>
              <el-col :span="16" style="padding-left: 30px;">
                <el-text type="info">疑似疾病指向</el-text>
                <h2 class="disease-name">{{ diagnosisResult.diseaseName }}</h2>
                <el-descriptions :column="2" border size="small" style="margin-top: 16px;">
                  <el-descriptions-item label="鉴别诊断">
                    {{ diagnosisResult.differentials.join('、') || '--' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="数据置信度">
                    <el-tag type="success" size="small">{{ diagnosisResult.dataConfidenceLabel }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="关键体征">
                    {{ diagnosisResult.keySigns.join('、') || '--' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>

            <div class="section-card">
              <div class="section-title">特征性生化指标偏离分析</div>
              <div class="chart-container">
                <div class="chart-row" v-for="item in diagnosisResult.indicators" :key="item.name">
                  <div class="chart-label">{{ item.name }}</div>
                  <div class="chart-bar-area">
                    <el-progress 
                      :percentage="item.percentage" 
                      :status="item.status" 
                      :stroke-width="14" 
                      :show-text="false"
                    />
                  </div>
                  <div class="chart-value" :class="item.status">
                    {{ item.value }} {{ item.unit }}
                    <span class="ref-range">(参考: {{ item.normal }})</span>
                  </div>
                </div>
              </div>
            </div>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <div class="section-card suggestion-card">
                  <div class="section-title">
                    <el-icon><Microphone /></el-icon> 靶向基因变异预测
                  </div>
                  <div class="gene-tags">
                    <el-tag v-for="gene in diagnosisResult.genes" :key="gene" type="danger" effect="light" class="gene-tag">
                      {{ gene }}
                    </el-tag>
                    <el-text v-if="diagnosisResult.genes.length === 0" type="info" size="small">
                      暂无明确高风险基因变异
                    </el-text>
                  </div>
                  <el-alert
                    :title="diagnosisResult.geneRecommendationTitle"
                    type="warning"
                    :description="diagnosisResult.sequencing"
                    show-icon
                    :closable="false"
                    style="margin-top: 16px;"
                  />
                </div>
              </el-col>
              
              <el-col :span="12">
                <div class="section-card suggestion-card diet-card">
                  <div class="section-title">
                    <el-icon><Food /></el-icon> 专病膳食与干预建议
                  </div>
                  <p class="diet-text">{{ diagnosisResult.diet }}</p>
                  <div style="margin-top: 12px;">
                    <el-tag
                      v-for="tag in diagnosisResult.dietTags"
                      :key="tag"
                      type="info"
                      effect="plain"
                      style="margin-right: 8px; margin-bottom: 8px;"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </el-col>
            </el-row>

          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { Cpu, Aim, Download, Microphone, Food } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import diagnosisApi from '../../api/diagnosis'
import type { DiagnosisQueuePatient, DiagnosisResult } from '../../api/types'

const DIAGNOSED_STATUS = '已诊断'
const UNDIAGNOSED_STATUS = '未诊断'
const MAX_REPORTED_PATIENT_COUNT = 3

const isDiagnosing = ref(false)
const isLoadingReportedResult = ref(false)
const loadingQueue = ref(false)
const selectedPatient = ref<DiagnosisQueuePatient | null>(null)
const diagnosisResult = ref<DiagnosisResult | null>(null)
const patients = ref<DiagnosisQueuePatient[]>([])
const seedReportedPatientIds = ref<string[]>([])
const diagnosedStatus = DIAGNOSED_STATUS

const isBusy = computed(() => isDiagnosing.value || isLoadingReportedResult.value)

const loadingText = computed(() => {
  if (isDiagnosing.value) {
    return 'IMLD AI 正在解析临床表型与数据，请稍候...'
  }
  return '正在调取该患者的历史 AI 报告，请稍候...'
})

const customColors = [
  { color: '#67c23a', percentage: 30 },
  { color: '#e6a23c', percentage: 70 },
  { color: '#f56c6c', percentage: 100 }
]

const RISK_BANDS = [
  { min: 85, label: '极高危', color: '#f56c6c' },
  { min: 70, label: '高危', color: '#f56c6c' },
  { min: 30, label: '中危', color: '#e6a23c' },
  { min: 0, label: '低危', color: '#67c23a' }
]

const riskBand = computed(() => {
  const probability = diagnosisResult.value?.probability ?? 0
  return RISK_BANDS.find((band) => probability >= band.min) ?? RISK_BANDS[RISK_BANDS.length - 1]
})

const resolveSeedReportedPatientIds = (items: DiagnosisQueuePatient[]): string[] => {
  return items
    .filter((item) => item.aiStatus === DIAGNOSED_STATUS)
    .slice(0, MAX_REPORTED_PATIENT_COUNT)
    .map((item) => item.id)
}

const applySeedStatus = (items: DiagnosisQueuePatient[]): DiagnosisQueuePatient[] => {
  if (seedReportedPatientIds.value.length === 0) {
    seedReportedPatientIds.value = resolveSeedReportedPatientIds(items)
  }
  if (seedReportedPatientIds.value.length === 0) {
    return items
  }

  const reportedSet = new Set(seedReportedPatientIds.value)
  return items.map((item) => ({
    ...item,
    aiStatus: reportedSet.has(item.id) ? DIAGNOSED_STATUS : UNDIAGNOSED_STATUS
  }))
}

const syncSelectedPatientFromQueue = () => {
  if (!selectedPatient.value) {
    return
  }
  selectedPatient.value = patients.value.find((item) => item.id === selectedPatient.value?.id) || null
}

const resetMockQueueStatuses = () => {
  patients.value = applySeedStatus(patients.value)
  syncSelectedPatientFromQueue()
}

const loadReportedDiagnosis = async (patient: DiagnosisQueuePatient) => {
  isLoadingReportedResult.value = true
  try {
    const res = await diagnosisApi.getLatestDiagnosisResultByPatient(patient.id)
    if (!res.data) {
      seedReportedPatientIds.value = seedReportedPatientIds.value.filter((id) => id !== patient.id)
      resetMockQueueStatuses()
      ElMessage.warning('该患者暂无已出报告，请点击“启动 AI 智能筛查”')
      return
    }
    diagnosisResult.value = res.data
  } catch {
    ElMessage.error('加载历史报告失败，请稍后重试')
  } finally {
    isLoadingReportedResult.value = false
  }
}

const loadSelectedPatientReport = async () => {
  const patient = selectedPatient.value
  if (!patient || isBusy.value) {
    return
  }
  await loadReportedDiagnosis(patient)
}

const fetchQueue = async () => {
  loadingQueue.value = true
  try {
    const res = await diagnosisApi.getAiQueue()
    patients.value = applySeedStatus(res.data.items || [])
    syncSelectedPatientFromQueue()
  } catch {
    ElMessage.error('加载待诊队列失败，请稍后重试')
  } finally {
    loadingQueue.value = false
  }
}

const handleSelectPatient = async (patient: DiagnosisQueuePatient) => {
  if (isBusy.value) {
    ElMessage.warning('当前正在处理任务，请稍后再切换患者')
    return
  }

  selectedPatient.value = patient
  diagnosisResult.value = null

  if (patient.aiStatus === DIAGNOSED_STATUS) {
    await loadReportedDiagnosis(patient)
  }
}

const startDiagnosis = async () => {
  const patient = selectedPatient.value
  if (!patient || isBusy.value) {
    return
  }

  if (patient.aiStatus === DIAGNOSED_STATUS) {
    await loadReportedDiagnosis(patient)
    return
  }

  isDiagnosing.value = true
  try {
    const res = await diagnosisApi.runAiDiagnosis(patient.id)
    diagnosisResult.value = res.data
    resetMockQueueStatuses()
    ElMessage.success('AI 辅助诊断已完成，患者队列状态已重置')
  } catch {
    ElMessage.error('AI 诊断失败，请稍后重试')
  } finally {
    isDiagnosing.value = false
  }
}

onMounted(() => {
  fetchQueue()
})
</script>

<style scoped>
.ai-diagnosis-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

/* 左侧患者列表样式 */
.left-panel, .right-panel {
  height: 100%;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.patient-list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.patient-list-item:hover {
  background-color: #f0f7ff;
}

.patient-list-item.is-active {
  background-color: #ecf5ff;
  border-left: 4px solid #409EFF;
}

.item-info {
  margin-left: 12px;
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.patient-identity {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.patient-no-label {
  font-size: 12px;
  color: #909399;
  letter-spacing: 1px;
}

.patient-no-value {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}

.patient-name {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-sub {
  font-size: 13px;
  color: #909399;
}

/* 右侧工作台样式 */
:deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  padding: 24px;
}

.empty-state, .ready-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

/* 呼吸灯效果按钮 */
.pulsing-btn {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

/* 诊断结果页样式 */
.result-state {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.report-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.title-area {
  display: flex;
  align-items: center;
}

.core-conclusion {
  background: #fafafa;
  padding: 24px 0;
  border-radius: 8px;
  margin-bottom: 24px;
}

.percentage-value {
  font-size: 28px;
  font-weight: bold;
}

.percentage-label {
  font-size: 14px;
  color: #909399;
}

.disease-name {
  margin: 0 0 16px 0;
  color: #f56c6c;
  font-size: 24px;
}

/* 内部卡片样式 */
.section-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
}

.suggestion-card {
  height: 100%;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 纯 CSS 图表样式 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-row {
  display: flex;
  align-items: center;
}

.chart-label {
  width: 150px;
  font-size: 14px;
  color: #606266;
  text-align: right;
  padding-right: 16px;
}

.chart-bar-area {
  flex: 1;
  padding-right: 16px;
}

.chart-value {
  width: 180px;
  font-size: 14px;
  font-weight: bold;
}

.chart-value.exception { color: #f56c6c; }
.chart-value.warning { color: #e6a23c; }

.ref-range {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 6px;
}

/* 基因与膳食样式 */
.gene-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.gene-tag {
  font-size: 14px;
  padding: 6px 12px;
  height: auto;
}

.diet-card {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border: none;
}

.diet-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}
</style>

