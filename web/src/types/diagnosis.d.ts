export interface DiagnosisQueuePatient {
  id: string
  name: string
  gender: string
  age: number
  avatar: string
  aiStatus: string
  encounterId?: number
  doctorId?: number
  modelRegistryId?: number
}

export interface DiagnosisQueueResponse {
  items: DiagnosisQueuePatient[]
}

export type ProgressStatus = '' | 'success' | 'warning' | 'exception'

export interface DiagnosisIndicator {
  name: string
  value: number
  unit: string
  normal: string
  percentage: number
  status: ProgressStatus
}

export interface DiagnosisResult {
  diseaseName: string
  probability: number
  indicators: DiagnosisIndicator[]
  genes: string[]
  diet: string
  sequencing: string
  differentials: string[]
  keySigns: string[]
  dietTags: string[]
  geneRecommendationTitle: string
  dataConfidenceLabel: string
}

export interface AiFindings {
  biochemical: string
  clinical: string
  probability: string
  disease: string
}

export interface ExpertReport {
  id: string
  patientId: string
  visitId: string
  patientName: string
  gender: string
  age: number
  date: string
  status: string
  aiFindings: AiFindings
  expertConclusion: string
  treatmentPlan: string
  signedAt?: string
  sessionId?: number
  resultId?: number
  doctorId?: number
}

export interface ExpertReportListResponse {
  items: ExpertReport[]
}

export interface SignExpertReportPayload {
  expertConclusion: string
  treatmentPlan: string
}

export interface SignExpertReportResponse {
  report: ExpertReport
}
