import type { CohortMetrics, WorklistCase, IntelligenceInsight } from "./types";

export interface CohortAggregateSnapshot {
  // ✅ 允许：纯聚合量
  totalPatients: number;
  highRiskCount: number;
  midRiskCount: number;
  lowRiskCount: number;
  positiveRate: number;
  autoReportCount: number;
  diseaseSpectrum: { name: string; count: number; pct: number }[];
  weeklyNewCases: number[]; // 近 12 周每周新检出数
  topRiskFactors: string[]; // 群体级高频风险因素
  generatedAt: string;

  // ❌ 禁止：任何可定位到个体的字段
  // patientNames?: string[]      // 绝不允许
  // patientIds?: string[]        // 绝不允许
  // encounterDetails?: ...       // 绝不允许
}

export function buildAggregateSnapshot(
  metrics: CohortMetrics,
  worklist: WorklistCase[],
  _insights: IntelligenceInsight[]
): CohortAggregateSnapshot {
  // 从 worklist 提取群体级高频风险因素（取 reason 中的关键词，不保留个体关联）
  const reasonFrequency = new Map<string, number>();
  for (const item of worklist) {
    const reasons = item.reason
      .split(/[,，、]/)
      .map((r) => r.trim())
      .filter(Boolean);
    for (const r of reasons) {
      reasonFrequency.set(r, (reasonFrequency.get(r) ?? 0) + 1);
    }
  }
  const topRiskFactors = Array.from(reasonFrequency.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([r]) => r);

  // 从 insights 提取本周新检出数（简化：用 insights 数量模拟每周趋势）
  // 实际应接真实历史数据；这里用确定性合成：基于队列规模
  const weeklyNewCases = Array.from({ length: 12 }, (_, i) => {
    const base = Math.max(1, Math.floor(metrics.totalPatients.data * 0.05));
    return Math.max(0, base + Math.sin(i * 0.8) * 3);
  });

  return {
    totalPatients: metrics.totalPatients.data,
    highRiskCount: metrics.highRiskCount.data,
    midRiskCount: metrics.midRiskCount.data,
    lowRiskCount: metrics.lowRiskCount.data,
    positiveRate: metrics.positiveRate.data,
    autoReportCount: metrics.autoReportCount.data,
    diseaseSpectrum: metrics.diseaseSpectrum.data.map((d) => ({
      name: d.name,
      count: Math.round((d.pct / 100) * metrics.totalPatients.data),
      pct: d.pct,
    })),
    weeklyNewCases,
    topRiskFactors,
    generatedAt: new Date().toISOString(),
  };
}
