<template>
  <div class="insight-item" :class="severityClass">
    <div class="insight-dot" />
    <div class="insight-body">
      <div class="insight-meta">
        <span class="insight-time">{{ props.time }}</span>
        <span v-if="props.confidence != null" class="insight-confidence"
          >置信 {{ Math.round(props.confidence) }}%</span
        >
      </div>
      <div class="insight-title">{{ props.title }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { InsightItemProps } from './InsightItem.types'

const props = defineProps<InsightItemProps>()

const severityClass = computed(() => {
  if (props.severity === 'high') return 'is-high'
  if (props.severity === 'mid') return 'is-mid'
  return 'is-low'
})
</script>

<style scoped>
.insight-item {
  display: flex;
  gap: var(--imld-sp-3);
  padding: var(--imld-sp-3) 0;
}
.insight-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 5px;
  flex-shrink: 0;
  background: var(--imld-risk-low);
}
.insight-item.is-mid .insight-dot {
  background: var(--imld-risk-mid);
}
.insight-item.is-high .insight-dot {
  background: var(--imld-risk-high);
}
.insight-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.insight-meta {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
  font-size: 11px;
}
.insight-time {
  color: var(--imld-muted);
}
.insight-confidence {
  color: var(--imld-primary);
  font-weight: 500;
}
.insight-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--imld-text);
  line-height: 1.4;
}
</style>
