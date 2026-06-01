import type { DiagnosisIndicator, DiagnosisResult, ExpertReport, ProgressStatus } from '@/types/diagnosis'
import { buildDiseaseDisplayFields, type InferenceDisplayLike } from './diseaseDisplay'

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
    const rangeLabel = range.length >= 2 ? `${range[0]}-${range[1]}` : '--'
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
      unit: '',
      normal: rangeLabel,
      percentage,
      status
    }
  })
}

export const normalizeDiagnosisResultPayload = (payload: Partial<DiagnosisResult>): DiagnosisResult => {
  const diseaseName = payload.diseaseName || DEFAULT_DISEASE_NAME
  const probability = numberToPercent(payload.probability)
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
    probability,
    indicators: payload.indicators || [],
    ...displayFields
  }
}

export const buildDiagnosisResultFromExpertReport = (report: ExpertReport): DiagnosisResult => {
  const diseaseName = report.aiFindings?.disease || DEFAULT_DISEASE_NAME
  const probability = numberToPercent(report.aiFindings?.probability)
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
    probability,
    indicators: [
      {
        name: '关键生化线索',
        value: 1,
        unit: '',
        normal: report.aiFindings?.biochemical || '--',
        percentage: 78,
        status: 'warning'
      }
    ],
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
  const displayFields = buildDiseaseDisplayFields({
    diseaseName,
    probability,
    inference,
    recommendations: session.recommendations || [],
    suggestions: inference.suggestions
  })

  return {
    diseaseName,
    probability,
    indicators: mapInferenceIndicators(inference),
    ...displayFields
  }
}
