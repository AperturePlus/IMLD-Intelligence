export interface ReasoningStep {
  title: string
  evidenceBars?: { label: string; weight: number; weak?: boolean }[]
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
  (e: 'replay'): void
  (e: 'expandReport'): void
}

export type ReasoningTimelineEvents = {
  replay: []
  expandReport: []
}
