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
  avatar?: string | null
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
