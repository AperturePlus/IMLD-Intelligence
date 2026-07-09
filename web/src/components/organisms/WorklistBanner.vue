<template>
  <div class="worklist-banner">
    <div class="banner-greeting">{{ props.greeting }}</div>
    <div class="banner-chips">
      <span class="banner-chip banner-chip--alert">
        AI 优先高危 {{ props.highRiskCount }} 例
      </span>
      <button
        v-for="c in props.cases"
        :key="c.id"
        type="button"
        class="banner-chip banner-chip--case"
        :class="riskClass(c.riskLevel)"
        :title="c.reason || undefined"
        @click="emit('openCase', c.id)"
      >
        <span class="case-dot" />
        <span class="case-name">{{ c.name }}</span>
        <span class="case-score">{{ c.score }}分</span>
        <span v-if="c.reason" class="case-reason">{{ c.reason }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { WorklistBannerProps } from './WorklistBanner.types'

const props = defineProps<WorklistBannerProps>()
const emit = defineEmits<{
  (e: 'openCase', id: string): void
}>()

const riskClass = (level: string): string => {
  if (level === '高') return 'is-high'
  if (level === '中') return 'is-mid'
  return 'is-low'
}
</script>

<style scoped>
.worklist-banner {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
  padding: var(--imld-sp-4) var(--imld-sp-5);
  border-radius: var(--imld-radius-md);
  background: var(--imld-card);
  box-shadow: var(--imld-shadow-card);
}
.banner-greeting {
  font-size: 16px;
  font-weight: 600;
  color: var(--imld-text);
}
.banner-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
  align-items: center;
}
.banner-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--imld-sp-2);
  padding: 5px 12px;
  border-radius: var(--imld-radius-pill);
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
  cursor: default;
}
.banner-chip--alert {
  background: rgba(245, 108, 108, 0.1);
  color: var(--imld-danger);
  border-color: rgba(245, 108, 108, 0.25);
  font-weight: 600;
}
.banner-chip--case {
  background: var(--imld-bg);
  border-color: var(--imld-border);
  cursor: pointer;
  transition: border-color 0.2s var(--imld-ease-out),
    box-shadow 0.2s var(--imld-ease-out);
  max-width: 320px;
}
.banner-chip--case:hover {
  border-color: var(--imld-primary);
  box-shadow: var(--imld-shadow-card);
}
.case-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.banner-chip--case.is-high .case-dot {
  background: var(--imld-risk-high);
}
.banner-chip--case.is-mid .case-dot {
  background: var(--imld-risk-mid);
}
.banner-chip--case.is-low .case-dot {
  background: var(--imld-risk-low);
}
.case-name {
  color: var(--imld-text);
  font-weight: 600;
}
.case-score {
  color: var(--imld-text);
  font-weight: 700;
}
.banner-chip--case.is-high .case-score {
  color: var(--imld-risk-high);
}
.case-reason {
  color: var(--imld-muted);
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
