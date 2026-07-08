import { describe, expect, test } from 'bun:test'
import { buildAggregateSnapshot } from '../aggregateSnapshot'
import { createScriptedResponder } from '../copilot/scriptedResponder'
import type { CohortAggregateSnapshot } from '../aggregateSnapshot'
import type { CohortMetrics, WorklistCase, IntelligenceInsight } from '../types'

// 1. 类型层断言：snapshot 不含患者级字段
type PatientFields = 'patientId' | 'patientName' | 'name' | 'id' | 'mrn' | 'encounterId'
type SnapshotKeys = keyof CohortAggregateSnapshot
type HasPatientField = PatientFields & SnapshotKeys
const _assertNoPatientFields: HasPatientField extends never ? true : false = true

function makeMetrics(): CohortMetrics {
  return {
    totalPatients: { data: 100, origin: 'derived' },
    highRiskCount: { data: 5, origin: 'derived' },
    midRiskCount: { data: 10, origin: 'derived' },
    lowRiskCount: { data: 85, origin: 'derived' },
    positiveRate: { data: 0.15, origin: 'derived' },
    autoReportCount: { data: 12, origin: 'derived' },
    diseaseSpectrum: {
      data: [{ name: 'A病', pct: 40 }],
      origin: 'derived',
    },
  }
}

function makeWorklist(): WorklistCase[] {
  return [
    {
      id: 'p1',
      name: '患者123',
      riskScore: 85,
      riskLevel: '高',
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

describe('隐私回归测试', () => {
  test('snapshot 不含患者可识别信息', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())
    const json = JSON.stringify(snapshot)

    // 不含 "患者123" 这种 mock 名
    expect(json).not.toMatch(/患者\d+/)
    expect(snapshot).not.toHaveProperty('patientIds')
    expect(snapshot).not.toHaveProperty('patientNames')
    expect(snapshot).not.toHaveProperty('id')
    expect(snapshot).not.toHaveProperty('name')
    expect(snapshot).not.toHaveProperty('patientId')
    expect(snapshot).not.toHaveProperty('mrn')
    expect(snapshot).not.toHaveProperty('encounterId')
  })

  test('snapshot 仅含聚合量', () => {
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())
    expect(typeof snapshot.totalPatients).toBe('number')
    expect(typeof snapshot.highRiskCount).toBe('number')
    expect(Array.isArray(snapshot.diseaseSpectrum)).toBe(true)
    expect(Array.isArray(snapshot.weeklyNewCases)).toBe(true)
    expect(Array.isArray(snapshot.topRiskFactors)).toBe(true)
  })

  test('responder 仅消费 snapshot', async () => {
    const responder = createScriptedResponder()
    const snapshot = buildAggregateSnapshot(makeMetrics(), makeWorklist(), makeInsights())

    const chunks: string[] = []
    for await (const c of responder.respond('test', snapshot)) {
      chunks.push(c)
    }

    const text = chunks.join('')
    // 回答中不得出现患者姓名
    expect(text).not.toMatch(/患者123/)
    // 回答末尾必须有来源行
    expect(text).toContain('数据来源：本机脱敏聚合统计 · 未访问任何患者明细')
  })
})
