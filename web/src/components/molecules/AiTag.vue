<template>
  <span class="ai-tag">
    <BaseIcon name="spark" :size="10" class="ai-icon" />
    <slot />
  </span>
</template>

<script setup lang="ts">
import BaseIcon from '@/components/atoms/BaseIcon.vue'
</script>

<style scoped>
.ai-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 1px 6px;
  border-radius: var(--imld-radius-sm);
  background: rgba(64, 158, 255, 0.1);
  color: var(--imld-primary);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
.ai-icon {
  opacity: 0.9;
}
</style>
