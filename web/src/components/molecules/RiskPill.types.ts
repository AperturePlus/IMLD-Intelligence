import type { StandardRiskLevel } from '@/features/diagnosis/services/riskLevel'

export interface RiskPillProps {
  level?: StandardRiskLevel | null
  probability?: number | null
}
