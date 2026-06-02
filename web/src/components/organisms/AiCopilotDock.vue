<template>
  <div class="ai-copilot-dock">
    <div class="dock-header">
      <div class="dock-title">
        <BaseIcon name="spark" :size="16" />
        <span>IMLD 智能助手</span>
      </div>
      <PrivacyBadge />
    </div>

    <div class="dock-messages">
      <ChatMessage
        v-for="(msg, i) in props.messages"
        :key="i"
        v-bind="msg"
      />
    </div>

    <ChatComposer
      :placeholder="'请描述患者情况或提问…'"
      :suggestions="props.suggestions"
      @send="emit('send', $event)"
      @pick="emit('send', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import BaseIcon from '@/components/atoms/BaseIcon.vue'
import PrivacyBadge from '@/components/molecules/PrivacyBadge.vue'
import ChatMessage from '@/components/molecules/ChatMessage.vue'
import ChatComposer from '@/components/molecules/ChatComposer.vue'
import type { AiCopilotDockProps } from './AiCopilotDock.types'

const props = defineProps<AiCopilotDockProps>()
const emit = defineEmits<{
  (e: 'send', text: string): void
}>()
</script>

<style scoped>
.ai-copilot-dock {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  height: 100%;
  border-radius: var(--imld-radius-lg);
  background: var(--imld-card);
  box-shadow: var(--imld-shadow-float);
  overflow: hidden;
}
.dock-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--imld-sp-3) var(--imld-sp-4);
  border-bottom: 1px solid var(--imld-border);
  background: var(--imld-bg);
}
.dock-title {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
  font-size: 14px;
  font-weight: 600;
  color: var(--imld-text);
}
.dock-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--imld-sp-3) var(--imld-sp-4);
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
}
</style>
