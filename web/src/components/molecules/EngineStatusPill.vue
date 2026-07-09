<template>
  <span class="engine-status-pill">
    <StatusDot :pulse="props.running" :color="dotColor" />
    <span class="engine-text">
      {{ props.running ? 'AI 引擎运行中' : '引擎待机' }} · {{ props.modelName }} v{{ props.version }}
    </span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import StatusDot from '@/components/atoms/StatusDot.vue'
import type { EngineStatusProps } from './EngineStatusPill.types'

const props = defineProps<EngineStatusProps>()

const dotColor = computed(() =>
  props.running ? 'var(--imld-success)' : 'var(--imld-muted)'
)
</script>

<style scoped>
.engine-status-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--imld-sp-2);
  padding: 4px 10px;
  border-radius: var(--imld-radius-pill);
  background: var(--imld-card);
  border: 1px solid var(--imld-border);
  font-size: 12px;
  color: var(--imld-muted);
  white-space: nowrap;
}
.engine-text {
  font-weight: 500;
}
</style>
