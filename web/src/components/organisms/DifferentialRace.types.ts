export interface DifferentialCandidate {
  name: string
  prob: number
  win?: boolean
  why?: string
}

export interface DifferentialRaceProps {
  candidates: DifferentialCandidate[]
}
