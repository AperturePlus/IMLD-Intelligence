import type { CohortAggregateSnapshot } from '../aggregateSnapshot'
import type { CopilotResponder } from './responder'
import { matchIntent, type CopilotIntent } from './intents'

const SOURCE_LINE = '\n\n---\n数据来源：本机脱敏聚合统计 · 未访问任何患者明细'

const templates: Record<CopilotIntent, (s: CohortAggregateSnapshot) => string> = {
  queueOverview: (s) =>
    `当前在管患者共 ${s.totalPatients} 人，其中高危 ${s.highRiskCount} 人、中危 ${s.midRiskCount} 人、低危 ${s.lowRiskCount} 人。AI 阳性率为 ${(s.positiveRate * 100).toFixed(1)}%。`,

  altAnalysis: (s) =>
    `目前队列中未发现 ALT 专项统计。整体在管 ${s.totalPatients} 人，高危 ${s.highRiskCount} 人。如需详细生化分析，请在诊断报告中查看具体患者的 ALT 指标偏离情况。`,

  weeklyBriefing: (s) => {
    const recent = s.weeklyNewCases.slice(-4)
    const avg = recent.reduce((a, b) => a + b, 0) / recent.length
    return `近四周平均每周新检出 ${avg.toFixed(1)} 例。本周在管患者 ${s.totalPatients} 人，自动报告 ${s.autoReportCount} 份。疾病谱首位为 ${s.diseaseSpectrum[0]?.name ?? '暂无数据'}，占比 ${s.diseaseSpectrum[0]?.pct.toFixed(1) ?? 0}%。`
  },

  diseaseSpectrum: (s) => {
    if (s.diseaseSpectrum.length === 0) return '当前暂无疾病谱数据。'
    const lines = s.diseaseSpectrum
      .slice(0, 5)
      .map((d, i) => `${i + 1}. ${d.name}：${d.pct.toFixed(1)}%（约 ${d.count} 例）`)
    return `疾病谱构成如下：\n${lines.join('\n')}`
  },

  riskTrend: (s) => {
    const recent = s.weeklyNewCases.slice(-4)
    const prev = s.weeklyNewCases.slice(-8, -4)
    const recentAvg = recent.reduce((a, b) => a + b, 0) / recent.length
    const prevAvg = prev.reduce((a, b) => a + b, 0) / prev.length
    const trend = recentAvg > prevAvg ? '上升' : recentAvg < prevAvg ? '下降' : '平稳'
    return `近四周新检出趋势呈${trend}态势（近四周平均 ${recentAvg.toFixed(1)} 例，前四周平均 ${prevAvg.toFixed(1)} 例）。当前队列高危 ${s.highRiskCount} 人，建议持续关注。`
  },

  unknown: (s) =>
    `我是 IMLD 智能助手，目前可以帮您了解：今日队列风险概览、疾病谱分析、本周筛查趋势等。当前在管患者 ${s.totalPatients} 人，您可以从左侧建议 prompt 中选择提问。`,
}

export class ScriptedResponder implements CopilotResponder {
  async *respond(query: string, snapshot: CohortAggregateSnapshot): AsyncIterable<string> {
    const intent = matchIntent(query)
    const text = templates[intent](snapshot) + SOURCE_LINE

    // 流式输出：按字符逐个 yield，模拟打字效果
    for (const char of text) {
      yield char
      // 小延迟由消费方控制；这里只负责拆分
      await delay(8)
    }
  }
}

export function createScriptedResponder(): CopilotResponder {
  return new ScriptedResponder()
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
