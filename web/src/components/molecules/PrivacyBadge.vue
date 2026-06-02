<template>
  <span class="privacy-badge">
    <BaseIcon name="lock" :size="12" />
    <span>{{ displayText }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import BaseIcon from '@/components/atoms/BaseIcon.vue'
import type { PrivacyBadgeProps } from './PrivacyBadge.types'

const props = withDefaults(defineProps<PrivacyBadgeProps>(), {
  text: '数据不出域 · 院内离线'
})

const displayText = computed(() => props.text)
</script>

<style scoped>
.privacy-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--imld-sp-1);
  padding: 3px 10px;
  border-radius: var(--imld-radius-sm);
  background: rgba(103, 194, 58, 0.1);
  color: var(--imld-success);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}
</style>
