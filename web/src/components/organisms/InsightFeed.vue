<template>
  <div class="insight-feed">
    <div v-if="props.items.length === 0" class="insight-empty">
      暂无洞察数据
    </div>
    <InsightItem
      v-for="(item, i) in props.items"
      :key="i"
      v-bind="item"
    />
  </div>
</template>

<script setup lang="ts">
import InsightItem from '@/components/molecules/InsightItem.vue'
import type { InsightFeedProps } from './InsightFeed.types'

const props = defineProps<InsightFeedProps>()
</script>

<style scoped>
.insight-feed {
  display: flex;
  flex-direction: column;
}
.insight-empty {
  padding: var(--imld-sp-4);
  text-align: center;
  color: var(--imld-muted);
  font-size: 13px;
}
</style>
