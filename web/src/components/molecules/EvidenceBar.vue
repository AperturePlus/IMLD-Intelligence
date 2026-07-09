<template>
  <div class="evidence-bar" :class="{ 'is-weak': props.weak }">
    <div class="evidence-bar-header">
      <span class="evidence-label">{{ props.label }}</span>
      <span v-if="showMetric" class="evidence-weight">{{ metricCaption }} {{ formattedWeight }}</span>
    </div>
    <div class="evidence-track">
      <div class="evidence-fill" :style="fillStyle" />
    </div>
    <div v-if="props.value != null" class="evidence-value">{{ formattedValue }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { EvidenceBarProps } from './EvidenceBar.types'

const props = defineProps<EvidenceBarProps>()

const clampedWeight = computed(() =>
  Math.max(0, Math.min(100, Number.isFinite(props.weight) ? props.weight : 0))
)

const formattedWeight = computed(() => `${Math.round(clampedWeight.value)}%`)

const showMetric = computed(() => props.metricLabel !== null)
const metricCaption = computed(() => props.metricLabel ?? '权重')

const fillStyle = computed(() => ({
  width: `${clampedWeight.value}%`,
  backgroundColor: props.weak ? 'var(--imld-muted)' : 'var(--imld-teal-1)'
}))

const formattedValue = computed(() => {
  if (props.value == null) return ''
  return String(props.value)
})
</script>

<style scoped>
.evidence-bar {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.evidence-bar.is-weak {
  opacity: 0.65;
}
.evidence-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}
.evidence-label {
  color: var(--imld-text);
  font-weight: 500;
}
.evidence-weight {
  color: var(--imld-muted);
  font-size: 11px;
}
.evidence-track {
  height: 6px;
  border-radius: 3px;
  background: var(--imld-border);
  overflow: hidden;
}
.evidence-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s var(--imld-ease-out);
}
.evidence-value {
  font-size: 11px;
  color: var(--imld-muted);
}
</style>
