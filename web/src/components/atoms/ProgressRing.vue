<template>
  <svg class="progress-ring" :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
    <circle
      class="progress-ring-track"
      :cx="center"
      :cy="center"
      :r="radius"
      fill="none"
      :stroke="trackColor"
      :stroke-width="stroke"
    />
    <circle
      class="progress-ring-fill"
      :cx="center"
      :cy="center"
      :r="radius"
      fill="none"
      :stroke="color"
      :stroke-width="stroke"
      :stroke-dasharray="circumference"
      :stroke-dashoffset="offset"
      stroke-linecap="round"
      :transform="`rotate(-90 ${center} ${center})`"
    />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    percentage: number
    color?: string
    size?: number
    stroke?: number
  }>(),
  {
    color: 'var(--imld-primary)',
    size: 64,
    stroke: 6
  }
)

const center = computed(() => props.size / 2)
const radius = computed(() => (props.size - props.stroke) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)

const clampedPercentage = computed(() =>
  Math.max(0, Math.min(100, Number.isFinite(props.percentage) ? props.percentage : 0))
)

const offset = computed(
  () => circumference.value - (clampedPercentage.value / 100) * circumference.value
)

const trackColor = computed(() => {
  // derive a faint track color from the fill color if possible
  return 'rgba(148, 163, 184, 0.25)'
})
</script>

<style scoped>
.progress-ring {
  display: inline-block;
  flex-shrink: 0;
}

.progress-ring-fill {
  transition: stroke-dashoffset 0.6s var(--imld-ease-out);
}
</style>
