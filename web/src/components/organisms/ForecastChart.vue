<template>
  <div ref="chartRef" class="forecast-chart" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts/core";
import { LineChart } from "echarts/charts";
import {
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
import type { ForecastChartProps } from "./ForecastChart.types";

function getCssVar(name: string, fallback = ""): string {
  if (typeof document === "undefined") return fallback;
  const val = getComputedStyle(document.documentElement)
    .getPropertyValue(name)
    .trim();
  return val || fallback;
}

echarts.use([
  LineChart,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer,
]);

const props = defineProps<ForecastChartProps>();

const chartRef = ref<HTMLDivElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

const buildOption = (): echarts.EChartsCoreOption => {
  const historyData = props.history.map((p) => [p.x, p.y]);
  const forecastData = props.forecast.map((p) => [p.x, p.y]);

  return {
    tooltip: {
      trigger: "axis",
    },
    legend: {
      data: ["历史趋势", "AI 预测"],
      bottom: 0,
      textStyle: {
        color: getCssVar("--imld-muted", "#6a7d90"),
        fontSize: 12,
      },
    },
    grid: {
      left: 10,
      right: 20,
      top: 20,
      bottom: 40,
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      axisLine: { lineStyle: { color: getCssVar("--imld-border", "#e6edf4") } },
      axisLabel: { color: getCssVar("--imld-muted", "#6a7d90"), fontSize: 11 },
    },
    yAxis: {
      type: "value",
      splitLine: {
        lineStyle: {
          color: getCssVar("--imld-border", "#e6edf4"),
          type: "dashed",
        },
      },
      axisLabel: { color: getCssVar("--imld-muted", "#6a7d90"), fontSize: 11 },
    },
    series: [
      {
        name: "历史趋势",
        type: "line",
        data: historyData,
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        lineStyle: { color: getCssVar("--imld-teal-1", "#0f6d8d"), width: 2 },
        itemStyle: { color: getCssVar("--imld-teal-1", "#0f6d8d") },
      },
      {
        name: "AI 预测",
        type: "line",
        data: forecastData,
        smooth: true,
        symbol: "emptyCircle",
        symbolSize: 6,
        lineStyle: {
          color: getCssVar("--imld-warning", "#e6a23c"),
          width: 2,
          type: "dashed",
        },
        itemStyle: { color: getCssVar("--imld-warning", "#e6a23c") },
      },
    ],
  };
};

const initChart = () => {
  if (!chartRef.value) return;
  chartInstance = echarts.init(chartRef.value);
  chartInstance.setOption(buildOption());
};

const handleResize = () => {
  chartInstance?.resize();
};

onMounted(() => {
  initChart();
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  chartInstance?.dispose();
  chartInstance = null;
});

watch(
  () => [props.history, props.forecast],
  () => {
    chartInstance?.setOption(buildOption(), true);
  },
  { deep: true }
);
</script>

<style scoped>
.forecast-chart {
  width: 100%;
  height: 280px;
}
</style>
