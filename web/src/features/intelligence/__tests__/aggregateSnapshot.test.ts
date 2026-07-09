import { describe, expect, test } from 'bun:test'
import { buildAggregateSnapshot } from '../aggregateSnapshot'
import type { CohortMetrics, WorklistCase, IntelligenceInsight } from '../types'

function makeMetrics(partial: Partial<CohortMetrics> = {}): CohortMetrics {
  return {
    totalPatients: { data: 100, origin: 'derived' },
    highRiskCount: { data: 5, origin: 'derived' },
    midRiskCount: { data: 10, origin: 'derived' },
    lowRiskCount: { data: 85, origin: 'derived' },
    positiveRate: { data: 0.15, origin: 'derived' },
    autoReportCount: { data: 12, origin: 'derived' },
    diseaseSpectrum: {
      data: [
        { name: 'A病', pct: 40 },
        { name: 'B病', pct: 30 },
        { name: 'C病', pct: 20 },
      ],
      origin: 'derived',
    },
    ...partial,
  }
}

function makeWorklist(): WorklistCase[] {
  return [
    {
      id: 'p1',
      name: '张三',
      riskScore: 85,
      riskLevel: '高',
      reason: '铁蛋白升高、ALT异常',
      origin: 'synthesized',
    },
    {
      id: 'p2',
      name: '李四',
      riskScore: 65,
      riskLevel: '中',
      reason: '铁蛋白升高',
      origin: 'synthesized',
    },
  ]
}

function makeInsights(): IntelligenceInsight[] {
  return [
    {
      time: '09:00',
      title: '测试洞察',
      confidence: 0.8,
      severity: 'mid',
      origin: 'synthesized',
    },
  ]
}

describe('buildAggregateSnapshot', () => {
  test('字段完整性', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())

    expect(snapshot.totalPatients).toBe(100)
    expect(snapshot.highRiskCount).toBe(5)
    expect(snapshot.midRiskCount).toBe(10)
    expect(snapshot.lowRiskCount).toBe(85)
    expect(snapshot.positiveRate).toBe(0.15)
    expect(snapshot.autoReportCount).toBe(12)
    expect(snapshot.diseaseSpectrum).toHaveLength(3)
    expect(snapshot.weeklyNewCases).toHaveLength(12)
    expect(snapshot.topRiskFactors.length).toBeGreaterThan(0)
    expect(snapshot.generatedAt).toBeString()
  })

  test('疾病谱 count 计算正确', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())
    expect(snapshot.diseaseSpectrum[0].count).toBe(40) // 40% of 100
    expect(snapshot.diseaseSpectrum[1].count).toBe(30)
    expect(snapshot.diseaseSpectrum[2].count).toBe(20)
  })

  test('topRiskFactors 从 worklist reason 提取高频词', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())
    expect(snapshot.topRiskFactors).toContain('铁蛋白升高')
  })

  test('weeklyNewCases 长度为 12', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())
    expect(snapshot.weeklyNewCases).toHaveLength(12)
    for (const v of snapshot.weeklyNewCases) {
      expect(v).toBeGreaterThanOrEqual(0)
    }
  })

  test('空 worklist 时 topRiskFactors 为空', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), [], makeInsights())
    expect(snapshot.topRiskFactors).toEqual([])
  })
})
