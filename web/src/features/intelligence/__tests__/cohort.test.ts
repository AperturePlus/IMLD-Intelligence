import { describe, test, expect } from 'bun:test'
import { computeCohortMetrics } from '../cohort'
import type { IntelligenceDataSource } from '../types'
import type { DiagnosisQueuePatient, DiagnosisResult } from '@/types/diagnosis'

function makeQueue(items: Partial<DiagnosisQueuePatient>[]): DiagnosisQueuePatient[] {
  return items.map((item, i) => ({
    id: String(i + 1),
    name: `患者${i + 1}`,
    gender: '男',
    age: 45,
    aiStatus: '未诊断',
    ...item
  }))
}

function makeResult(overrides: Partial<DiagnosisResult> = {}): DiagnosisResult {
  return {
    diseaseName: '血色病',
    riskLevel: '高',
    probability: 85,
    indicators: [],
    evidenceItems: [],
    evidenceSummary: { modelFeatureCount: 40, abnormalEvidenceCount: 5, reviewRequired: false },
    confidence: { visible: true, rawValue: 0.85, displayValue: 85, reviewRequired: false, adjusted: false, label: '高置信度' },
    genes: [],
    diet: '',
    sequencing: '',
    differentials: [],
    keySigns: [],
    dietTags: [],
    geneRecommendationTitle: '',
    dataConfidenceLabel: '',
    ...overrides
  }
}

function makeDataSource(
  queue: DiagnosisQueuePatient[],
  results: Map<string, DiagnosisResult> = new Map()
): IntelligenceDataSource {
  return {
    async getQueue() { return queue },
    async getPatientRecord() { return null },
    async getDiagnosisResult(patientId: string) {
      return results.get(patientId) ?? null
    }
  }
}

describe('computeCohortMetrics', () => {
  test('空队列返回全 0 derived', async () => {
    const ds = makeDataSource([])
    const metrics = await computeCohortMetrics(ds)

    expect(metrics.totalPatients.data).toBe(0)
    expect(metrics.totalPatients.origin).toBe('derived')
    expect(metrics.highRiskCount.data).toBe(0)
    expect(metrics.positiveRate.data).toBe(0)
    expect(metrics.diseaseSpectrum.data).toEqual([])
  })

  test('全待诊队列：阳性率 0，疾病谱为空', async () => {
    const queue = makeQueue([
      { aiStatus: '未诊断' },
      { aiStatus: '未诊断' }
    ])
    const ds = makeDataSource(queue)
    const metrics = await computeCohortMetrics(ds)

    expect(metrics.totalPatients.data).toBe(2)
    expect(metrics.positiveRate.data).toBe(0)
    expect(metrics.diseaseSpectrum.data).toEqual([])
    expect(metrics.autoReportCount.data).toBe(0)
  })

  test('全已报告队列：正确统计风险分布与疾病谱', async () => {
    const queue = makeQueue([
      { id: '1', aiStatus: '已诊断' },
      { id: '2', aiStatus: '已诊断' },
      { id: '3', aiStatus: '已诊断' }
    ])
    const results = new Map([
      ['1', makeResult({ diseaseName: '血色病', probability: 85 })],
      ['2', makeResult({ diseaseName: '血色病', probability: 45 })],
      ['3', makeResult({ diseaseName: '脂肪肝', probability: 15 })]
    ])
    const ds = makeDataSource(queue, results)
    const metrics = await computeCohortMetrics(ds)

    expect(metrics.totalPatients.data).toBe(3)
    expect(metrics.highRiskCount.data).toBe(1)
    expect(metrics.midRiskCount.data).toBe(1)
    expect(metrics.lowRiskCount.data).toBe(1)
    // 阳性率 = 高危 / 已报告 = 1 / 3
    expect(metrics.positiveRate.data).toBeCloseTo(1 / 3, 2)
    expect(metrics.autoReportCount.data).toBe(3)
    expect(metrics.diseaseSpectrum.data).toHaveLength(2)
    expect(metrics.diseaseSpectrum.data[0].name).toBe('血色病')
    expect(metrics.diseaseSpectrum.data[0].pct).toBe(66.7)
  })

  test('混合队列：只统计已报告患者', async () => {
    const queue = makeQueue([
      { id: '1', aiStatus: '已诊断' },
      { id: '2', aiStatus: '未诊断' },
      { id: '3', aiStatus: '已诊断' }
    ])
    const results = new Map([
      ['1', makeResult({ diseaseName: '血色病', probability: 85 })],
      ['3', makeResult({ diseaseName: '脂肪肝', probability: 15 })]
    ])
    const ds = makeDataSource(queue, results)
    const metrics = await computeCohortMetrics(ds)

    expect(metrics.totalPatients.data).toBe(3)
    // 阳性率 = 高危 / 已报告 = 1 / 2
    expect(metrics.positiveRate.data).toBeCloseTo(1 / 2, 2)
    expect(metrics.autoReportCount.data).toBe(2)
  })
})
