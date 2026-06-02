<template>
  <svg
    class="sparkline"
    :width="width"
    :height="height"
    :viewBox="`0 0 ${width} ${height}`"
  >
    <polyline
      :points="normalizedPoints"
      fill="none"
      :stroke="color"
      :stroke-width="strokeWidth"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { normalizeSparklinePoints } from "./SparklineUtils";

const props = withDefaults(
  defineProps<{
    points: number[];
    color?: string;
    width?: number;
    height?: number;
    strokeWidth?: number;
  }>(),
  {
    color: "var(--imld-teal-1)",
    width: 120,
    height: 40,
    strokeWidth: 2,
  }
);

const normalizedPoints = computed(() =>
  normalizeSparklinePoints(props.points, props.width, props.height)
);
</script>

<style scoped>
.sparkline {
  display: inline-block;
  flex-shrink: 0;
  vertical-align: middle;
}
</style>
