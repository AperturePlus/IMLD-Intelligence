<template>
  <div class="welcome-page">
    <WorklistBanner
      :greeting="greeting"
      :high-risk-count="cohortMetrics?.highRiskCount.data ?? 0"
      :cases="worklistCases"
      @open-case="handleOpenCase"
    />

    <KpiGrid :items="kpiItems" />

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="panel-card">
          <div class="panel-title">风险分布</div>
          <RiskDonut
            :high="cohortMetrics?.highRiskCount.data ?? 0"
            :mid="cohortMetrics?.midRiskCount.data ?? 0"
            :low="cohortMetrics?.lowRiskCount.data ?? 0"
            :total="cohortMetrics?.totalPatients.data ?? 0"
          />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="panel-card">
          <div class="panel-title">疾病谱</div>
          <DiseaseSpectrum :items="diseaseSpectrum" />
        </div>
      </el-col>
    </el-row>

    <div class="panel-card">
      <div class="panel-title">洞察流</div>
      <InsightFeed :items="insights" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import WorklistBanner from "@/components/organisms/WorklistBanner.vue";
import KpiGrid from "@/components/organisms/KpiGrid.vue";
import RiskDonut from "@/components/organisms/RiskDonut.vue";
import DiseaseSpectrum from "@/components/organisms/DiseaseSpectrum.vue";
import InsightFeed from "@/components/organisms/InsightFeed.vue";
import { useIntelligenceEngine } from "@/features/intelligence/composables/useIntelligenceEngine";
import type { KpiStatProps } from "@/components/molecules/KpiStat.types";

const router = useRouter();
const { cohortMetrics, worklist, insights, forecast } = useIntelligenceEngine();

const greeting = computed(() => {
  const total = cohortMetrics.value?.totalPatients.data ?? 0;
  return `欢迎回来 · 今日队列 ${total} 人`;
});

// 近 8 周新检出量（引擎合成的趋势序列），作为「自动报告」吞吐量火花线
const weeklyVolume = computed(() =>
  (forecast.value?.history.data ?? []).map((p) => p.y).slice(-8)
);

// 由序列末两点推导环比趋势与变化幅度
const seriesTrend = (
  series: number[]
): { trend: KpiStatProps["trend"]; delta: number | null } => {
  if (series.length < 2) return { trend: "flat", delta: null };
  const last = series[series.length - 1];
  const prev = series[series.length - 2];
  if (!Number.isFinite(prev) || prev === 0) {
    return { trend: "flat", delta: null };
  }
  const pct = Math.round(((last - prev) / prev) * 100);
  return { trend: pct > 0 ? "up" : pct < 0 ? "down" : "flat", delta: pct };
};

const kpiItems = computed<KpiStatProps[]>(() => {
  const m = cohortMetrics.value;
  if (!m) return [];
  const volume = weeklyVolume.value;
  const { trend, delta } = seriesTrend(volume);
  return [
    {
      label: "在管患者",
      value: m.totalPatients.data,
      format: "number",
    },
    {
      label: "AI 高危",
      value: m.highRiskCount.data,
      format: "number",
      tone: "danger",
    },
    {
      label: "阳性率",
      value: Math.round(m.positiveRate.data * 100),
      format: "percent",
    },
    {
      label: "自动报告",
      value: m.autoReportCount.data,
      format: "number",
      sparklinePoints: volume,
      trend,
      delta,
    },
  ];
});

const worklistCases = computed(() =>
  worklist.value.map((c) => ({
    id: c.id,
    name: c.name,
    riskLevel: c.riskLevel,
    score: c.riskScore,
    reason: c.reason,
  }))
);

const diseaseSpectrum = computed(
  () => cohortMetrics.value?.diseaseSpectrum.data ?? []
);

const handleOpenCase = (patientId: string) => {
  router.push({ path: "/center/ai-diagnosis", query: { patientId } });
};
</script>

<style scoped>
.welcome-page {
  min-height: 100%;
  padding: var(--imld-sp-6);
  display: flex;
  flex-direction: column;
  gap: var(--imld-sp-5);
  background: radial-gradient(
      circle at 8% 8%,
      rgba(34, 163, 159, 0.18),
      transparent 38%
    ),
    radial-gradient(circle at 90% 0%, rgba(15, 109, 141, 0.18), transparent 40%),
    var(--imld-bg);
}

.panel-card {
  padding: var(--imld-sp-4) var(--imld-sp-5);
  border-radius: var(--imld-radius-md);
  background: var(--imld-card);
  box-shadow: var(--imld-shadow-card);
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--imld-text);
  margin-bottom: var(--imld-sp-3);
}
</style>
