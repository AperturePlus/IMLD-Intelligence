<template>
  <header class="app-topbar">
    <div class="topbar-left">
      <EngineStatusPill
        :running="props.engineRunning"
        :model-name="props.modelName"
        :version="props.version"
      />
      <PrivacyBadge />
    </div>
    <div class="topbar-right">
      <BaseButton variant="ghost" size="sm" @click="emit('openCopilot')">
        <BaseIcon name="spark" :size="14" /> 问 IMLD 助手
      </BaseButton>
    </div>
  </header>
</template>

<script setup lang="ts">
import BaseIcon from '@/components/atoms/BaseIcon.vue'
import BaseButton from '@/components/atoms/BaseButton.vue'
import EngineStatusPill from '@/components/molecules/EngineStatusPill.vue'
import PrivacyBadge from '@/components/molecules/PrivacyBadge.vue'
import type { AppTopbarProps } from './AppTopbar.types'

const props = withDefaults(defineProps<AppTopbarProps>(), {
  engineRunning: true,
  modelName: 'IMLD-Core',
  version: '2.1'
})

const emit = defineEmits<{
  (e: 'openCopilot'): void
}>()
</script>

<style scoped>
.app-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--imld-sp-3);
  height: 52px;
  padding: 0 var(--imld-sp-5);
  background: var(--imld-card);
  border-bottom: 1px solid var(--imld-border);
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-3);
}

.topbar-right {
  display: flex;
  align-items: center;
}
</style>
