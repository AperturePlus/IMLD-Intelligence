export const resolveRiskColor = (level: string): string => {
  if (level === '高') return 'var(--imld-risk-high)'
  if (level === '中') return 'var(--imld-risk-mid)'
  return 'var(--imld-risk-low)'
}
