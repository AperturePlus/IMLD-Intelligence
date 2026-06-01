export type StandardRiskLevel = '低' | '中' | '高'

export const LOW_RISK_THRESHOLD = 20
export const HIGH_RISK_THRESHOLD = 70

export const riskLevelFromProbability = (value: unknown): StandardRiskLevel => {
  const parsed = typeof value === 'number' ? value : Number.parseFloat(String(value ?? '0'))
  if (!Number.isFinite(parsed)) {
    return '中'
  }
  const probability = parsed > 1 ? parsed : parsed * 100
  if (probability < LOW_RISK_THRESHOLD) {
    return '低'
  }
  if (probability >= HIGH_RISK_THRESHOLD) {
    return '高'
  }
  return '中'
}

export const normalizeRiskLevel = (
  riskLevel: unknown,
  probabilityFallback?: unknown
): StandardRiskLevel => {
  if (typeof riskLevel === 'string' && riskLevel.trim()) {
    const normalized = riskLevel.trim().toUpperCase()
    if (normalized.includes('HIGH') || riskLevel.includes('高')) {
      return '高'
    }
    if (normalized.includes('LOW') || riskLevel.includes('低')) {
      return '低'
    }
    if (normalized.includes('MEDIUM') || normalized.includes('MIDDLE') || riskLevel.includes('中')) {
      return '中'
    }
  }

  return riskLevelFromProbability(probabilityFallback)
}

export const toChineseRiskLabel = (riskLevel: unknown, probabilityFallback?: unknown): string => {
  return `${normalizeRiskLevel(riskLevel, probabilityFallback)}风险`
}
