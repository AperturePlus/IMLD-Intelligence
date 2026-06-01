import type { DiagnosisEvidenceItem, DiagnosisIndicator, DiagnosisResult, ExpertReport, ProgressStatus } from '@/types/diagnosis'
import { buildDiseaseDisplayFields, type InferenceDisplayLike } from './diseaseDisplay'
import { resolveDiagnosisConfidence } from './confidenceConfig'
import {
  DEFAULT_MODEL_FEATURE_COUNT,
  buildEvidenceItemsFromDiagnosis,
  buildEvidenceSummary
} from './diagnosisEvidence'
import { normalizeRiskLevel } from './riskLevel'

export interface DiagnosisResultItemApi {
  id: number
  diseaseCode?: string
  diseaseName?: string
  confidence?: number
  rankNo?: number
  riskLevel?: string
  evidenceJson?: unknown
}

export interface DiagnosisRecommendationItemApi {
  recType?: string
  content?: string
  reason?: string
}

export interface DiagnosisFeedbackItemApi {
  doctorId?: number
  action?: string
  modifiedValue?: unknown
  createdAt?: string
}

export interface DiagnosisSessionApi {
  id: number
  patientId: number
  encounterId?: number
  doctorId?: number
  modelRegistryId?: number
  status: string
  startedAt?: string
  completedAt?: string
  results?: DiagnosisResultItemApi[]
  recommendations?: DiagnosisRecommendationItemApi[]
  feedbacks?: DiagnosisFeedbackItemApi[]
}

interface InferencePayloadApi extends InferenceDisplayLike {
  risk_probability?: number
  riskProbability?: number
  risk_level?: string
  riskLevel?: string
  model_feature_count?: number
  modelFeatureCount?: number
  abnormal_evidence_count?: number
  abnormalEvidenceCount?: number
}

const DEFAULT_DISEASE_NAME = '遗传代谢性肝病风险提示'

const asRecord = (value: unknown): Record<string, unknown> | null => {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    return null
  }
  return value as Record<string, unknown>
}

const numberToPercent = (value: unknown): number => {
  const parsed = typeof value === 'number' ? value : Number.parseFloat(String(value ?? '0'))
  if (!Number.isFinite(parsed)) {
    return 0
  }
  const normalized = parsed > 1 ? parsed : parsed * 100
  return Math.round(Math.max(0, Math.min(100, normalized)))
}

const toProgressStatus = (severity: string | undefined): ProgressStatus => {
  if (!severity) {
    return ''
  }
  if (severity.includes('高')) {
    return 'exception'
  }
  if (severity.includes('中')) {
    return 'warning'
  }
  return ''
}

const sortResults = (results: DiagnosisResultItemApi[] = []): DiagnosisResultItemApi[] => {
  return [...results].sort((a, b) => (a.rankNo || 0) - (b.rankNo || 0))
}

export const extractInferencePayload = (session: DiagnosisSessionApi): InferencePayloadApi => {
  const primaryResult = sortResults(session.results)[0]
  const evidence = asRecord(primaryResult?.evidenceJson)
  const nested = asRecord(evidence?.inference)
  return ((nested || evidence || {}) as InferencePayloadApi)
}

export const mapInferenceIndicators = (inference: InferencePayloadApi): DiagnosisIndicator[] => {
  const clinical = inference.clinical_abnormalities || inference.clinicalAbnormalities || []
  return clinical.map((item) => {
    const range = item.normal_range || item.normalRange || []
    const rangeLabel = item.normal_range_label || item.normalRangeLabel || (range.length >= 2 ? `${range[0]}-${range[1]}` : '--')
    const status = toProgressStatus(item.severity)
    const percentage =
      status === 'exception'
        ? 92
        : status === 'warning'
          ? 76
          : item.direction === 'low'
            ? 38
            : item.direction === 'high'
              ? 65
              : 50
    return {
      name: item.feature || '临床指标',
      value: Number(item.value ?? 0),
      unit: item.unit || '',
      normal: rangeLabel,
      percentage,
      status
    }
  })
}

const mapInferenceEvidenceItems = (inference: InferencePayloadApi): DiagnosisEvidenceItem[] => {
  const items = inference.evidence_items || inference.evidenceItems || []
  return items
    .map((item) => ({
      category: item.category || '证据',
      label: item.label || '',
      value: item.value,
      source: item.source,
      severity: item.severity === 'exception' || item.severity === 'warning' || item.severity === 'success'
        ? item.severity
        : 'info'
    } satisfies DiagnosisEvidenceItem))
    .filter((item) => item.label.trim())
}

const resolveModelFeatureCount = (inference: InferencePayloadApi): number => {
  const value = inference.model_feature_count ?? inference.modelFeatureCount
  return typeof value === 'number' && Number.isFinite(value) && value > 0
    ? Math.round(value)
    : DEFAULT_MODEL_FEATURE_COUNT
}

