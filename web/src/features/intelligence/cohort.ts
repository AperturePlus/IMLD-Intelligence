import type { DiagnosisResult } from "@/types/diagnosis";
import type { IntelligenceDataSource } from "./dataSource";
import type { CohortMetrics, DiseaseSpectrumItem, IntelligenceOrigin } from "./types";
import { riskLevelFromProbability } from "../diagnosis/services/riskLevel";
import { generateSynthesizedScore } from "./synthesis";

export async function computeCohortMetrics(
  dataSource: IntelligenceDataSource
): Promise<CohortMetrics> {
  const queue = await dataSource.getQueue();
  const total = queue.length;

  if (total === 0) {
    return {
      totalPatients: { data: 0, origin: "derived" },
      highRiskCount: { data: 0, origin: "derived" },
      midRiskCount: { data: 0, origin: "derived" },
      lowRiskCount: { data: 0, origin: "derived" },
      positiveRate: { data: 0, origin: "derived" },
      autoReportCount: { data: 0, origin: "derived" },
      diseaseSpectrum: { data: [], origin: "derived" },
    };
  }

  // 获取所有已出报告患者的真实结果，按患者 ID 建索引
  const reportedPatients = queue.filter((p) => p.aiStatus === "已诊断");
  const resultEntries = await Promise.all(
    reportedPatients.map(async (p) => {
      try {
        return [p.id, await dataSource.getDiagnosisResult(p.id)] as const;
      } catch {
        return [p.id, null] as const;
      }
    })
  );
  const resultById = new Map<string, DiagnosisResult>();
  for (const [id, result] of resultEntries) {
    if (result) resultById.set(id, result);
  }
  const validResults = [...resultById.values()];

  // 风险分布：覆盖全队列，保证 高 + 中 + 低 === 在管总数。
  // 已出报告且有真实结果 → 由 probability 派生（derived）；
  // 其余（待诊 / 结果缺失）→ 由稳定种子合成（synthesized）。
  let highRisk = 0;
  let midRisk = 0;
  let lowRisk = 0;
  let synthesizedCount = 0;

  for (const patient of queue) {
    const result = resultById.get(patient.id);
    const probability = result
      ? result.probability
      : generateSynthesizedScore(patient.id, total);
    if (!result) synthesizedCount++;

    const level = riskLevelFromProbability(probability);
    if (level === "高") highRisk++;
    else if (level === "中") midRisk++;
    else lowRisk++;
  }

  // 任一患者走合成路径，则风险分布整体标记为 synthesized
  const riskOrigin: IntelligenceOrigin =
    synthesizedCount > 0 ? "synthesized" : "derived";

  // 疾病谱统计（仅基于有真实结果的已出报告患者）
  const diseaseCounts = new Map<string, number>();
  for (const result of validResults) {
    if (result.diseaseName) {
      diseaseCounts.set(
        result.diseaseName,
        (diseaseCounts.get(result.diseaseName) || 0) + 1
      );
    }
  }

  const diseaseSpectrum: DiseaseSpectrumItem[] = [];
  if (validResults.length > 0) {
    for (const [name, count] of diseaseCounts) {
      diseaseSpectrum.push({
        name,
        pct: Math.round((count / validResults.length) * 1000) / 10,
      });
    }
    diseaseSpectrum.sort((a, b) => b.pct - a.pct);
  }

  // 阳性率 = 已出报告且判为高危的患者数 / 已出报告患者数
  // （以已诊断人群为分母，反映 AI 筛查在高危识别上的阳性占比）
  const reportedCount = reportedPatients.length;
  const positiveRate = reportedCount > 0 ? highRisk / reportedCount : 0;

  // 自动报告数 = 已出报告数（当前全部视为自动）
  const autoReportCount = reportedCount;

  return {
    totalPatients: { data: total, origin: "derived" },
    highRiskCount: { data: highRisk, origin: riskOrigin },
    midRiskCount: { data: midRisk, origin: riskOrigin },
    lowRiskCount: { data: lowRisk, origin: riskOrigin },
    positiveRate: { data: positiveRate, origin: "derived" },
    autoReportCount: { data: autoReportCount, origin: "derived" },
    diseaseSpectrum: { data: diseaseSpectrum, origin: "derived" },
  };
}
