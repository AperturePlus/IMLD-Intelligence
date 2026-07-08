export interface EvidenceBarProps {
  label: string
  value?: number | null
  weight: number
  weak?: boolean
  /** 右侧度量标题，默认 "权重"；传 null 则隐藏（用于计数类条目） */
  metricLabel?: string | null
}
