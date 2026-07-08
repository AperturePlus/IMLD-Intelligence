import type { CohortAggregateSnapshot } from '../aggregateSnapshot'

export interface CopilotResponder {
  respond(query: string, snapshot: CohortAggregateSnapshot): AsyncIterable<string>
}