const withResultScaffolding = (
  payload: Pick<DiagnosisResult, 'diseaseName' | 'probability' | 'indicators'> &
    Partial<Pick<DiagnosisResult, 'evidenceItems' | 'confidence' | 'evidenceSummary'>>,
  inference?: InferencePayloadApi
) => {
  const confidence = payload.confidence || resolveDiagnosisConfidence(payload.probability)
  const evidenceItems = payload.evidenceItems?.length
    ? payload.evidenceItems
    : buildEvidenceItemsFromDiagnosis({
        diseaseName: payload.diseaseName,
        indicators: payload.indicators
      })
  const modelFeatureCount = inference ? resolveModelFeatureCount(inference) : DEFAULT_MODEL_FEATURE_COUNT
  const evidenceSummary = payload.evidenceSummary || buildEvidenceSummary(evidenceItems, confidence, modelFeatureCount)
  return {
    confidence,
    evidenceItems,
    evidenceSummary
  }
}

const parseLegacyBiochemicalIndicators = (text: string | undefined): DiagnosisIndicator[] => {
  if (!text) {
    return []
  }
  return text
    .split(/[，,。；;]/)
    .map((segment) => segment.trim())
    .map((segment): DiagnosisIndicator | null => {
      const matched = segment.match(/^(.+?)\s*([<>]?\s*\d+(?:\.\d+)?)\s*([^\s(（]*)/)
      if (!matched) {
        return null
      }
      const value = Number.parseFloat(matched[2].replace(/[<>\s]/g, ''))
      if (!Number.isFinite(value)) {
        return null
      }
      const status: ProgressStatus = /升高|降低|异常|极低|显著|高/.test(segment) ? 'warning' : ''
      return {
        name: matched[1].trim(),
        value,
        unit: matched[3] || '',
        normal: '--',
        percentage: status ? 76 : 50,
        status
      }
    })
    .filter((item): item is DiagnosisIndicator => item !== null)
}

export const normalizeDiagnosisResultPayload = (payload: Partial<DiagnosisResult>): DiagnosisResult => {
  const diseaseName = payload.diseaseName || DEFAULT_DISEASE_NAME
  const probability = numberToPercent(payload.probability)
  const indicators = payload.indicators || []
  const scaffolding = withResultScaffolding({
    diseaseName,
    probability,
    indicators,
    evidenceItems: payload.evidenceItems,
    confidence: payload.confidence,
    evidenceSummary: payload.evidenceSummary
  })
  const displayFields = buildDiseaseDisplayFields({
    diseaseName,
    probability,
    inference: {
      differentials: payload.differentials,
      keySigns: payload.keySigns,
      dietTags: payload.dietTags,
      geneRecommendationTitle: payload.geneRecommendationTitle
    },
    genes: payload.genes,
    diet: payload.diet,
    sequencing: payload.sequencing
  })

  return {
    diseaseName,
    riskLevel: normalizeRiskLevel(payload.riskLevel, probability),
    probability,
    indicators,
    ...scaffolding,
    ...displayFields
  }
}

export const buildDiagnosisResultFromExpertReport = (report: ExpertReport): DiagnosisResult => {
  const diseaseName = report.aiFindings?.disease || DEFAULT_DISEASE_NAME
  const probability = numberToPercent(report.aiFindings?.probability)
  const indicators = parseLegacyBiochemicalIndicators(report.aiFindings?.biochemical)
  const scaffolding = withResultScaffolding({
    diseaseName,
    probability,
    indicators
  })
  const displayFields = buildDiseaseDisplayFields({
    diseaseName,
    probability,
    diet: report.treatmentPlan,
    suggestions: [report.aiFindings?.clinical, report.aiFindings?.biochemical].filter(
      (item): item is string => Boolean(item)
    )
  })

  return {
    diseaseName,
    riskLevel: normalizeRiskLevel(undefined, probability),
    probability,
    indicators,
    ...scaffolding,
    ...displayFields
  }
}

export const buildDiagnosisResultFromSession = (session: DiagnosisSessionApi): DiagnosisResult => {
  const sortedResults = sortResults(session.results)
  const primaryResult = sortedResults[0]
  const inference = extractInferencePayload(session)
  const rawProbability =
    typeof inference.risk_probability === 'number'
      ? inference.risk_probability
      : typeof inference.riskProbability === 'number'
        ? inference.riskProbability
        : primaryResult?.confidence || 0
  const probability = numberToPercent(rawProbability)
  const diseaseName = primaryResult?.diseaseName || DEFAULT_DISEASE_NAME
  const riskLevel = normalizeRiskLevel(
    primaryResult?.riskLevel || inference.risk_level || inference.riskLevel,
    probability
  )
  const indicators = mapInferenceIndicators(inference)
  const evidenceItems = mapInferenceEvidenceItems(inference)
  const confidence = resolveDiagnosisConfidence(probability)
  const scaffolding = withResultScaffolding(
    {
      diseaseName,
      probability,
      indicators,
      evidenceItems,
      confidence
    },
    inference
  )
  if (typeof inference.abnormal_evidence_count === 'number' || typeof inference.abnormalEvidenceCount === 'number') {
    scaffolding.evidenceSummary.abnormalEvidenceCount = Math.max(
      0,
      Math.round(inference.abnormal_evidence_count ?? inference.abnormalEvidenceCount ?? 0)
    )
  }
  const displayFields = buildDiseaseDisplayFields({
    diseaseName,
    probability,
    inference,
    recommendations: session.recommendations || [],
    suggestions: inference.suggestions
  })

  return {
    diseaseName,
    riskLevel,
    probability,
    indicators,
    ...scaffolding,
    ...displayFields
  }
}
