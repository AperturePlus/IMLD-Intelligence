import { ref } from 'vue'
import type { CohortMetrics, WorklistCase, IntelligenceInsight, ForecastSeries } from '../types'
import { computeCohortMetrics } from '../cohort'
import { computeWorklist } from '../worklist'
import { generateInsights } from '../insights'
import { computeForecast } from '../forecast'
import { defaultIntelligenceDataSource } from '../dataSource'

// 引擎标识（单一来源，供状态条等展示）
export const ENGINE_INFO = {
  modelName: 'IMLD-Core',
  version: '2.1'
} as const

// 单例响应式状态：所有调用方（布局、仪表盘、Copilot）共享同一组 ref，
// 避免每次调用都新建 ref 并重复拉取队列 / 重复计算。
const loading = ref(false)
const cohortMetrics = ref<CohortMetrics | null>(null)
const worklist = ref<WorklistCase[]>([])
const insights = ref<IntelligenceInsight[]>([])
const forecast = ref<ForecastSeries | null>(null)
const error = ref<string | null>(null)

let inFlight: Promise<void> | null = null

async function load(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const [metrics, list] = await Promise.all([
      computeCohortMetrics(defaultIntelligenceDataSource),
      computeWorklist(defaultIntelligenceDataSource, 5)
    ])

    cohortMetrics.value = metrics
    worklist.value = list
    insights.value = generateInsights(metrics)
    forecast.value = computeForecast(metrics.totalPatients.data)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载数据失败'
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 并发去重：同时挂载的多个消费者只触发一次真实加载；
// 后续（重新进入仪表盘等）再次调用会刷新数据。
function refresh(): Promise<void> {
  if (!inFlight) {
    inFlight = load().finally(() => {
      inFlight = null
    })
  }
  return inFlight
}

export function useIntelligenceEngine() {
  refresh()
  return {
    loading,
    cohortMetrics,
    worklist,
    insights,
    forecast,
    error,
    refresh
  }
}
