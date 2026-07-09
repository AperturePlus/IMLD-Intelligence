<template>
  <div class="reasoning-timeline">
    <div class="timeline-header">
      <span class="timeline-title">AI 推理过程</span>
      <div class="timeline-actions">
        <span class="timeline-status" :class="{ 'is-done': completed }">
          {{ completed ? "推理完成" : `推理中 ${revealedCount}/${totalSteps}` }}
        </span>
        <BaseButton variant="primary" size="sm" @click="handleSkip">
          查看完整报告
        </BaseButton>
      </div>
    </div>

    <div class="timeline-body">
      <div
        v-for="(step, i) in visibleSteps"
        :key="i"
        class="timeline-step"
        :class="{ 'is-active': i === revealedCount - 1 && !completed }"
      >
        <div class="step-marker">
          <span class="step-number">{{ i + 1 }}</span>
          <div v-if="i < totalSteps - 1" class="step-line" />
        </div>

        <div class="step-content">
          <div class="step-title">{{ step.title }}</div>

          <div
            v-if="step.evidenceBars && step.evidenceBars.length > 0"
            class="step-evidence"
          >
            <EvidenceBar
              v-for="(bar, bi) in step.evidenceBars"
              :key="bi"
              v-bind="bar"
            />
          </div>

          <DifferentialRace
            v-if="step.candidates && step.candidates.length > 0"
            :candidates="step.candidates"
          />

          <ConfidenceGauge v-if="step.confidence" v-bind="step.confidence" />
        </div>
      </div>

      <div v-if="!completed" class="timeline-thinking">
        <span class="thinking-dots"> <i /><i /><i /> </span>
        <span class="thinking-label">IMLD AI 正在逐步推理…</span>
      </div>

      <div v-else class="timeline-footer">
        <BaseButton variant="primary" size="md" @click="handleViewReport">
          查看报告
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import BaseButton from "@/components/atoms/BaseButton.vue";
import EvidenceBar from "@/components/molecules/EvidenceBar.vue";
import ConfidenceGauge from "@/components/molecules/ConfidenceGauge.vue";
import DifferentialRace from "./DifferentialRace.vue";
import type { ReasoningTimelineProps } from "./ReasoningTimeline.types";

const STEP_INTERVAL = 850; // 步骤逐条显现的间隔
const START_DELAY = 300; // 首步显现前的起手延时
const FINAL_BEAT = 650; // 末步显现后、切换报告前的收束停顿

const props = defineProps<ReasoningTimelineProps>();
const emit = defineEmits<{
  (e: "complete"): void;
  (e: "skip"): void;
}>();

const revealedCount = ref(0);
const completed = ref(false);
let timer: ReturnType<typeof setTimeout> | null = null;

const totalSteps = computed(() => props.trace.steps.length);
const visibleSteps = computed(() =>
  props.trace.steps.slice(0, revealedCount.value)
);

const clearTimer = () => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
};

const advance = () => {
  if (revealedCount.value < totalSteps.value) {
    revealedCount.value += 1;
  }
  if (revealedCount.value < totalSteps.value) {
    timer = setTimeout(advance, STEP_INTERVAL);
  } else {
    // 末步已显现，收束停顿后标记完成，但不自动跳转
    timer = setTimeout(() => {
      timer = null;
      completed.value = true;
    }, FINAL_BEAT);
  }
};

const startPlayback = () => {
  clearTimer();
  revealedCount.value = 0;
  completed.value = false;
  if (totalSteps.value === 0) {
    completed.value = true;
    emit("complete");
    return;
  }
  timer = setTimeout(advance, START_DELAY);
};

const handleSkip = () => {
  clearTimer();
  revealedCount.value = totalSteps.value;
  completed.value = true;
  emit("skip");
};

const handleViewReport = () => {
  emit("complete");
};

onMounted(startPlayback);
onBeforeUnmount(clearTimer);
// 复用同一实例时（如重播传入新 trace）重新播放
watch(() => props.trace, startPlayback);
</script>

<style scoped>
.reasoning-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-4);
  padding: var(--imld-sp-4);
  border-radius: var(--imld-radius-md);
  background: var(--imld-card);
  box-shadow: var(--imld-shadow-card);
}
.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--imld-sp-3);
  border-bottom: 1px solid var(--imld-border);
}
.timeline-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--imld-text);
}
.timeline-actions {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-3);
}
.timeline-status {
  font-size: 12px;
  color: var(--imld-primary);
  font-weight: 600;
}
.timeline-status.is-done {
  color: var(--imld-risk-low);
}
.timeline-body {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-4);
}
.timeline-step {
  display: flex;
  gap: var(--imld-sp-3);
  animation: stepIn 0.42s var(--imld-ease-out);
}
.step-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.step-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--imld-primary);
  color: var(--imld-white);
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  transition: box-shadow 0.3s var(--imld-ease-out);
}
.timeline-step.is-active .step-number {
  animation: markerPulse 1.1s ease-in-out infinite;
}
.step-line {
  width: 2px;
  flex: 1;
  background: var(--imld-border);
  min-height: 20px;
}
.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
  padding-bottom: var(--imld-sp-3);
}
.step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--imld-text);
}
.step-evidence {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-2);
}
.timeline-thinking {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
  padding-left: 36px;
  color: var(--imld-muted);
  font-size: 12px;
}
.thinking-dots {
  display: inline-flex;
  gap: 4px;
}
.thinking-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--imld-primary);
  animation: dotBounce 1.2s ease-in-out infinite;
}
.thinking-dots i:nth-child(2) {
  animation-delay: 0.18s;
}
.thinking-dots i:nth-child(3) {
  animation-delay: 0.36s;
}

@keyframes stepIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes markerPulse {
  0% {
    box-shadow: 0 0 0 0 rgba(15, 109, 141, 0.45);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(15, 109, 141, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(15, 109, 141, 0);
  }
}
@keyframes dotBounce {
  0%,
  100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-4px);
  }
}
.timeline-footer {
  display: flex;
  justify-content: center;
  padding: var(--imld-sp-4) 0;
}
</style>
