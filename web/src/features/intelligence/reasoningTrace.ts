import type { DiagnosisResult, DiagnosisEvidenceItem } from "@/types/diagnosis";
import type { PatientRecordPayload } from "@/types/patient";
import type {
  ReasoningTrace,
  ReasoningStep,
} from "@/components/organisms/ReasoningTimeline.types";

const CATEGORY_LABELS: Record<string, string> = {
  生化: "生化指标",
  影像: "影像特征",
  基因: "基因位点",
  病史: "病史信息",
  体征: "体征数据",
  临床表型: "临床表型",
  病理: "病理结果",
};

// 按异常严重度赋予基础权重（0-100），用于「证据加权」步骤
const SEVERITY_BASE_WEIGHT: Record<string, number> = {
  exception: 100,
  warning: 65,
  info: 35,
};

function countByCategory(
  items: DiagnosisEvidenceItem[]
): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const item of items) {
    counts[item.category] = (counts[item.category] ?? 0) + 1;
  }
  return counts;
}

// 步骤一「特征提取」：按来源类别统计纳入特征数，条宽表示相对覆盖度
function buildFeatureBars(counts: Record<string, number>) {
  const entries = Object.entries(counts).filter(([, n]) => n > 0);
  const maxCount = Math.max(...entries.map(([, n]) => n), 1);
  return entries
    .sort((a, b) => b[1] - a[1])
    .map(([category, n]) => ({
      label: `${CATEGORY_LABELS[category] ?? category} · ${n} 项`,
      weight: Math.round((n / maxCount) * 100),
      // 计数类条目右侧不重复展示百分比，数量已在标签中给出
      metricLabel: null,
    }));
}

// 步骤二「证据加权」：对单条异常证据按严重度加权（0-100），递减排列凸显主导证据
function buildWeightedEvidenceBars(items: DiagnosisEvidenceItem[]) {
  return items
    .filter((item) => item.severity in SEVERITY_BASE_WEIGHT)
    .map((item) => ({
      label: item.label,
      base: SEVERITY_BASE_WEIGHT[item.severity],
      weak: item.severity === "info",
    }))
    .sort((a, b) => b.base - a.base)
    .slice(0, 6)
    .map((item, i) => ({
      label: item.label,
      // 同级证据按排名轻微衰减，形成可读的权重梯度
      weight: Math.max(12, item.base - i * 6),
      weak: item.weak,
    }));
}

function buildDifferentialCandidates(result: DiagnosisResult) {
  const candidates: {
    name: string;
    prob: number;
    win?: boolean;
    why?: string;
  }[] = [];

  // 主导诊断
  candidates.push({
    name: result.diseaseName,
    prob: result.probability,
    win: true,
    why: "综合证据权重最高",
  });

  // 鉴别诊断（下调概率）
  const others =
    result.differentials.length > 0 ? result.differentials : ["其他鉴别诊断"];

  for (let i = 0; i < others.length; i++) {
    const dampFactor = 0.3 + i * 0.1;
    const prob = Math.max(5, Math.round(result.probability * dampFactor));
    candidates.push({
      name: others[i],
      prob,
      why: `证据支持度低于主导诊断（${prob}%）`,
    });
  }

  return candidates.slice(0, 4);
}

export function buildReasoningTrace(
  result: DiagnosisResult,
  _record?: PatientRecordPayload | null
): ReasoningTrace {
  const evidenceItems = result.evidenceItems || [];
  const categoryCounts = countByCategory(evidenceItems);

  const step1: ReasoningStep = {
    title: "特征提取",
    evidenceBars: buildFeatureBars(categoryCounts),
  };

  const step2: ReasoningStep = {
    title: "证据加权",
    evidenceBars: buildWeightedEvidenceBars(evidenceItems),
  };

  const step3: ReasoningStep = {
    title: "鉴别赛跑",
    candidates: buildDifferentialCandidates(result),
  };

  const step4: ReasoningStep = {
    title: "置信收敛",
    confidence: {
      percentage: result.probability,
      level:
        result.riskLevel === "高"
          ? "high"
          : result.riskLevel === "中"
          ? "mid"
          : "low",
      label: result.confidence?.label || "置信度评估",
    },
  };

  return {
    steps: [step1, step2, step3, step4],
  };
}
