import type { ForecastSeries, ForecastPoint } from './types'

// 稳定种子伪随机
function seededRandom(seed: number): number {
  const x = Math.sin(seed * 9999 + 0.5) * 10000
  return x - Math.floor(x)
}

function hashString(str: string): number {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = (h << 5) - h + str.charCodeAt(i)
    h |= 0
  }
  return Math.abs(h)
}

function getDailySeed(): number {
  const now = new Date()
  return now.getFullYear() * 10000 + (now.getMonth() + 1) * 100 + now.getDate()
}

function generateWeekLabel(weeksAgo: number): string {
  const now = new Date()
  const target = new Date(now.getTime() - weeksAgo * 7 * 24 * 60 * 60 * 1000)
  const month = target.getMonth() + 1
  // 计算是该月的第几周（简化）
  const weekNum = Math.ceil(target.getDate() / 7)
  return `${month}月W${weekNum}`
}

export function computeForecast(queueSize: number): ForecastSeries {
  const baseSeed = hashString('imld-forecast') + queueSize + getDailySeed()

  // 生成 12 周历史序列
  const history: ForecastPoint[] = []
  let baseValue = 5 + (queueSize % 10) // 基础新检出数

  for (let i = 11; i >= 0; i--) {
    const seed = baseSeed + i * 137
    const noise = (seededRandom(seed) - 0.5) * 4 // ±2 波动
    const seasonal = Math.sin(i / 4 * Math.PI) * 1.5 // 微弱季节因子
    const value = Math.max(0, Math.round((baseValue + noise + seasonal) * 10) / 10)
    history.push({
      x: generateWeekLabel(i),
      y: value
    })
    // 微弱趋势变化
    baseValue += (seededRandom(seed + 1) - 0.45) * 0.5
  }

  // 简单线性回归外推未来 4 周
  const n = history.length
  let sumX = 0
  let sumY = 0
  let sumXY = 0
  let sumXX = 0

  for (let i = 0; i < n; i++) {
    sumX += i
    sumY += history[i].y
    sumXY += i * history[i].y
    sumXX += i * i
  }

  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
  const intercept = (sumY - slope * sumX) / n

  const forecast: ForecastPoint[] = []
  for (let i = 1; i <= 4; i++) {
    const projected = intercept + slope * (n - 1 + i)
    const seasonal = Math.sin((n - 1 + i) / 4 * Math.PI) * 1.5
    const value = Math.max(0, Math.round((projected + seasonal) * 10) / 10)
    forecast.push({
      x: generateWeekLabel(-i),
      y: value
    })
  }

  return {
    history: { data: history, origin: 'synthesized' },
    forecast: { data: forecast, origin: 'synthesized' }
  }
}
