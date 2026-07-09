<template>
  <div class="disease-spectrum">
    <div
      v-for="item in props.items"
      :key="item.name"
      class="spectrum-row"
    >
      <span class="spectrum-name">{{ item.name }}</span>
      <div class="spectrum-track">
        <div
          class="spectrum-fill"
          :style="{ width: `${Math.min(100, Math.max(0, item.pct))}%` }"
        />
      </div>
      <span class="spectrum-pct">{{ item.pct }}%</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DiseaseSpectrumProps } from './DiseaseSpectrum.types'

const props = defineProps<DiseaseSpectrumProps>()
</script>

<style scoped>
.disease-spectrum {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-3);
}
.spectrum-row {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-3);
}
.spectrum-name {
  width: 100px;
  font-size: 13px;
  color: var(--imld-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.spectrum-track {
  flex: 1;
  height: 8px;
  border-radius: 4px;
  background: var(--imld-border);
  overflow: hidden;
}
.spectrum-fill {
  height: 100%;
  border-radius: 4px;
  background: var(--imld-brand-grad);
  transition: width 0.6s var(--imld-ease-out);
}
.spectrum-pct {
  width: 40px;
  text-align: right;
  font-size: 12px;
  color: var(--imld-muted);
  font-weight: 500;
}
</style>
