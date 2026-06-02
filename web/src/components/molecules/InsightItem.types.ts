export interface InsightItemProps {
  time: string
  title: string
  confidence?: number | null
  severity?: 'low' | 'mid' | 'high'
}
