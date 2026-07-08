import { describe, test, expect } from 'bun:test'
import { computeForecast } from '../forecast'

describe('computeForecast', () => {
  test('数值稳定性：同种子同结果', () => {
    const result1 = computeForecast(100)
    const result2 = computeForecast(100)

    expect(result1.history.data).toEqual(result2.history.data)
    expect(result1.forecast.data).toEqual(result2.forecast.data)
  })

  test('不同队列规模产生不同结果', () => {
    const result1 = computeForecast(100)
    const result2 = computeForecast(200)

    // 至少历史序列的某些值应该不同
    const hasDiff = result1.history.data.some((p, i) =>
      p.y !== result2.history.data[i].y
    )
    expect(hasDiff).toBe(true)
  })

  test('历史序列长度为 12', () => {
    const result = computeForecast(100)
    expect(result.history.data).toHaveLength(12)
  })

  test('预测序列长度为 4', () => {
    const result = computeForecast(100)
    expect(result.forecast.data).toHaveLength(4)
  })

  test('所有数值非负', () => {
    const result = computeForecast(100)

    for (const point of result.history.data) {
      expect(point.y).toBeGreaterThanOrEqual(0)
    }
    for (const point of result.forecast.data) {
      expect(point.y).toBeGreaterThanOrEqual(0)
    }
  })

  test('origin 标注为 synthesized', () => {
    const result = computeForecast(100)
    expect(result.history.origin).toBe('synthesized')
    expect(result.forecast.origin).toBe('synthesized')
  })

  test('序列单调性合理：预测值不应剧烈跳变', () => {
    const result = computeForecast(100)
    const forecast = result.forecast.data

    for (let i = 1; i < forecast.length; i++) {
      const diff = Math.abs(forecast[i].y - forecast[i - 1].y)
      expect(diff).toBeLessThan(20) // 不应单周跳变超过 20
    }
  })
})
