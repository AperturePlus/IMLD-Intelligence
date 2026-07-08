// ============================================
// Atomic Design Component Barrel
// ============================================
// Atoms — indivisible UI primitives
// Molecules — composite components (2+ atoms)
// Organisms — complex sections (molecules + atoms)
// ============================================

// ----- Atoms -----
export { default as BaseAvatar } from "./atoms/BaseAvatar.vue";
export { default as BaseBadge } from "./atoms/BaseBadge.vue";
export { default as BaseButton } from "./atoms/BaseButton.vue";
export { default as BaseIcon } from "./atoms/BaseIcon.vue";
export { default as BaseTag } from "./atoms/BaseTag.vue";
export { default as ProgressRing } from "./atoms/ProgressRing.vue";
export { default as Sparkline } from "./atoms/Sparkline.vue";
export { default as StatusDot } from "./atoms/StatusDot.vue";
export { default as StreamingText } from "./atoms/StreamingText.vue";

// ----- Molecules -----
export { default as AiTag } from "./molecules/AiTag.vue";
export { default as ChatComposer } from "./molecules/ChatComposer.vue";
export { default as ChatMessage } from "./molecules/ChatMessage.vue";
export { default as ConfidenceGauge } from "./molecules/ConfidenceGauge.vue";
export { default as EngineStatusPill } from "./molecules/EngineStatusPill.vue";
export { default as EvidenceBar } from "./molecules/EvidenceBar.vue";
export { default as InsightItem } from "./molecules/InsightItem.vue";
export { default as KpiStat } from "./molecules/KpiStat.vue";
export { default as NavItem } from "./molecules/NavItem.vue";
export { default as PatientAvatar } from "./molecules/PatientAvatar.vue";
export { default as PrivacyBadge } from "./molecules/PrivacyBadge.vue";
export { default as RiskPill } from "./molecules/RiskPill.vue";

// ----- Organisms -----
export { default as AiCopilotDock } from "./organisms/AiCopilotDock.vue";
export { default as AppSidebar } from "./organisms/AppSidebar.vue";
export { default as AppTopbar } from "./organisms/AppTopbar.vue";
export { default as DesktopTitlebar } from "./organisms/DesktopTitlebar.vue";
export { default as DiagnosisReportCard } from "./organisms/DiagnosisReportCard.vue";
export { default as DifferentialRace } from "./organisms/DifferentialRace.vue";
export { default as DiseaseSpectrum } from "./organisms/DiseaseSpectrum.vue";
export { default as ForecastChart } from "./organisms/ForecastChart.vue";
export { default as InsightFeed } from "./organisms/InsightFeed.vue";
export { default as KpiGrid } from "./organisms/KpiGrid.vue";
export { default as ReasoningTimeline } from "./organisms/ReasoningTimeline.vue";
export { default as RiskDonut } from "./organisms/RiskDonut.vue";
export { default as WorklistBanner } from "./organisms/WorklistBanner.vue";

// ----- Re-export shared types (consumer-facing) -----
export type { ChatComposerProps } from "./molecules/ChatComposer.types";
export type {
  ChatMessageProps,
  ChatRole,
} from "./molecules/ChatMessage.types";
export type { ConfidenceGaugeProps } from "./molecules/ConfidenceGauge.types";
export type { EngineStatusProps } from "./molecules/EngineStatusPill.types";
export type { EvidenceBarProps } from "./molecules/EvidenceBar.types";
export type { InsightItemProps } from "./molecules/InsightItem.types";
export type { KpiStatProps } from "./molecules/KpiStat.types";
export type { PrivacyBadgeProps } from "./molecules/PrivacyBadge.types";
export type { RiskPillProps } from "./molecules/RiskPill.types";

export type { AiCopilotDockProps } from "./organisms/AiCopilotDock.types";
export type { DiagnosisReportProps } from "./organisms/DiagnosisReportCard.types";
export type { DifferentialRaceProps } from "./organisms/DifferentialRace.types";
export type { DiseaseSpectrumProps } from "./organisms/DiseaseSpectrum.types";
export type { ForecastChartProps } from "./organisms/ForecastChart.types";
export type { ReasoningTimelineProps } from "./organisms/ReasoningTimeline.types";
export type { RiskDonutProps } from "./organisms/RiskDonut.types";
export type { WorklistBannerProps } from "./organisms/WorklistBanner.types";
export type { AppTopbarProps } from "./organisms/AppTopbar.types";
