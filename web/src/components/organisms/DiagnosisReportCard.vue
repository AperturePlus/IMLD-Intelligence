<template>
  <div class="diagnosis-report-card">
    <div class="report-header">
      <BaseIcon name="activity" :size="20" />
      <span class="report-title">AI 辅助诊断评估报告</span>
      <BaseButton variant="ghost" size="sm">导出 PDF</BaseButton>
    </div>

    <div class="report-core">
      <div class="report-probability">
        <el-progress
          type="dashboard"
          :percentage="props.result.probability"
          :color="riskColor"
          :width="140"
          :stroke-width="12"
        >
          <template #default="{ percentage }">
            <span class="percentage-value" :style="{ color: riskColor }">{{ percentage }}%</span>
            <br>
            <span class="percentage-label">{{ riskLabel }}</span>
          </template>
        </el-progress>
      </div>

      <div class="report-details">
        <div class="report-disease">{{ props.result.diseaseName }}</div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="鉴别诊断">
            {{ props.result.differentials.join('、') || '--' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="props.result.confidence.visible" label="数据置信度">
            <BaseTag :type="confidenceTagType" size="sm">{{ props.result.confidence.label }}</BaseTag>
          </el-descriptions-item>
          <el-descriptions-item label="关键证据" :span="2">
            <div class="key-sign-list">
              <BaseTag
                v-for="sign in props.result.keySigns"
                :key="sign"
                type="warning"
                effect="plain"
                size="sm"
              >
                {{ sign }}
              </BaseTag>
              <span v-if="props.result.keySigns.length === 0">--</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="evidence-summary">
          <BaseTag type="info" effect="plain" size="sm">
            纳入模型特征 {{ props.result.evidenceSummary.modelFeatureCount }} 项
          </BaseTag>
          <BaseTag type="warning" effect="plain" size="sm">
            异常证据 {{ props.result.evidenceSummary.abnormalEvidenceCount }} 项
          </BaseTag>
          <BaseTag :type="reviewTagType" effect="plain" size="sm">
            {{ props.result.evidenceSummary.reviewRequired ? '需医生复核' : '置信度达标' }}
          </BaseTag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '@/components/atoms/BaseIcon.vue'
import BaseButton from '@/components/atoms/BaseButton.vue'
import BaseTag from '@/components/atoms/BaseTag.vue'
import type { DiagnosisReportProps } from './DiagnosisReportCard.types'

const props = defineProps<DiagnosisReportProps>()

const riskColor = computed(() => {
  const level = props.result.riskLevel
  if (level === '高') return 'var(--imld-risk-high)'
  if (level === '中') return 'var(--imld-risk-mid)'
  return 'var(--imld-risk-low)'
})

const riskLabel = computed(() => {
  const level = props.result.riskLevel
  if (level === '高') return '高风险'
  if (level === '中') return '中风险'
  return '低风险'
})

const confidenceTagType = computed(() => {
  const c = props.result.confidence
  if (!c) return 'info'
  if (c.adjusted || c.reviewRequired) return 'warning'
  return 'success'
})

const reviewTagType = computed(() =>
  props.result.evidenceSummary.reviewRequired ? 'warning' : 'success'
)
</script>

<style scoped>
.diagnosis-report-card {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-4);
  padding: var(--imld-sp-5);
  border-radius: var(--imld-radius-md);
  background: var(--imld-card);
  box-shadow: var(--imld-shadow-card);
}
.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--imld-sp-3);
  padding-bottom: var(--imld-sp-3);
  border-bottom: 1px solid var(--imld-border);
}
.report-title {
  flex: 1;
  font-size: 18px;
  font-weight: 700;
  color: var(--imld-text);
}
.report-core {
  display: flex;
  gap: var(--imld-sp-5);
  flex-wrap: wrap;
}
.report-probability {
  text-align: center;
  flex-shrink: 0;
}
.percentage-value {
  font-size: 28px;
  font-weight: 700;
}
.percentage-label {
  font-size: 14px;
  color: var(--imld-muted);
}
.report-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
  min-width: 280px;
}
.report-disease {
  margin: 0 0 var(--imld-sp-2) 0;
  color: var(--imld-danger);
  font-size: 24px;
  font-weight: 700;
}
.key-sign-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
  line-height: 1.5;
}
.evidence-summary {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
  margin-top: var(--imld-sp-2);
}
</style>
