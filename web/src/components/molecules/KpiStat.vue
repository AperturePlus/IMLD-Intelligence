<template>
  <div class="kpi-stat" :class="`tone-${props.tone ?? 'default'}`">
    <div class="kpi-label">{{ props.label }}</div>
    <div class="kpi-value-row">
      <span class="kpi-value">{{ displayValue }}</span>
      <span v-if="props.delta != null" class="kpi-delta" :class="deltaClass">
        {{ deltaSymbol }}{{ Math.abs(props.delta) }}%
      </span>
    </div>
    <Sparkline
      v-if="props.sparklinePoints && props.sparklinePoints.length > 0"
      :points="props.sparklinePoints"
      :color="sparklineColor"
      :width="100"
      :height="28"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Sparkline from '@/components/atoms/Sparkline.vue'
import type { KpiStatProps } from './KpiStat.types'

const props = defineProps<KpiStatProps>()

const displayValue = computed(() => {
  const v = props.value
  if (props.format === 'percent') {
    return `${v}%`
  }
  if (props.format === 'currency') {
    return `¥${v}`
  }
  return String(v)
})

const deltaClass = computed(() => {
  if (props.trend === 'up') return 'is-up'
  if (props.trend === 'down') return 'is-down'
  return 'is-flat'
})

const deltaSymbol = computed(() => {
  if (props.trend === 'up') return '+'
  if (props.trend === 'down') return '-'
  return ''
})

const sparklineColor = computed(() => {
  if (props.trend === 'up') return 'var(--imld-success)'
  if (props.trend === 'down') return 'var(--imld-danger)'
  return 'var(--imld-teal-1)'
})
</script>

<style scoped>
.kpi-stat {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-2);
  padding: var(--imld-sp-4) var(--imld-sp-5);
  border-radius: var(--imld-radius-md);
  background: var(--imld-card);
  border: 1px solid var(--imld-border);
  box-shadow: var(--imld-shadow-card);
  transition: transform 0.2s var(--imld-ease-out);
}
.kpi-stat:hover {
  transform: translateY(-2px);
}
.kpi-label {
  font-size: 12px;
  color: var(--imld-muted);
  font-weight: 500;
}
.kpi-value-row {
  display: flex;
  align-items: baseline;
  gap: var(--imld-sp-2);
}
.kpi-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--imld-text);
  line-height: 1.2;
}
.tone-danger .kpi-value {
  color: var(--imld-risk-high);
}
.tone-success .kpi-value {
  color: var(--imld-risk-low);
}
.tone-warning .kpi-value {
  color: var(--imld-risk-mid);
}
.kpi-delta {
  font-size: 12px;
  font-weight: 600;
}
.kpi-delta.is-up {
  color: var(--imld-success);
}
.kpi-delta.is-down {
  color: var(--imld-danger);
}
.kpi-delta.is-flat {
  color: var(--imld-muted);
}
</style>
