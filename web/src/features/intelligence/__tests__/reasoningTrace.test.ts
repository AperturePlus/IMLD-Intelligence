import { describe, test, expect } from 'bun:test'
import { buildReasoningTrace } from '../reasoningTrace'
import type { DiagnosisResult } from '@/types/diagnosis'

function makeResult(overrides: Partial<DiagnosisResult> = {}): DiagnosisResult {
  return {
    diseaseName: '血色病',
    riskLevel: '高',
    probability: 85,
    indicators: [],
    evidenceItems: [
      { category: '生化', label: '铁蛋白 1240μg/L', value: '显著升高', source: 'LIS', severity: 'exception' },
      { category: '基因', label: 'HFE C282Y 纯合', value: '致病性变异', source: '基因检测', severity: 'exception' },
      { category: '病史', label: '糖尿病史', source: '病史', severity: 'warning' }
    ],
    evidenceSummary: { modelFeatureCount: 40, abnormalEvidenceCount: 5, reviewRequired: false },
    confidence: { visible: true, rawValue: 0.85, displayValue: 85, reviewRequired: false, adjusted: false, label: '高置信度' },
    genes: ['HFE'],
    diet: '低铁饮食',
    sequencing: '建议完善 HFE 基因检测',
    differentials: ['酒精性肝病', '非酒精性脂肪性肝病'],
    keySigns: ['铁蛋白升高', 'HFE C282Y 纯合'],
    dietTags: ['低铁饮食'],
    geneRecommendationTitle: 'HFE 相关遗传性血色病',
    dataConfidenceLabel: '高',
    ...overrides
  }
}

describe('buildReasoningTrace', () => {
  test('正常 result 生成 4 步 trace', () => {
    const result = makeResult()
    const trace = buildReasoningTrace(result)

    expect(trace.steps).toHaveLength(4)
    expect(trace.steps[0].title).toBe('特征提取')
    expect(trace.steps[1].title).toBe('证据加权')
    expect(trace.steps[2].title).toBe('鉴别赛跑')
    expect(trace.steps[3].title).toBe('置信收敛')
  })

  test('trace.steps[2].candidates[0] 与 result 一致', () => {
    const result = makeResult({ diseaseName: '血色病', probability: 85 })
    const trace = buildReasoningTrace(result)

    expect(trace.steps[2].candidates![0].name).toBe(result.diseaseName)
    expect(trace.steps[2].candidates![0].prob).toBe(result.probability)
    expect(trace.steps[2].candidates![0].win).toBe(true)
  })

  test('空 evidenceItems 不报错', () => {
    const result = makeResult({ evidenceItems: [] })
    const trace = buildReasoningTrace(result)

    expect(trace.steps).toHaveLength(4)
    expect(trace.steps[0].evidenceBars).toEqual([])
  })

  test('空 differentials 时生成默认降级', () => {
    const result = makeResult({ differentials: [] })
    const trace = buildReasoningTrace(result)

    const candidates = trace.steps[2].candidates!
    expect(candidates.length).toBeGreaterThanOrEqual(1)
    expect(candidates[0].name).toBe(result.diseaseName)
  })

  test('高风险分级正确映射', () => {
    const result = makeResult({ riskLevel: '高', probability: 85 })
    const trace = buildReasoningTrace(result)

    expect(trace.steps[3].confidence!.level).toBe('high')
  })

  test('中风险分级正确映射', () => {
    const result = makeResult({ riskLevel: '中', probability: 50 })
    const trace = buildReasoningTrace(result)

    expect(trace.steps[3].confidence!.level).toBe('mid')
  })

  test('低风险分级正确映射', () => {
    const result = makeResult({ riskLevel: '低', probability: 15 })
    const trace = buildReasoningTrace(result)

    expect(trace.steps[3].confidence!.level).toBe('low')
  })

  test('纯函数：相同输入相同输出', () => {
    const result = makeResult()
    const trace1 = buildReasoningTrace(result)
    const trace2 = buildReasoningTrace(result)

    expect(trace1).toEqual(trace2)
  })
})
