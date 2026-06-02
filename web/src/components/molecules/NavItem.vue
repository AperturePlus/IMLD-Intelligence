<template>
  <div class="nav-item" :class="{ 'is-active': props.active }">
    <BaseIcon v-if="props.icon" :name="props.icon" :size="16" />
    <span class="nav-text">{{ props.title }}</span>
  </div>
</template>

<script setup lang="ts">
import BaseIcon from "@/components/atoms/BaseIcon.vue";

const props = withDefaults(
  defineProps<{
    icon?: string;
    title: string;
    active?: boolean;
  }>(),
  {
    active: false,
  }
);
</script>

<style scoped>
.nav-item {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
  padding: 10px var(--imld-sp-3);
  border-radius: var(--imld-radius-sm);
  color: var(--imld-on-dark-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.nav-item:hover {
  background: rgba(var(--imld-white-rgb), 0.08);
  color: var(--imld-white);
}
.nav-item.is-active {
  background: var(--imld-primary);
  color: var(--imld-white);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
.nav-text {
  white-space: nowrap;
}
</style>
