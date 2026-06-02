<template>
  <div ref="chartRef" class="forecast-chart" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ForecastChartProps } from './ForecastChart.types'

echarts.use([LineChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const props = defineProps<ForecastChartProps>()

const chartRef = ref<HTMLDivElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const buildOption = (): echarts.EChartsCoreOption => {
  const historyData = props.history.map((p) => [p.x, p.y])
  const forecastData = props.forecast.map((p) => [p.x, p.y])

  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['历史趋势', 'AI 预测'],
      bottom: 0,
      textStyle: {
        color: 'var(--imld-muted)',
        fontSize: 12
      }
    },
    grid: {
      left: 10,
      right: 20,
      top: 20,
      bottom: 40,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: { lineStyle: { color: 'var(--imld-border)' } },
      axisLabel: { color: 'var(--imld-muted)', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'var(--imld-border)', type: 'dashed' } },
      axisLabel: { color: 'var(--imld-muted)', fontSize: 11 }
    },
    series: [
      {
        name: '历史趋势',
        type: 'line',
        data: historyData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: 'var(--imld-teal-1)', width: 2 },
        itemStyle: { color: 'var(--imld-teal-1)' }
      },
      {
        name: 'AI 预测',
        type: 'line',
        data: forecastData,
        smooth: true,
        symbol: 'emptyCircle',
        symbolSize: 6,
        lineStyle: { color: 'var(--imld-warning)', width: 2, type: 'dashed' },
        itemStyle: { color: 'var(--imld-warning)' }
      }
    ]
  }
}

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

watch(() => [props.history, props.forecast], () => {
  chartInstance?.setOption(buildOption(), true)
}, { deep: true })
</script>

<style scoped>
.forecast-chart {
  width: 100%;
  height: 280px;
}
</style>
