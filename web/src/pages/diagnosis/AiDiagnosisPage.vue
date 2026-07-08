<template>
  <div class="ai-diagnosis-container" v-loading="loadingQueue">
    <el-row :gutter="20" class="full-height">
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <el-text tag="b" size="large">AI 诊断队列</el-text>
              <el-tag size="small" type="info">{{ patients.length }} 人</el-tag>
            </div>
          </template>

          <el-tabs v-model="queueTab" class="queue-tabs">
            <el-tab-pane name="pending">
              <template #label>
                <span class="queue-tab-label"
                  >待诊 <span>{{ pendingPatients.length }}</span></span
                >
              </template>
            </el-tab-pane>
            <el-tab-pane name="reported">
              <template #label>
                <span class="queue-tab-label"
                  >已出报告 <span>{{ reportedPatients.length }}</span></span
                >
              </template>
            </el-tab-pane>
          </el-tabs>

          <el-scrollbar class="queue-scrollbar">
            <div
              v-for="patient in visiblePatients"
              :key="patient.id"
              class="patient-list-item"
              :class="{ 'is-active': selectedPatient?.id === patient.id }"
              @click="handleSelectPatient(patient)"
            >
              <PatientAvatar
                :size="46"
                :src="patient.avatar"
                :name="patient.name"
              />
              <div class="item-info">
                <div class="item-header">
                  <div class="patient-identity">
                    <span class="patient-no-label">病号</span>
                    <span class="patient-no-value">{{ patient.id }}</span>
                    <span class="patient-name">{{ patient.name }}</span>
                  </div>
                  <el-tag
                    v-if="patient.aiStatus === '已诊断'"
                    size="small"
                    type="success"
                    effect="dark"
                    round
                    >已出报告</el-tag
                  >
                </div>
                <div class="item-sub">
                  {{ patient.gender }} | {{ patient.age }} 岁
                </div>
              </div>
            </div>
            <el-empty
              v-if="visiblePatients.length === 0"
              :description="
                queueTab === 'pending' ? '暂无待诊患者' : '暂无已出报告患者'
              "
              :image-size="90"
            />
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18" class="full-height">
        <el-card
          class="right-panel"
          shadow="never"
          v-loading="isBusy"
          :element-loading-text="loadingText"
          element-loading-background="rgba(var(--imld-white-rgb), 0.9)"
        >
          <div v-if="!selectedPatient" class="empty-state">
            <el-empty description="请从左侧列表选择一位患者进行 AI 辅助诊断" />
          </div>

          <div
            v-else-if="
              panelState === 'ready' &&
              selectedPatient.aiStatus === diagnosedStatus
            "
            class="ready-state"
          >
            <PatientAvatar
              :size="80"
              :src="selectedPatient.avatar"
              :name="selectedPatient.name"
              style="margin-bottom: 20px"
            />
            <el-text
              size="large"
              tag="b"
              style="font-size: 22px; display: block; margin-bottom: 12px"
            >
              {{ selectedPatient.name }} 已完成 AI 报告
            </el-text>
            <el-text type="info" style="margin-bottom: 30px; display: block">
              已出报告患者无需再次启动 AI，系统将直接展示历史报告。
            </el-text>
            <el-button
              type="primary"
              plain
              size="large"
              :icon="Download"
              @click="loadSelectedPatientReport"
            >
              重新调取已出报告
            </el-button>
          </div>

          <div v-else-if="panelState === 'ready'" class="ready-state">
            <PatientAvatar
              :size="80"
              :src="selectedPatient.avatar"
              :name="selectedPatient.name"
              style="margin-bottom: 20px"
            />
            <el-text
              size="large"
              tag="b"
              style="font-size: 22px; display: block; margin-bottom: 12px"
            >
              {{ selectedPatient.name }} 的诊疗档案已就绪
            </el-text>
            <el-text type="info" style="margin-bottom: 30px; display: block">
              系统已提取该患者的基础档案、生化指标及临床表征数据。
            </el-text>
            <el-button
              type="primary"
              size="large"
              :icon="Cpu"
              class="pulsing-btn"
              :disabled="isBusy"
              @click="startDiagnosis"
            >
              启动 AI 智能筛查
            </el-button>
          </div>

          <div
            v-else-if="panelState === 'reasoning' && reasoningTrace"
            class="reasoning-state"
          >
            <ReasoningTimeline
              :trace="reasoningTrace"
              @complete="goToReport"
              @skip="goToReport"
            />
          </div>

          <div
            v-else-if="panelState === 'report' && diagnosisResult"
            class="report-state"
          >
            <DiagnosisReportCard :result="diagnosisResult" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { Cpu, Download } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import PatientAvatar from "@/components/molecules/PatientAvatar.vue";
