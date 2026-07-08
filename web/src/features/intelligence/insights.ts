import type { CohortMetrics, IntelligenceInsight } from "./types";

export function generateInsights(
  metrics: CohortMetrics
): IntelligenceInsight[] {
  const items: IntelligenceInsight[] = [];
  const now = new Date();
  const timePrefix = `${String(now.getHours()).padStart(2, "0")}:${String(
    now.getMinutes()
  ).padStart(2, "0")}`;

  const high = metrics.highRiskCount.data;
  const mid = metrics.midRiskCount.data;
  const total = metrics.totalPatients.data;
  const positiveRate = metrics.positiveRate.data;
  const spectrum = metrics.diseaseSpectrum.data;

  if (high > 0) {
    items.push({
      time: timePrefix,
      title: `今日队列含 ${high} 例高危患者，建议优先处理`,
      confidence: 0.92,
      severity: "high",
      origin: "synthesized",
    });
  }

  if (positiveRate > 0.5) {
    items.push({
      time: timePrefix,
      title: `本周 AI 阳性率为 ${(positiveRate * 100).toFixed(1)}%，高于基线`,
      confidence: 0.78,
      severity: "mid",
      origin: "synthesized",
    });
  } else if (positiveRate > 0 && total > 0) {
    items.push({
      time: timePrefix,
      title: `当前 AI 阳性率为 ${(positiveRate * 100).toFixed(1)}%`,
      confidence: 0.75,
      severity: "low",
      origin: "synthesized",
    });
  }

  if (spectrum.length > 0 && spectrum[0].pct > 30) {
    items.push({
      time: timePrefix,
      title: `${spectrum[0].name} 占疾病谱 ${spectrum[0].pct}%，呈聚集趋势`,
      confidence: 0.65,
      severity: "mid",
      origin: "synthesized",
    });
  }

  if (mid > 0) {
    items.push({
      time: timePrefix,
      title: `中危患者 ${mid} 例，建议纳入随访计划`,
      confidence: 0.7,
      severity: "mid",
      origin: "synthesized",
    });
  }

  if (total > 0) {
    items.push({
      time: timePrefix,
      title: `在管患者共 ${total} 人，${
        high > 0 ? `其中高危 ${high} 人` : "暂无高危病例"
      }`,
      confidence: 0.95,
      severity: high > 0 ? "high" : "low",
      origin: "synthesized",
    });
  } else {
    items.push({
      time: timePrefix,
      title: "当前在管队列为空，暂无患者数据",
      confidence: 0.99,
      severity: "low",
      origin: "synthesized",
    });
  }

  if (spectrum.length >= 2 && spectrum[1].pct > 20) {
    items.push({
      time: timePrefix,
      title: `${spectrum[1].name} 占比 ${spectrum[1].pct}%，为第二大疾病类别`,
      confidence: 0.6,
      severity: "low",
      origin: "synthesized",
    });
  }

  // 按置信度降序，取前 8 条
  items.sort((a, b) => b.confidence - a.confidence);
  return items.slice(0, 8);
}
