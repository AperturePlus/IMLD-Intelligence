<template>
  <span class="base-tag" :class="[typeClass, effectClass, sizeClass]">
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    type?: "primary" | "success" | "warning" | "danger" | "info";
    effect?: "light" | "dark" | "plain";
    size?: "sm" | "md" | "lg";
  }>(),
  {
    type: "primary",
    effect: "light",
    size: "md",
  }
);

const typeClass = computed(() => `is-${props.type}`);
const effectClass = computed(() => `is-${props.effect}`);
const sizeClass = computed(() => `is-${props.size}`);
</script>

<style scoped>
.base-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: var(--imld-radius-sm);
  font-weight: 500;
  white-space: nowrap;
}

/* sizes */
.base-tag.is-sm {
  height: 22px;
  padding: 0 6px;
  font-size: 11px;
}
.base-tag.is-md {
  height: 26px;
  padding: 0 8px;
  font-size: 12px;
}
.base-tag.is-lg {
  height: 30px;
  padding: 0 10px;
  font-size: 13px;
}

/* type + effect combinations */
.base-tag.is-primary.is-light {
  background: rgba(64, 158, 255, 0.1);
  color: var(--imld-primary);
  border: 1px solid rgba(64, 158, 255, 0.2);
}
.base-tag.is-success.is-light {
  background: rgba(103, 194, 58, 0.1);
  color: var(--imld-success);
  border: 1px solid rgba(103, 194, 58, 0.2);
}
.base-tag.is-warning.is-light {
  background: rgba(230, 162, 60, 0.1);
  color: var(--imld-warning);
  border: 1px solid rgba(230, 162, 60, 0.2);
}
.base-tag.is-danger.is-light {
  background: rgba(245, 108, 108, 0.1);
  color: var(--imld-danger);
  border: 1px solid rgba(245, 108, 108, 0.2);
}
.base-tag.is-info.is-light {
  background: rgba(144, 147, 153, 0.1);
  color: var(--imld-info);
  border: 1px solid rgba(144, 147, 153, 0.2);
}

.base-tag.is-primary.is-dark {
  background: var(--imld-primary);
  color: var(--imld-white);
  border: 1px solid var(--imld-primary);
}
.base-tag.is-success.is-dark {
  background: var(--imld-success);
  color: var(--imld-white);
  border: 1px solid var(--imld-success);
}
.base-tag.is-warning.is-dark {
  background: var(--imld-warning);
  color: var(--imld-white);
  border: 1px solid var(--imld-warning);
}
.base-tag.is-danger.is-dark {
  background: var(--imld-danger);
  color: var(--imld-white);
  border: 1px solid var(--imld-danger);
}
.base-tag.is-info.is-dark {
  background: var(--imld-info);
  color: var(--imld-white);
  border: 1px solid var(--imld-info);
}

.base-tag.is-primary.is-plain {
  background: transparent;
  color: var(--imld-primary);
  border: 1px solid rgba(64, 158, 255, 0.45);
}
.base-tag.is-success.is-plain {
  background: transparent;
  color: var(--imld-success);
  border: 1px solid rgba(103, 194, 58, 0.45);
}
.base-tag.is-warning.is-plain {
  background: transparent;
  color: var(--imld-warning);
  border: 1px solid rgba(230, 162, 60, 0.45);
}
.base-tag.is-danger.is-plain {
  background: transparent;
  color: var(--imld-danger);
  border: 1px solid rgba(245, 108, 108, 0.45);
}
.base-tag.is-info.is-plain {
  background: transparent;
  color: var(--imld-info);
  border: 1px solid rgba(144, 147, 153, 0.45);
}
</style>
