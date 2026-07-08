export interface KpiStatProps {
  label: string
  value: number | string
  delta?: number | null
  trend?: 'up' | 'down' | 'flat'
  format?: 'number' | 'percent' | 'currency'
  sparklinePoints?: number[]
  /** 数值强调色调，用于突出关键指标（如高危=danger） */
  tone?: 'default' | 'danger' | 'success' | 'warning'
}
