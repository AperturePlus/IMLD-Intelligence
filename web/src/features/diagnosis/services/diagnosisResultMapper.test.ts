import { describe, expect, test } from 'bun:test'
import { buildDiseaseDisplayFields } from './diseaseDisplay'
import {
  buildDiagnosisResultFromSession,
  normalizeDiagnosisResultPayload,
  type DiagnosisSessionApi
} from './diagnosisResultMapper'

describe('disease display fields', () => {
  test('resolves disease-specific fallback content', () => {
    const cases = [
      { disease: '肝豆状核变性 (Wilson病)', gene: 'ATP7B', tag: '低铜饮食' },
      { disease: '遗传性血色病', gene: 'HFE', tag: '限制红肉' },
      { disease: 'α1-抗胰蛋白酶缺乏症', gene: 'SERPINA1', tag: '高蛋白低脂' },
      { disease: '代谢相关脂肪性肝病', gene: '', tag: '控制总热量' },
      { disease: 'Gilbert综合征', gene: 'UGT1A1', tag: '规律作息' }
    ]

    for (const item of cases) {
      const fields = buildDiseaseDisplayFields({ diseaseName: item.disease, probability: 86 })
      expect(fields.dietTags).toContain(item.tag)
      if (item.gene) {
        expect(fields.genes.some((gene) => gene.includes(item.gene))).toBe(true)
      } else {
        expect(fields.genes).toHaveLength(0)
      }
      expect(fields.dataConfidenceLabel).toBe('高 (0.86)')
    }
  })

  test('prefers interface-provided fields over fallback disease profile', () => {
    const fields = buildDiseaseDisplayFields({
      diseaseName: '遗传性血色病',
      probability: 61,
      inference: {
        differentials: ['接口鉴别诊断'],
        key_signs: ['接口关键体征'],
        diet_tags: ['接口饮食标签'],
        gene_recommendation_title: '接口基因标题'
      }
    })

    expect(fields.differentials).toEqual(['接口鉴别诊断'])
    expect(fields.keySigns).toEqual(['接口关键体征'])
    expect(fields.dietTags).toEqual(['接口饮食标签'])
    expect(fields.geneRecommendationTitle).toBe('接口基因标题')
  })
})

describe('diagnosis result mapper', () => {
  test('maps complete diagnosis session with interface content first', () => {
    const session: DiagnosisSessionApi = {
      id: 1,
      patientId: 1001,
      status: 'COMPLETED',
      results: [
        {
          id: 11,
          diseaseName: '遗传性血色病',
          confidence: 0.89,
          rankNo: 1,
          riskLevel: 'HIGH',
          evidenceJson: {
            inference: {
              risk_probability: 0.91,
              suggestions: ['建议进行 HFE 基因检测。'],
              clinical_abnormalities: [
                { feature: '铁蛋白', value: 850, normal_range: [30, 300], direction: 'high', severity: '高' }
              ],
              gene_abnormalities: [{ gene: 'HFE', c_change: 'c.845G>A' }],
              differentials: ['接口鉴别'],
              diet_tags: ['接口饮食']
            }
          }
        }
      ],
      recommendations: [{ recType: 'DIET', content: '接口饮食建议' }]
    }

    const result = buildDiagnosisResultFromSession(session)

    expect(result.diseaseName).toBe('遗传性血色病')
    expect(result.probability).toBe(91)
    expect(result.indicators[0]?.name).toBe('铁蛋白')
    expect(result.genes).toContain('HFE (c.845G>A)')
    expect(result.differentials).toEqual(['接口鉴别'])
    expect(result.dietTags).toEqual(['接口饮食'])
    expect(result.diet).toBe('接口饮食建议')
  })

  test('falls back to disease profile when diagnosis session omits display fields', () => {
    const session: DiagnosisSessionApi = {
      id: 2,
      patientId: 1002,
      status: 'COMPLETED',
      results: [{ id: 21, diseaseName: 'α1-抗胰蛋白酶缺乏症', confidence: 0.83, rankNo: 1 }]
    }

    const result = buildDiagnosisResultFromSession(session)

    expect(result.probability).toBe(83)
    expect(result.genes).toContain('SERPINA1 (Pi*ZZ)')
    expect(result.dietTags).toContain('高蛋白低脂')
    expect(result.keySigns.some((item) => item.includes('α1-抗胰蛋白酶'))).toBe(true)
  })

  test('normalizes legacy mock diagnosis payloads into current result shape', () => {
    const result = normalizeDiagnosisResultPayload({
      diseaseName: 'Gilbert综合征',
      probability: 48,
      indicators: []
    })

    expect(result.probability).toBe(48)
    expect(result.genes).toContain('UGT1A1')
    expect(result.dietTags).toContain('规律作息')
    expect(result.geneRecommendationTitle).toBe('UGT1A1 多态性检测建议')
  })
})
