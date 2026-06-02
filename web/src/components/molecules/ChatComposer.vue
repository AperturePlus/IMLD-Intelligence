<template>
  <div class="chat-composer">
    <div v-if="props.suggestions && props.suggestions.length > 0" class="composer-chips">
      <button
        v-for="s in props.suggestions"
        :key="s"
        type="button"
        class="composer-chip"
        @click="handlePick(s)"
      >
        {{ s }}
      </button>
    </div>
    <div class="composer-input-row">
      <input
        v-model="text"
        type="text"
        class="composer-input"
        :placeholder="props.placeholder"
        @keydown.enter="handleSend"
      />
      <BaseButton variant="primary" size="sm" @click="handleSend">发送</BaseButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/atoms/BaseButton.vue'
import type { ChatComposerProps } from './ChatComposer.types'

const props = withDefaults(defineProps<ChatComposerProps>(), {
  placeholder: '请输入问题...',
  suggestions: () => []
})

const emit = defineEmits<{
  (e: 'send', text: string): void
  (e: 'pick', text: string): void
}>()

const text = ref('')

const handleSend = () => {
  const t = text.value.trim()
  if (!t) return
  emit('send', t)
  text.value = ''
}

const handlePick = (s: string) => {
  emit('pick', s)
}
</script>

<style scoped>
.chat-composer {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-2);
  padding: var(--imld-sp-3);
  border-top: 1px solid var(--imld-border);
  background: var(--imld-card);
}
.composer-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--imld-sp-2);
}
.composer-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: var(--imld-radius-pill);
  border: 1px solid var(--imld-border);
  background: transparent;
  color: var(--imld-muted);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.composer-chip:hover {
  border-color: var(--imld-primary);
  color: var(--imld-primary);
  background: rgba(64, 158, 255, 0.06);
}
.composer-input-row {
  display: flex;
  gap: var(--imld-sp-2);
  align-items: center;
}
.composer-input {
  flex: 1;
  height: 36px;
  padding: 0 var(--imld-sp-3);
  border: 1px solid var(--imld-border);
  border-radius: var(--imld-radius-sm);
  background: var(--imld-bg);
  color: var(--imld-text);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.composer-input:focus {
  border-color: var(--imld-primary);
}
</style>
