import { describe, expect, test } from 'bun:test'
import { createScriptedResponder } from '../copilot/scriptedResponder'
import { matchIntent } from '../copilot/intents'
import type { CohortAggregateSnapshot } from '../aggregateSnapshot'

function makeSnapshot(): CohortAggregateSnapshot {
  return {
    totalPatients: 100,
    highRiskCount: 5,
    midRiskCount: 10,
    lowRiskCount: 85,
    positiveRate: 0.15,
    autoReportCount: 12,
    diseaseSpectrum: [
      { name: 'A病', count: 40, pct: 40 },
      { name: 'B病', count: 30, pct: 30 },
    ],
    weeklyNewCases: [2, 3, 4, 3, 2, 3, 4, 3, 2, 3, 4, 3],
    topRiskFactors: ['铁蛋白升高'],
    generatedAt: new Date().toISOString(),
  }
}

describe('matchIntent', () => {
  test('识别队列概览意图', () => {
    expect(matchIntent('今日队列风险概览')).toBe('queueOverview')
    expect(matchIntent('queue overview')).toBe('queueOverview')
  })

  test('识别疾病谱意图', () => {
    expect(matchIntent('疾病谱分析')).toBe('diseaseSpectrum')
    expect(matchIntent('disease spectrum')).toBe('diseaseSpectrum')
  })

  test('识别趋势意图', () => {
    expect(matchIntent('风险趋势如何')).toBe('riskTrend')
    expect(matchIntent('forecast')).toBe('riskTrend')
  })

  test('识别周报意图', () => {
    expect(matchIntent('生成本周简报')).toBe('weeklyBriefing')
  })

  test('未知意图降级', () => {
    expect(matchIntent('random gibberish')).toBe('unknown')
  })
})

describe('ScriptedResponder', () => {
  test('队列概览模板填充完整', async () => {
    const responder = createScriptedResponder()
    const chunks: string[] = []
    for await (const c of responder.respond('今日队列风险概览', makeSnapshot())) {
      chunks.push(c)
    }
    const text = chunks.join('')
    expect(text).toContain('100 人')
    expect(text).toContain('高危 5 人')
    expect(text).toContain('AI 阳性率为 15.0%')
    expect(text).toContain('数据来源：本机脱敏聚合统计')
  })

  test('疾病谱模板填充完整', async () => {
    const responder = createScriptedResponder()
    const chunks: string[] = []
    for await (const c of responder.respond('疾病谱', makeSnapshot())) {
      chunks.push(c)
    }
    const text = chunks.join('')
    expect(text).toContain('A病')
    expect(text).toContain('40.0%')
    expect(text).toContain('数据来源：本机脱敏聚合统计')
  })

  test('未知意图返回引导语', async () => {
    const responder = createScriptedResponder()
    const chunks: string[] = []
    for await (const c of responder.respond('hello world', makeSnapshot())) {
      chunks.push(c)
    }
    const text = chunks.join('')
    expect(text).toContain('IMLD 智能助手')
    expect(text).toContain('数据来源：本机脱敏聚合统计')
  })

  test('流式输出非空', async () => {
    const responder = createScriptedResponder()
    let count = 0
    for await (const _ of responder.respond('test', makeSnapshot())) {
      count++
    }
    expect(count).toBeGreaterThan(0)
  })
})
