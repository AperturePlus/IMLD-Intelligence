export interface ReasoningStep {
  title: string
  evidenceBars?: { label: string; weight: number; weak?: boolean; metricLabel?: string | null }[]
  candidates?: { name: string; prob: number; win?: boolean; why?: string }[]
  confidence?: { percentage: number; level: 'low' | 'mid' | 'high'; label: string }
}

export interface ReasoningTrace {
  steps: ReasoningStep[]
}

export interface ReasoningTimelineProps {
  trace: ReasoningTrace
}

export interface ReasoningTimelineEmits {
  (e: 'complete'): void
  (e: 'skip'): void
}

export type ReasoningTimelineEvents = {
  complete: []
  skip: []
}
