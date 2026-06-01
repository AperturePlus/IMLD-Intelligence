<template>
  <div class="ai-diagnosis-container" v-loading="loadingQueue">
    <el-row :gutter="20" class="full-height">
      
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <el-text tag="b" size="large">AI 诊断队列</el-text>
              <el-tag size="small" type="info">{{ patients.length }} 人</el-tag>
            </div>
          </template>

          <el-tabs v-model="queueTab" class="queue-tabs">
            <el-tab-pane name="pending">
              <template #label>
                <span class="queue-tab-label">待诊 <span>{{ pendingPatients.length }}</span></span>
              </template>
            </el-tab-pane>
            <el-tab-pane name="reported">
              <template #label>
                <span class="queue-tab-label">已出报告 <span>{{ reportedPatients.length }}</span></span>
              </template>
            </el-tab-pane>
          </el-tabs>
          
          <el-scrollbar class="queue-scrollbar">
            <div 
              v-for="patient in visiblePatients"
              :key="patient.id"
              class="patient-list-item"
              :class="{ 'is-active': selectedPatient?.id === patient.id }"
              @click="handleSelectPatient(patient)"
            >
              <PatientAvatar :size="46" :src="patient.avatar" :name="patient.name" />
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
            <el-empty
              v-if="visiblePatients.length === 0"
              :description="queueTab === 'pending' ? '暂无待诊患者' : '暂无已出报告患者'"
              :image-size="90"
            />
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
            <PatientAvatar
              :size="80"
              :src="selectedPatient.avatar"
              :name="selectedPatient.name"
              style="margin-bottom: 20px;"
            />
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
            <PatientAvatar
              :size="80"
              :src="selectedPatient.avatar"
              :name="selectedPatient.name"
              style="margin-bottom: 20px;"
            />
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
                    :color="riskBand.color"
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
                  <el-descriptions-item v-if="diagnosisResult.confidence.visible" label="数据置信度">
                    <el-tag :type="confidenceTagType" size="small">{{ diagnosisResult.confidence.label }}</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="关键证据" :span="2">
                    <div class="key-sign-list">
                      <el-tag
                        v-for="sign in diagnosisResult.keySigns"
                        :key="sign"
                        type="warning"
                        effect="plain"
                        size="small"
                      >
                        {{ sign }}
                      </el-tag>
                      <span v-if="diagnosisResult.keySigns.length === 0">--</span>
                    </div>
                  </el-descriptions-item>
                </el-descriptions>
                <div class="evidence-summary">
                  <el-tag type="info" effect="plain">纳入模型特征 {{ diagnosisResult.evidenceSummary.modelFeatureCount }} 项</el-tag>
                  <el-tag type="warning" effect="plain">异常证据 {{ diagnosisResult.evidenceSummary.abnormalEvidenceCount }} 项</el-tag>
                  <el-tag :type="reviewTagType" effect="plain">
                    {{ diagnosisResult.evidenceSummary.reviewRequired ? '需医生复核' : '置信度达标' }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>

            <div v-if="diagnosisResult.evidenceItems.length > 0" class="section-card evidence-card">
              <div class="section-title">关键证据汇总</div>
              <div class="evidence-grid">
                <div
                  v-for="item in diagnosisResult.evidenceItems"
                  :key="`${item.category}-${item.label}-${item.value || ''}`"
                  class="evidence-item"
                  :class="item.severity"
                >
                  <div class="evidence-item-top">
                    <span class="evidence-category">{{ item.category }}</span>
                    <span v-if="item.source" class="evidence-source">{{ item.source }}</span>
                  </div>
                  <div class="evidence-label">{{ item.label }}</div>
                  <div v-if="item.value" class="evidence-value">{{ item.value }}</div>
                </div>
              </div>
            </div>

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
                <el-empty v-if="diagnosisResult.indicators.length === 0" description="暂无结构化生化指标" :image-size="80" />
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
import PatientAvatar from '@/components/PatientAvatar.vue'
import diagnosisApi from '../../api/diagnosis'
import type { DiagnosisQueuePatient, DiagnosisResult } from '../../api/types'

const DIAGNOSED_STATUS = '已诊断'

const isDiagnosing = ref(false)
const isLoadingReportedResult = ref(false)
const loadingQueue = ref(false)
const selectedPatient = ref<DiagnosisQueuePatient | null>(null)
const diagnosisResult = ref<DiagnosisResult | null>(null)
const patients = ref<DiagnosisQueuePatient[]>([])
const queueTab = ref<'pending' | 'reported'>('pending')
const diagnosedStatus = DIAGNOSED_STATUS

const isBusy = computed(() => isDiagnosing.value || isLoadingReportedResult.value)

const loadingText = computed(() => {
  if (isDiagnosing.value) {
    return 'IMLD AI 正在解析临床表型与数据，请稍候...'
  }
  return '正在调取该患者的历史 AI 报告，请稍候...'
})

const RISK_BANDS = {
  高: { label: '高风险', color: '#f56c6c' },
  中: { label: '中风险', color: '#e6a23c' },
  低: { label: '低风险', color: '#67c23a' }
} as const

const riskBand = computed(() => {
  const level = diagnosisResult.value?.riskLevel ?? '中'
  return RISK_BANDS[level] ?? RISK_BANDS.中
})

const confidenceTagType = computed(() => {
  const confidence = diagnosisResult.value?.confidence
  if (!confidence) {
    return 'info'
  }
  if (confidence.adjusted || confidence.reviewRequired) {
    return 'warning'
  }
  return 'success'
})

const reviewTagType = computed(() => {
  return diagnosisResult.value?.evidenceSummary.reviewRequired ? 'warning' : 'success'
})

const pendingPatients = computed(() => patients.value.filter((item) => item.aiStatus !== DIAGNOSED_STATUS))

const reportedPatients = computed(() => patients.value.filter((item) => item.aiStatus === DIAGNOSED_STATUS))

const visiblePatients = computed(() => {
  return queueTab.value === 'reported' ? reportedPatients.value : pendingPatients.value
})

const syncSelectedPatientFromQueue = () => {
  if (!selectedPatient.value) {
    return
  }
  selectedPatient.value = patients.value.find((item) => item.id === selectedPatient.value?.id) || null
}

const loadReportedDiagnosis = async (patient: DiagnosisQueuePatient) => {
  isLoadingReportedResult.value = true
  try {
    const res = await diagnosisApi.getLatestDiagnosisResultByPatient(patient.id)
    if (!res.data) {
      await fetchQueue()
      queueTab.value = 'pending'
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
    patients.value = res.data.items || []
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
    await fetchQueue()
    queueTab.value = 'reported'
    ElMessage.success('AI 辅助诊断已完成，患者已移入已出报告')
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
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-panel :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  min-height: 0;
}

.queue-tabs {
  flex-shrink: 0;
  padding: 0 16px;
}

.queue-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.queue-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.queue-tab-label span {
  display: inline-flex;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #f0f2f5;
  color: #606266;
  font-size: 12px;
  line-height: 20px;
  justify-content: center;
}

.queue-scrollbar {
  flex: 1;
  min-height: 0;
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
  flex: 1;
  overflow: auto;
  box-sizing: border-box;
  padding: 24px;
  min-height: 0;
  min-width: 0;
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

.key-sign-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  line-height: 1.5;
}

.evidence-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
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

.evidence-card {
  margin-bottom: 20px;
}

.evidence-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.evidence-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.evidence-item.warning {
  border-color: #f3d19e;
  background: #fdf6ec;
}

.evidence-item.exception {
  border-color: #fab6b6;
  background: #fef0f0;
}

.evidence-item-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.evidence-category {
  font-size: 12px;
  font-weight: 700;
  color: #606266;
}

.evidence-source {
  font-size: 12px;
  color: #909399;
}

.evidence-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
}

.evidence-value {
  margin-top: 6px;
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
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

