export interface WorklistBannerProps {
  greeting: string
  highRiskCount: number
  cases: { id: string; name: string; riskLevel: string }[]
}
