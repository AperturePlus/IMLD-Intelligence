<template>
  <div class="confidence-gauge">
    <ProgressRing :percentage="clampedPct" :color="ringColor" :size="56" :stroke="5" />
    <div class="gauge-meta">
      <div class="gauge-value" :style="{ color: ringColor }">{{ clampedPct }}%</div>
      <div v-if="props.label" class="gauge-label">{{ props.label }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ProgressRing from '@/components/atoms/ProgressRing.vue'
import type { ConfidenceGaugeProps } from './ConfidenceGauge.types'

const props = withDefaults(defineProps<ConfidenceGaugeProps>(), {
  level: 'mid',
  label: ''
})

const clampedPct = computed(() =>
  Math.max(0, Math.min(100, Number.isFinite(props.percentage) ? props.percentage : 0))
)

const ringColor = computed(() => {
  if (props.level === 'high') return 'var(--imld-risk-high)'
  if (props.level === 'mid') return 'var(--imld-risk-mid)'
  return 'var(--imld-risk-low)'
})
</script>

<style scoped>
.confidence-gauge {
  display: inline-flex;
  align-items: center;
  gap: var(--imld-sp-2);
}
.gauge-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.gauge-value {
  font-size: 16px;
  font-weight: 700;
  line-height: 1.2;
}
.gauge-label {
  font-size: 11px;
  color: var(--imld-muted);
}
</style>
