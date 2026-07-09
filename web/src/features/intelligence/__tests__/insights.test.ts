import { describe, test, expect } from 'bun:test'
import { generateInsights } from '../insights'
import type { CohortMetrics } from '../types'

function makeMetrics(overrides: Partial<{
  total: number
  high: number
  mid: number
  low: number
  positiveRate: number
  spectrum: { name: string; pct: number }[]
}> = {}): CohortMetrics {
  const {
    total = 10,
    high = 2,
    mid = 3,
    low = 5,
    positiveRate: rate = 0.6,
    spectrum = [{ name: '血色病', pct: 40 }]
  } = overrides

  return {
    totalPatients: { data: total, origin: 'derived' },
    highRiskCount: { data: high, origin: 'derived' },
    midRiskCount: { data: mid, origin: 'derived' },
    lowRiskCount: { data: low, origin: 'derived' },
    positiveRate: { data: rate, origin: 'derived' },
    autoReportCount: { data: 6, origin: 'derived' },
    diseaseSpectrum: { data: spectrum, origin: 'derived' }
  }
}

describe('generateInsights', () => {
  test('模板匹配逻辑：高危触发 high 洞察', () => {
    const metrics = makeMetrics({ high: 3 })
    const insights = generateInsights(metrics)

    const highInsight = insights.find((i) => i.severity === 'high')
    expect(highInsight).toBeDefined()
    expect(highInsight!.title).toContain('高危')
  })

  test('阳性率 > 0.5 触发 mid 洞察', () => {
    const metrics = makeMetrics({ positiveRate: 0.6 })
    const insights = generateInsights(metrics)

    const rateInsight = insights.find((i) => i.title.includes('阳性率'))
    expect(rateInsight).toBeDefined()
    expect(rateInsight!.severity).toBe('mid')
  })

  test('疾病谱首位占比 > 30% 触发洞察', () => {
    const metrics = makeMetrics({ spectrum: [{ name: '血色病', pct: 35 }] })
    const insights = generateInsights(metrics)

    const spectrumInsight = insights.find((i) => i.title.includes('疾病谱'))
    expect(spectrumInsight).toBeDefined()
    expect(spectrumInsight!.title).toContain('血色病')
  })

  test('输出不含患者标识（正则断言）', () => {
    const metrics = makeMetrics()
    const insights = generateInsights(metrics)

    for (const insight of insights) {
      expect(insight.title).not.toMatch(/患者\d+/)
      expect(insight.title).not.toMatch(/病号/)
      expect(insight.title).not.toMatch(/ID/)
    }
  })

  test('空聚合返回合理洞察', () => {
    const metrics = makeMetrics({ total: 0, high: 0, mid: 0, low: 0, positiveRate: 0, spectrum: [] })
    const insights = generateInsights(metrics)

    // 应该只生成一条关于在管患者的洞察，但因为 high=0 所以 severity 为 low
    expect(insights.length).toBeGreaterThanOrEqual(1)
  })

  test('置信度在 0-1 范围内', () => {
    const metrics = makeMetrics()
    const insights = generateInsights(metrics)

    for (const insight of insights) {
      expect(insight.confidence).toBeGreaterThanOrEqual(0)
      expect(insight.confidence).toBeLessThanOrEqual(1)
    }
  })
})
