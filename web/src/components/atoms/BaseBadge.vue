<template>
  <span v-if="displayValue !== ''" class="base-badge" :class="[`is-${type}`]">
    {{ displayValue }}
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    value?: number | string;
    type?: "primary" | "success" | "warning" | "danger" | "info";
  }>(),
  {
    value: "",
    type: "danger",
  }
);

const displayValue = computed(() => {
  const v = props.value;
  if (v === "" || v === null || v === undefined) return "";
  if (typeof v === "number") {
    return v > 99 ? "99+" : String(v);
  }
  return String(v);
});
</script>

<style scoped>
.base-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  color: var(--imld-white);
  background-color: var(--imld-danger);
}

.base-badge.is-primary {
  background-color: var(--imld-primary);
}
.base-badge.is-success {
  background-color: var(--imld-success);
}
.base-badge.is-warning {
  background-color: var(--imld-warning);
}
.base-badge.is-danger {
  background-color: var(--imld-danger);
}
.base-badge.is-info {
  background-color: var(--imld-info);
}
</style>
