<template>
  <span class="risk-pill" :style="pillStyle">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import {
  normalizeRiskLevel,
  toChineseRiskLabel,
} from "@/features/diagnosis/services/riskLevel";
import { resolveRiskColor } from "./RiskPillUtils";
import type { RiskPillProps } from "./RiskPill.types";

const props = defineProps<RiskPillProps>();

const resolvedLevel = computed(() =>
  normalizeRiskLevel(props.level, props.probability)
);

const label = computed(() => {
  const base = toChineseRiskLabel(resolvedLevel.value);
  if (props.probability != null && Number.isFinite(props.probability)) {
    return `${base} · ${Math.round(props.probability)}%`;
  }
  return base;
});

const pillStyle = computed(() => ({
  backgroundColor: `${resolveRiskColor(resolvedLevel.value)}1a`,
  color: resolveRiskColor(resolvedLevel.value),
  border: `1px solid ${resolveRiskColor(resolvedLevel.value)}33`,
}));
</script>

<style scoped>
.risk-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--imld-radius-pill);
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