import DiagnosisReportCard from "@/components/organisms/DiagnosisReportCard.vue";
import ReasoningTimeline from "@/components/organisms/ReasoningTimeline.vue";
import diagnosisApi from "@/api/diagnosis";
import type { DiagnosisQueuePatient, DiagnosisResult } from "@/api/types";
import { buildReasoningTrace } from "@/features/intelligence/reasoningTrace";
import type { ReasoningTrace } from "@/components/organisms/ReasoningTimeline.types";

const DIAGNOSED_STATUS = "已诊断";
const route = useRoute();

const isDiagnosing = ref(false);
const isLoadingReportedResult = ref(false);
const loadingQueue = ref(false);
const selectedPatient = ref<DiagnosisQueuePatient | null>(null);
const diagnosisResult = ref<DiagnosisResult | null>(null);
const patients = ref<DiagnosisQueuePatient[]>([]);
const queueTab = ref<"pending" | "reported">("pending");
const diagnosedStatus = DIAGNOSED_STATUS;
const panelState = ref<"ready" | "reasoning" | "report">("ready");
const reasoningTrace = ref<ReasoningTrace | null>(null);
// 标记当前推理剧场是否需要在结束时收尾（刷新队列、切到已出报告、提示）。
const finalizePending = ref(false);

const isBusy = computed(
  () => isDiagnosing.value || isLoadingReportedResult.value
);

const loadingText = computed(() => {
  if (isDiagnosing.value) {
    return "IMLD AI 正在解析临床表型与数据，请稍候...";
  }
  return "正在调取该患者的历史 AI 报告，请稍候...";
});

const pendingPatients = computed(() =>
  patients.value.filter((item) => item.aiStatus !== DIAGNOSED_STATUS)
);

const reportedPatients = computed(() =>
  patients.value.filter((item) => item.aiStatus === DIAGNOSED_STATUS)
);

const visiblePatients = computed(() => {
  return queueTab.value === "reported"
    ? reportedPatients.value
    : pendingPatients.value;
});

const syncSelectedPatientFromQueue = () => {
  if (!selectedPatient.value) {
    return;
  }
  selectedPatient.value =
    patients.value.find((item) => item.id === selectedPatient.value?.id) ||
    null;
};

const loadReportedDiagnosis = async (patient: DiagnosisQueuePatient) => {
  isLoadingReportedResult.value = true;
  try {
    const res = await diagnosisApi.getLatestDiagnosisResultByPatient(
      patient.id
    );
    if (!res.data) {
      await fetchQueue();
      queueTab.value = "pending";
      ElMessage.warning("该患者暂无已出报告，请点击“启动 AI 智能筛查”");
      return;
    }
    diagnosisResult.value = res.data;
  } catch {
    ElMessage.error("加载历史报告失败，请稍后重试");
  } finally {
    isLoadingReportedResult.value = false;
  }
};

const loadSelectedPatientReport = async () => {
  const patient = selectedPatient.value;
  if (!patient || isBusy.value) {
    return;
  }
  await loadReportedDiagnosis(patient);
};

const fetchQueue = async () => {
  loadingQueue.value = true;
  try {
    const res = await diagnosisApi.getAiQueue();
    patients.value = res.data.items || [];
    syncSelectedPatientFromQueue();
  } catch {
    ElMessage.error("加载待诊队列失败，请稍后重试");
  } finally {
    loadingQueue.value = false;
  }
};

