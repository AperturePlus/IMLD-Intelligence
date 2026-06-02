<template>
  <button
    class="base-button"
    :class="[variantClass, sizeClass]"
    v-bind="$attrs"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    variant?: "primary" | "ghost" | "text";
    size?: "sm" | "md" | "lg";
  }>(),
  {
    variant: "primary",
    size: "md",
  }
);

const variantClass = computed(() => `is-${props.variant}`);
const sizeClass = computed(() => `is-${props.size}`);
</script>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--imld-sp-1);
  border: 1px solid transparent;
  border-radius: var(--imld-radius-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s var(--imld-ease-out);
  white-space: nowrap;
  outline: none;
}
.base-button:focus-visible {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--imld-primary) 30%, transparent);
}

/* sizes */
.base-button.is-sm {
  height: 28px;
  padding: 0 var(--imld-sp-3);
  font-size: 12px;
}
.base-button.is-md {
  height: 36px;
  padding: 0 var(--imld-sp-4);
  font-size: 14px;
}
.base-button.is-lg {
  height: 44px;
  padding: 0 var(--imld-sp-5);
  font-size: 15px;
}

/* variants */
.base-button.is-primary {
  background: var(--imld-brand-grad);
  color: var(--imld-white);
  border-color: transparent;
}
.base-button.is-primary:hover {
  filter: brightness(1.08);
}

.base-button.is-ghost {
  background: transparent;
  color: var(--imld-teal-1);
  border-color: rgba(15, 109, 141, 0.35);
}
.base-button.is-ghost:hover {
  background: rgba(15, 109, 141, 0.08);
}

.base-button.is-text {
  background: transparent;
  color: var(--imld-teal-1);
  border-color: transparent;
}
.base-button.is-text:hover {
  background: rgba(15, 109, 141, 0.06);
}
</style>
