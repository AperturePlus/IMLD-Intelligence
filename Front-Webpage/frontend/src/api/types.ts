export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  key?: string
  token?: string
  access?: string
  access_token?: string
  username?: string
}

export interface RegisterRequest {
  username: string
  email: string
  password1: string
  password2: string
}

export interface RegisterResponse {
  detail: string
}

export interface UserDetailResponse {
  username: string
  email: string
  role: string
}

export interface PatientListQuery {
  keyword?: string
}

export interface PatientSummary {
  id: string
  name: string
  gender: string
  age: number
  riskLevel: string
  avatar?: string
}

export interface PatientListResponse {
  items: PatientSummary[]
}

export interface PatientRecordPayload {
  name: string
  gender: string
  age: number | null
  visitDate: string
  chiefComplaint: string
  diagnosis: string
  visitId?: string
  [key: string]: unknown
}

export interface CreatePatientRecordResponse {
  recordId: string
  visitId: string
  message: string
}

export interface ScreeningOverviewQuery {
  from?: string
  to?: string
}

export interface StatCard {
  title: string
  value: number
  color: string
  icon: string
  trend: number
  suffix?: string
}

export interface RiskDistributionItem {
  level: string
  count: number
  percentage: number
  color: string
}

export interface TopGeneItem {
  name: string
  desc: string
  percentage: number
}

export interface AiEfficiencyMetrics {
  diagnosisMatchRate: number
  missRate: string
  avgDuration: string
}

export interface HighRiskPatientItem {
  date: string
  name: string
  age: number
  clue: string
  aiSuggest: string
}

export interface ScreeningOverviewResponse {
  updatedAt: string
  statCards: StatCard[]
  riskDistribution: RiskDistributionItem[]
  topGenes: TopGeneItem[]
  aiEfficiency: AiEfficiencyMetrics
  highRiskPatients: HighRiskPatientItem[]
}

export interface DietPatient {
  id: string
  name: string
  gender: string
  age: number
  avatar: string
  disease: string
  compliance: string
}

export interface DietPatientsResponse {
  items: DietPatient[]
}

export interface DietTarget {
  label: string
  value: string | number
  unit: string
  color: string
  desc: string
}

export interface DietFoods {
  red: string[]
  yellow: string[]
  green: string[]
}

export interface MealPlanItem {
  type: string
  time: string
  menu: string
  nutrition: string
}

export interface DietPlanResponse {
  targets: DietTarget[]
  foods: DietFoods
  mealPlan: MealPlanItem[]
}

export interface RegenerateDietPlanResponse {
  mealPlan: MealPlanItem[]
  regeneratedAt: string
}

export interface PushDietPlanResponse {
  delivered: boolean
  patientId: string
  deliveredAt: string
}

export interface DiagnosisQueuePatient {
  id: string
  name: string
  gender: string
  age: number
  avatar: string
  aiStatus: string
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
