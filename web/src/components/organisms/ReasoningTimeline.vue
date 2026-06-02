<template>
  <div class="reasoning-timeline">
    <div class="timeline-header">
      <span class="timeline-title">AI 推理过程</span>
      <div class="timeline-actions">
        <BaseButton variant="ghost" size="sm" @click="emit('replay')">
          <BaseIcon name="refresh" :size="12" /> 重播
        </BaseButton>
        <BaseButton variant="primary" size="sm" @click="emit('expandReport')">
          查看完整报告
        </BaseButton>
      </div>
    </div>

    <div class="timeline-body">
      <div
        v-for="(step, i) in props.trace.steps"
        :key="i"
        class="timeline-step"
      >
        <div class="step-marker">
          <span class="step-number">{{ i + 1 }}</span>
          <div v-if="i < props.trace.steps.length - 1" class="step-line" />
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
    </div>
  </div>
</template>

<script setup lang="ts">
import BaseButton from "@/components/atoms/BaseButton.vue";
import BaseIcon from "@/components/atoms/BaseIcon.vue";
import EvidenceBar from "@/components/molecules/EvidenceBar.vue";
import ConfidenceGauge from "@/components/molecules/ConfidenceGauge.vue";
import DifferentialRace from "./DifferentialRace.vue";
import type { ReasoningTimelineProps } from "./ReasoningTimeline.types";

const props = defineProps<ReasoningTimelineProps>();
const emit = defineEmits<{
  (e: "replay"): void;
  (e: "expandReport"): void;
}>();
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
  gap: var(--imld-sp-2);
}
.timeline-body {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-4);
}
.timeline-step {
  display: flex;
  gap: var(--imld-sp-3);
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
</style>
