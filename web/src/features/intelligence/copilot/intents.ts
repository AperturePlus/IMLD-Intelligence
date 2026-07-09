export type CopilotIntent =
  | 'queueOverview'
  | 'altAnalysis'
  | 'weeklyBriefing'
  | 'diseaseSpectrum'
  | 'riskTrend'
  | 'unknown'

const INTENT_KEYWORDS: Record<CopilotIntent, string[]> = {
  queueOverview: ['队列', '概览', '概况', '今日', '风险', '概览', '总览', 'overview', 'queue'],
  altAnalysis: ['alt', 'alt分析', 'alt升高', 'alt 升高', '生化', '转氨酶'],
  weeklyBriefing: ['本周', '周报', '简报', 'weekly', 'briefing', '周报', '周'],
  diseaseSpectrum: ['疾病谱', '病种', '构成', 'spectrum', 'disease', '占比'],
  riskTrend: ['趋势', '风险趋势', '预测', 'forecast', 'trend', '走向'],
  unknown: [],
}

export function matchIntent(input: string): CopilotIntent {
  const lower = input.toLowerCase().trim()

  const scores = new Map<CopilotIntent, number>()

  for (const [intent, keywords] of Object.entries(INTENT_KEYWORDS) as [CopilotIntent, string[]][]) {
    if (intent === 'unknown') continue
    let score = 0
    for (const kw of keywords) {
      if (lower.includes(kw.toLowerCase())) score++
    }
    scores.set(intent, score)
  }

  let best: CopilotIntent = 'unknown'
  let bestScore = 0
  for (const [intent, score] of scores) {
    if (score > bestScore) {
      bestScore = score
      best = intent
    }
  }

  return bestScore > 0 ? best : 'unknown'
}

export const SUGGESTED_PROMPTS = [
  '今日队列风险概览',
  '本周筛查趋势如何',
  '疾病谱中占比最高的是哪种',
]
