import type { StandardRiskLevel } from '@/features/diagnosis/services/riskLevel'

export interface WorklistBannerCase {
  id: string
  name: string
  riskLevel: StandardRiskLevel
  score: number
  reason?: string
}

export interface WorklistBannerProps {
  greeting: string
  highRiskCount: number
  cases: WorklistBannerCase[]
}
