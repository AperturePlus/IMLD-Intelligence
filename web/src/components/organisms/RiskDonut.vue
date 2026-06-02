<template>
  <div ref="chartRef" class="risk-donut" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { RiskDonutProps } from './RiskDonut.types'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<RiskDonutProps>()

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const buildOption = (): echarts.EChartsCoreOption => ({
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c} ({d}%)'
  },
  legend: {
    bottom: 0,
    left: 'center',
    itemWidth: 10,
    itemHeight: 10,
    textStyle: {
      color: 'var(--imld-muted)',
      fontSize: 12
    }
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      label: {
        show: true,
        position: 'center',
        formatter: () => `总计\n${props.total}`,
        fontSize: 14,
        fontWeight: 'bold',
        color: 'var(--imld-text)'
      },
      emphasis: {
        label: { show: true }
      },
      labelLine: { show: false },
      data: [
        { value: props.high, name: '高危', itemStyle: { color: 'var(--imld-risk-high)' } },
        { value: props.mid, name: '中危', itemStyle: { color: 'var(--imld-risk-mid)' } },
        { value: props.low, name: '低危', itemStyle: { color: 'var(--imld-risk-low)' } }
      ]
    }
  ]
})

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(buildOption())
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})

watch(() => [props.high, props.mid, props.low, props.total], () => {
  chartInstance?.setOption(buildOption())
}, { deep: true })
</script>

<style scoped>
.risk-donut {
  width: 100%;
  height: 260px;
}
</style>
