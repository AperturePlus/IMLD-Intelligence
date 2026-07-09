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
            <br />
            <span class="percentage-label">{{ riskLabel }}</span>
          </template>
        </el-progress>
      </div>

      <div class="report-details">
        <div class="report-disease">{{ props.result.diseaseName }}</div>
        <dl class="report-meta">
          <div class="meta-row">
            <dt>鉴别诊断</dt>
            <dd>{{ props.result.differentials.join('、') || '--' }}</dd>
          </div>
          <div v-if="props.result.confidence.visible" class="meta-row">
            <dt>数据置信度</dt>
            <dd>
              <BaseTag :type="confidenceTagType" size="sm">{{ props.result.confidence.label }}</BaseTag>
            </dd>
          </div>
        </dl>
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

    <!-- 关键证据汇总 -->
    <div v-if="props.result.evidenceItems.length > 0" class="report-section">
      <div class="section-title">关键证据汇总</div>
      <div class="evidence-grid">
        <div
          v-for="item in props.result.evidenceItems"
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

    <!-- 特征性生化指标偏离分析 -->
    <div class="report-section">
      <div class="section-title">特征性生化指标偏离分析</div>
      <div class="indicator-list">
        <div
          v-for="item in props.result.indicators"
          :key="item.name"
          class="indicator-row"
        >
          <div class="indicator-label">{{ item.name }}</div>
          <div class="indicator-bar-area">
            <el-progress
              :percentage="item.percentage"
              :status="item.status"
              :stroke-width="14"
              :show-text="false"
            />
          </div>
          <div class="indicator-value" :class="item.status">
            {{ item.value }} {{ item.unit }}
            <span class="indicator-ref">(参考: {{ item.normal }})</span>
          </div>
        </div>
        <div v-if="props.result.indicators.length === 0" class="indicator-empty">
          暂无结构化生化指标
        </div>
      </div>
    </div>

    <!-- 靶向基因变异预测 + 专病膳食 -->
    <div class="report-duo">
      <div class="report-section suggestion-section">
        <div class="section-title">
          <BaseIcon name="microscope" :size="16" /> 靶向基因变异预测
        </div>
        <div class="gene-tags">
          <BaseTag
            v-for="gene in props.result.genes"
            :key="gene"
            type="danger"
            effect="light"
            size="sm"
          >
            {{ gene }}
          </BaseTag>
          <span v-if="props.result.genes.length === 0" class="text-muted">暂无明确高风险基因变异</span>
        </div>
        <el-alert
          v-if="props.result.geneRecommendationTitle"
          :title="props.result.geneRecommendationTitle"
          type="warning"
          :description="props.result.sequencing"
          show-icon
          :closable="false"
          class="gene-alert"
        />
      </div>
      <div class="report-section suggestion-section diet-section">
        <div class="section-title">
          <BaseIcon name="apple" :size="16" /> 专病膳食与干预建议
        </div>
        <p class="diet-text">{{ props.result.diet }}</p>
        <div class="diet-tags">
          <BaseTag
            v-for="tag in props.result.dietTags"
            :key="tag"
            type="info"
            effect="plain"
            size="sm"
          >
            {{ tag }}
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
  align-items: center;
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
  margin: 0;
  color: var(--imld-danger);
  font-size: 24px;
  font-weight: 700;
}

.report-meta {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-2);
}

.meta-row {
  display: flex;
  gap: var(--imld-sp-3);
  align-items: baseline;
  font-size: 13px;
}

.meta-row dt {
  flex-shrink: 0;
  width: 72px;
  color: var(--imld-muted);
}

.meta-row dd {
  margin: 0;
  color: var(--imld-text);
}

.evidence-summary {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
}

/* 区块 */
.report-section {
  border: 1px solid var(--imld-border);
  border-radius: var(--imld-radius-sm);
  padding: var(--imld-sp-4);
  background: var(--imld-card);
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--imld-text);
  margin-bottom: var(--imld-sp-3);
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
}

.evidence-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--imld-sp-3);
}

.evidence-item {
  border: 1px solid var(--imld-border);
  border-radius: var(--imld-radius-sm);
  padding: var(--imld-sp-3);
  background: var(--imld-card);
}

.evidence-item.warning {
  border-color: var(--imld-warning);
  background: rgba(230, 162, 60, 0.06);
}

.evidence-item.exception {
  border-color: var(--imld-danger);
  background: rgba(245, 108, 108, 0.06);
}

.evidence-item-top {
  display: flex;
  justify-content: space-between;
  gap: var(--imld-sp-2);
  margin-bottom: var(--imld-sp-2);
}

.evidence-category {
  font-size: 12px;
  font-weight: 700;
  color: var(--imld-text-secondary);
}

.evidence-source {
  font-size: 12px;
  color: var(--imld-muted);
}

.evidence-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--imld-text);
  line-height: 1.5;
}

.evidence-value {
  margin-top: var(--imld-sp-1);
  font-size: 12px;
  color: var(--imld-text-secondary);
  line-height: 1.4;
}

.indicator-list {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
}

.indicator-row {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-3);
}

.indicator-label {
  width: 120px;
  font-size: 14px;
  color: var(--imld-text-secondary);
  text-align: right;
  flex-shrink: 0;
}

.indicator-bar-area {
  flex: 1;
}

.indicator-value {
  width: 160px;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.indicator-value.exception {
  color: var(--imld-danger);
}

.indicator-value.warning {
  color: var(--imld-warning);
}

.indicator-ref {
  font-size: 12px;
  color: var(--imld-muted);
  font-weight: normal;
  margin-left: 4px;
}

.indicator-empty {
  text-align: center;
  color: var(--imld-muted);
  padding: var(--imld-sp-4);
}

.report-duo {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--imld-sp-4);
}

.suggestion-section {
  height: 100%;
}

.gene-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
  margin-bottom: var(--imld-sp-3);
}

.gene-alert {
  margin-top: var(--imld-sp-3);
}

.diet-section {
  background: linear-gradient(
    135deg,
    rgba(15, 109, 141, 0.05),
    rgba(34, 163, 159, 0.08)
  );
}

.diet-text {
  font-size: 14px;
  color: var(--imld-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--imld-sp-3);
}

.diet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
}

.text-muted {
  font-size: 13px;
  color: var(--imld-muted);
}
</style>
