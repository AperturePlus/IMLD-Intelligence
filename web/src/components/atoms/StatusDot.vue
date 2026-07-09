<template>
  <span class="status-dot-wrapper">
    <span class="status-dot" :style="{ backgroundColor: dotColor }"></span>
    <span v-if="pulse" class="status-ping" :style="{ backgroundColor: dotColor }"></span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    color?: string
    pulse?: boolean
  }>(),
  {
    color: 'var(--imld-success)',
    pulse: false
  }
)

const dotColor = computed(() => props.color)
</script>

<style scoped>
.status-dot-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 8px;
  height: 8px;
}

.status-dot {
  position: relative;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-ping {
  position: absolute;
  top: 0;
  left: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: imld-ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}
</style>
