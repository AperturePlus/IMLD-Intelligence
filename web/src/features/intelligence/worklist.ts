import type { IntelligenceDataSource } from "./dataSource";
import type { WorklistCase } from "./types";
import { riskLevelFromProbability } from "../diagnosis/services/riskLevel";
import { buildEvidenceItemsFromDiagnosis } from "../diagnosis/services/diagnosisEvidence";
import {
  generateSynthesizedScore,
  generateSynthesizedReason,
} from "./synthesis";

export async function computeWorklist(
  dataSource: IntelligenceDataSource,
  topN = 5
): Promise<WorklistCase[]> {
  const queue = await dataSource.getQueue();
  const pendingPatients = queue.filter((p) => p.aiStatus !== "已诊断");

  const cases: WorklistCase[] = await Promise.all(
    pendingPatients.map(async (patient) => {
      const record = await dataSource.getPatientRecord(patient.id);

      if (record) {
        // derived 路径：基于真实病历计算
        const evidenceItems = buildEvidenceItemsFromDiagnosis({
          diseaseName: "待评估",
          indicators: [],
          record,
        });

        const exceptionCount = evidenceItems.filter(
          (e) => e.severity === "exception"
        ).length;
        const warningCount = evidenceItems.filter(
          (e) => e.severity === "warning"
        ).length;
        const variantCount = record.geneticSequencing?.variants?.length ?? 0;

        let score = Math.min(
          100,
          exceptionCount * 3 + warningCount * 1 + variantCount * 2
        );
        // 如果所有计数为0，给一个基础分避免全部为0
        if (score === 0 && evidenceItems.length > 0) {
          score = 15;
        }

        const riskLevel = riskLevelFromProbability(score);

        // 取前2个异常证据作为 reason
        const abnormalItems = evidenceItems
          .filter((e) => e.severity === "exception" || e.severity === "warning")
          .slice(0, 2);
        const reason =
          abnormalItems.map((e) => e.label).join("、") || "基础档案已就绪";

        return {
          id: patient.id,
          name: patient.name,
          riskScore: score,
          riskLevel,
          reason,
          origin: "derived",
        };
      } else {
        // synthesized 路径：基于稳定种子生成
        const score = generateSynthesizedScore(patient.id, queue.length);
        const riskLevel = riskLevelFromProbability(score);

        return {
          id: patient.id,
          name: patient.name,
          riskScore: score,
          riskLevel,
          reason: generateSynthesizedReason(patient.id),
          origin: "synthesized",
        };
      }
    })
  );

  // 按风险分降序，然后取 Top-N
  cases.sort((a, b) => b.riskScore - a.riskScore);
  return cases.slice(0, topN);
}
