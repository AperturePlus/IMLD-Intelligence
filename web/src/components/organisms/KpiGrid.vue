<template>
  <div class="kpi-grid">
    <KpiStat
      v-for="(item, i) in props.items"
      :key="i"
      v-bind="item"
    />
  </div>
</template>

<script setup lang="ts">
import KpiStat from '@/components/molecules/KpiStat.vue'
import type { KpiStatProps } from '@/components/molecules/KpiStat.types'

const props = defineProps<{
  items: KpiStatProps[]
}>()
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--imld-sp-4);
}
</style>