const handleSelectPatient = async (patient: DiagnosisQueuePatient) => {
  if (isBusy.value) {
    ElMessage.warning("当前正在处理任务，请稍后再切换患者");
    return;
  }

  selectedPatient.value = patient;
  diagnosisResult.value = null;
  reasoningTrace.value = null;
  // 切换患者时清除上一场的收尾守卫，避免残留触发队列刷新与标签切换
  finalizePending.value = false;
  panelState.value = "ready";

  if (patient.aiStatus === DIAGNOSED_STATUS) {
    await loadReportedDiagnosis(patient);
    if (diagnosisResult.value) {
      panelState.value = "report";
    }
  }
};

// 推理剧场结束（用户点击「查看报告」或「查看完整报告」跳过）后统一收尾。
// 用 finalizePending 守卫，保证刷新队列 / 切换标签 / 提示只执行一次。
const goToReport = async () => {
  panelState.value = "report";
  if (!finalizePending.value) {
    return;
  }
  finalizePending.value = false;
  await fetchQueue();
  queueTab.value = "reported";
  ElMessage.success("AI 辅助诊断已完成，患者已移入已出报告");
};

const startDiagnosis = async () => {
  const patient = selectedPatient.value;
  if (!patient || isBusy.value) {
    return;
  }

  if (patient.aiStatus === DIAGNOSED_STATUS) {
    await loadReportedDiagnosis(patient);
    if (diagnosisResult.value) {
      panelState.value = "report";
    }
    return;
  }

  isDiagnosing.value = true;
  try {
    const res = await diagnosisApi.runAiDiagnosis(patient.id);
    diagnosisResult.value = res.data;
    reasoningTrace.value = buildReasoningTrace(res.data);
    finalizePending.value = true;
    // 进入推理剧场；收尾交由 ReasoningTimeline 的 complete / skip 事件驱动
    panelState.value = "reasoning";
  } catch {
    ElMessage.error("AI 诊断失败，请稍后重试");
  } finally {
    isDiagnosing.value = false;
  }
};

onMounted(() => {
  fetchQueue().then(() => {
    const patientId = route.query.patientId as string | undefined;
    if (patientId) {
      const patient = patients.value.find((p) => p.id === patientId);
      if (patient) handleSelectPatient(patient);
    }
  });
});
</script>

<style scoped>
.ai-diagnosis-container {
  padding: 24px;
  background-color: var(--imld-bg);
  height: 100%;
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

/* 左侧患者列表样式 */
.left-panel,
.right-panel {
  height: 100%;
  border-radius: 8px;
  border: none;
  box-shadow: var(--imld-shadow-card);
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-panel :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  min-height: 0;
}

.queue-tabs {
  flex-shrink: 0;
  padding: 0 16px;
}

.queue-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.queue-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.queue-tab-label span {
  display: inline-flex;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: var(--imld-bg);
  color: var(--imld-muted);
  font-size: 12px;
  line-height: 20px;
  justify-content: center;
}

.queue-scrollbar {
  flex: 1;
  min-height: 0;
}

.patient-list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--imld-border);
  cursor: pointer;
  transition: all 0.3s;
}

.patient-list-item:hover {
  background-color: var(--imld-bg);
}

.patient-list-item.is-active {
  background-color: rgba(var(--imld-primary-rgb), 0.08);
  border-left: 4px solid var(--imld-primary);
}

.item-info {
  margin-left: 12px;
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.patient-identity {
  display: flex;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.patient-no-label {
  font-size: 12px;
  color: var(--imld-muted);
  letter-spacing: 1px;
}

.patient-no-value {
  font-size: 17px;
  font-weight: 700;
  color: var(--imld-text);
}

.patient-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--imld-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-sub {
  font-size: 13px;
  color: var(--imld-muted);
}

/* 右侧工作台样式 */
:deep(.el-card__body) {
  flex: 1;
  overflow: auto;
  box-sizing: border-box;
  padding: 24px;
  min-height: 0;
  min-width: 0;
}

.empty-state,
.ready-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

/* 呼吸灯效果按钮 */
.pulsing-btn {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(var(--imld-primary-rgb), 0.4);
  }
  70% {
    box-shadow: 0 0 0 15px rgba(var(--imld-primary-rgb), 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(var(--imld-primary-rgb), 0);
  }
}

/* 推理中状态 */
.reasoning-state {
  height: 100%;
  overflow-y: auto;
  animation: fadeIn 0.3s ease-in-out;
}

/* 报告状态 */
.report-state {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
