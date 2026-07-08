import type { StandardRiskLevel } from '@/features/diagnosis/services/riskLevel'

export type IntelligenceOrigin = 'derived' | 'synthesized'

export interface Originated<T> {
  data: T
  origin: IntelligenceOrigin
}

// 疾病谱项（喂 DiseaseSpectrum）
export interface DiseaseSpectrumItem {
  name: string
  pct: number
}

// 队列聚合（喂 KpiGrid + RiskDonut + DiseaseSpectrum）
export interface CohortMetrics {
  totalPatients: Originated<number>
  highRiskCount: Originated<number>
  midRiskCount: Originated<number>
  lowRiskCount: Originated<number>
  positiveRate: Originated<number>        // AI 阳性率
  autoReportCount: Originated<number>     // 自动报告数
  diseaseSpectrum: Originated<DiseaseSpectrumItem[]>
}

// 工作清单项（喂 WorklistBanner）
export interface WorklistCase {
  id: string
  name: string
  riskScore: number
  riskLevel: StandardRiskLevel
  reason: string           // 派生理由，如 "铁蛋白 1240↑↑、HFE C282Y 纯合"
  origin: IntelligenceOrigin
}

// 洞察条目（喂 InsightFeed）
export interface IntelligenceInsight {
  time: string             // 生成时间 HH:mm
  title: string
  confidence: number       // 0-1
  severity: 'low' | 'mid' | 'high'
  origin: IntelligenceOrigin
}

// 预测点（喂 ForecastChart）
export interface ForecastPoint {
  x: string                // 周标签，如 "5月W1"
  y: number
}

export interface ForecastSeries {
  history: Originated<ForecastPoint[]>
  forecast: Originated<ForecastPoint[]>
}
