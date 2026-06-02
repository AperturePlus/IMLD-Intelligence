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
        @click="emit('openCase', c.id)"
      >
        {{ c.name }}
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
  padding: 4px 10px;
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
}
.banner-chip--case {
  background: var(--imld-bg);
  color: var(--imld-muted);
  border-color: var(--imld-border);
  cursor: pointer;
  transition: all 0.2s;
}
.banner-chip--case:hover {
  border-color: var(--imld-primary);
  color: var(--imld-primary);
}
</style>
