<template>
  <div class="center-layout">
    <AppSidebar />
    <div class="center-main">
      <AppTopbar
        :engine-running="engineRunning"
        :model-name="ENGINE_INFO.modelName"
        :version="ENGINE_INFO.version"
        @open-copilot="showCopilot = !showCopilot"
      />
      <div class="center-content">
        <router-view />
      </div>
    </div>
    <AiCopilotDock
      v-if="showCopilot"
      class="copilot-float"
      :messages="copilotMessages"
      :suggestions="SUGGESTED_PROMPTS"
      @send="handleCopilotSend"
      @close="showCopilot = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import AppSidebar from "@/components/organisms/AppSidebar.vue";
import AppTopbar from "@/components/organisms/AppTopbar.vue";
import AiCopilotDock from "@/components/organisms/AiCopilotDock.vue";
import {
  useIntelligenceEngine,
  ENGINE_INFO,
} from "@/features/intelligence/composables/useIntelligenceEngine";
import { buildAggregateSnapshot } from "@/features/intelligence/aggregateSnapshot";
import { createScriptedResponder } from "@/features/intelligence/copilot/scriptedResponder";
import { SUGGESTED_PROMPTS } from "@/features/intelligence/copilot/intents";
import type { ChatMessageProps } from "@/components/molecules/ChatMessage.types";

const showCopilot = ref(false);
// 用自增 id 锁定消息，避免流式期间数组长度变化导致索引错位覆盖用户消息。
let messageSeq = 0;
const copilotMessages = ref<(ChatMessageProps & { __id: number })[]>([]);

const { cohortMetrics, worklist, insights, error } = useIntelligenceEngine();

// 引擎状态条由真实引擎状态驱动：无错误即视为运行中
const engineRunning = computed(() => error.value == null);

const snapshot = computed(() => {
  const m = cohortMetrics.value;
  if (!m) return null;
  return buildAggregateSnapshot(m, worklist.value, insights.value);
});

const responder = createScriptedResponder();

const handleCopilotSend = async (text: string) => {
  copilotMessages.value.push({ __id: messageSeq++, role: "user", text });

  const s = snapshot.value;
  if (!s) {
    copilotMessages.value.push({
      __id: messageSeq++,
      role: "ai",
      text: "数据尚未加载完成，请稍后再试。",
      source: "IMLD 助手",
    });
    return;
  }

  // 锁定当前流式消息的 id，即便用户在流式期间追加新消息，也只会更新本条。
  const aiId = messageSeq++;
  copilotMessages.value.push({
    __id: aiId,
    role: "ai",
    text: "",
    streaming: true,
    source: "IMLD 助手",
  });

  const updateAi = (patch: Partial<ChatMessageProps>) => {
    const idx = copilotMessages.value.findIndex((m) => m.__id === aiId);
    if (idx === -1) return;
    copilotMessages.value.splice(idx, 1, {
      ...copilotMessages.value[idx],
      ...patch,
    });
  };

  let fullText = "";
  for await (const chunk of responder.respond(text, s)) {
    fullText += chunk;
    updateAi({ text: fullText });
  }

  updateAi({ streaming: false });
};
</script>

<style scoped>
.center-layout {
  display: flex;
  height: calc(100vh - var(--electron-titlebar-safe-top, 0px));
  background: var(--imld-navy);
  overflow: hidden;
}

.center-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: radial-gradient(
      circle at 2% 2%,
      rgba(34, 163, 159, 0.14),
      transparent 32%
    ),
    radial-gradient(circle at 98% 0%, rgba(15, 109, 141, 0.14), transparent 34%),
    var(--imld-bg);
}

.center-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.copilot-float {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  height: 600px;
  max-height: 80vh;
  width: 480px;
  max-width: calc(100vw - 40px);
}
</style>
