<template>
  <div class="differential-race">
    <div
      v-for="c in sortedCandidates"
      :key="c.name"
      class="race-row"
      :class="{ 'is-winner': c.win }"
    >
      <span class="race-name">{{ c.name }}</span>
      <div class="race-track">
        <div
          class="race-fill"
          :style="{ width: `${Math.min(100, Math.max(0, c.prob))}%` }"
        />
      </div>
      <span class="race-prob">{{ Math.round(c.prob) }}%</span>
      <span v-if="c.why" class="race-why">{{ c.why }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DifferentialRaceProps } from './DifferentialRace.types'

const props = defineProps<DifferentialRaceProps>()

const sortedCandidates = computed(() =>
  [...props.candidates].sort((a, b) => b.prob - a.prob)
)
</script>

<style scoped>
.differential-race {
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-2);
}
.race-row {
  display: flex;
  align-items: center;
  gap: var(--imld-sp-2);
  font-size: 12px;
}
.race-row.is-winner .race-name {
  color: var(--imld-primary);
  font-weight: 700;
}
.race-name {
  width: 80px;
  color: var(--imld-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}
.race-track {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: var(--imld-border);
  overflow: hidden;
}
.race-fill {
  height: 100%;
  border-radius: 3px;
  background: var(--imld-brand-grad);
  transition: width 0.8s var(--imld-ease-out);
}
.race-prob {
  width: 36px;
  text-align: right;
  color: var(--imld-text);
  font-weight: 600;
  flex-shrink: 0;
}
.race-why {
  color: var(--imld-muted);
  font-size: 11px;
  white-space: nowrap;
}
</style>
