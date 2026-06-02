<template>
  <div class="chat-message" :class="`is-${props.role}`">
    <div class="chat-bubble">
      <StreamingText
        v-if="props.streaming && props.role === 'ai'"
        :text="props.text"
        :speed="25"
        @complete="handleComplete"
      />
      <span v-else>{{ props.text }}</span>
    </div>
    <div v-if="props.source" class="chat-source">来源：{{ props.source }}</div>
  </div>
</template>

<script setup lang="ts">
import StreamingText from "@/components/atoms/StreamingText.vue";
import type { ChatMessageProps } from "./ChatMessage.types";

const props = defineProps<ChatMessageProps>();
const emit = defineEmits<{
  (e: "streamComplete"): void;
}>();

const handleComplete = () => {
  emit("streamComplete");
};
</script>

<style scoped>
.chat-message {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 80%;
}
.chat-message.is-user {
  align-self: flex-end;
  align-items: flex-end;
}
.chat-message.is-ai {
  align-self: flex-start;
  align-items: flex-start;
}
.chat-bubble {
  padding: 10px 14px;
  border-radius: var(--imld-radius-md);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.chat-message.is-user .chat-bubble {
  background: var(--imld-primary);
  color: var(--imld-white);
  border-bottom-right-radius: 4px;
}
.chat-message.is-ai .chat-bubble {
  background: var(--imld-card);
  color: var(--imld-text);
  border: 1px solid var(--imld-border);
  border-bottom-left-radius: 4px;
  box-shadow: var(--imld-shadow-card);
}
.chat-source {
  font-size: 11px;
  color: var(--imld-muted);
}
</style>
