<template>
  <div class="ai-diagnosis-container">
    <el-row :gutter="20" class="full-height">
      
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <template #header>
            <div class="panel-header">
              <el-text tag="b" size="large">待诊患者队列</el-text>
              <el-tag size="small" type="info">{{ patients.length }} 人</el-tag>
            </div>
          </template>
          
          <el-scrollbar height="calc(100vh - 160px)">
            <div 
              v-for="patient in patients" 
              :key="patient.id"
              class="patient-list-item"
              :class="{ 'is-active': selectedPatient?.id === patient.id }"
              @click="handleSelectPatient(patient)"
            >
              <el-avatar :size="46" :src="patient.avatar" />
              <div class="item-info">
                <div class="item-header">
                  <span class="name">{{ patient.name }}</span>
                  <el-tag 
                    v-if="patient.aiStatus === '已诊断'" 
                    size="small" type="success" effect="dark" round
                  >已出报告</el-tag>
                </div>
                <div class="item-sub">
                  {{ patient.gender }} | {{ patient.age }} 岁 | ID: {{ patient.id }}
                </div>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18" class="full-height">
        <el-card 
          class="right-panel" 
          shadow="never"
          v-loading="isDiagnosing"
          element-loading-text="数智肝循 AI 正在解析临床表型与多模态数据，请稍候..."
          element-loading-background="rgba(255, 255, 255, 0.9)"
        >
          
          <div v-if="!selectedPatient" class="empty-state">
            <el-empty description="请从左侧列表选择一位患者进行 AI 辅助诊断" />
          </div>

          <div v-else-if="selectedPatient && !diagnosisResult" class="ready-state">
            <el-avatar :size="80" :src="selectedPatient.avatar" style="margin-bottom: 20px;" />
            <el-text size="large" tag="b" style="font-size: 22px; display: block; margin-bottom: 12px;">
              {{ selectedPatient.name }} 的诊疗档案已就绪
            </el-text>
            <el-text type="info" style="margin-bottom: 30px; display: block;">
              系统已提取该患者的基础档案、生化指标及临床表征数据。
            </el-text>
            <el-button type="primary" size="large" :icon="Cpu" class="pulsing-btn" @click="startDiagnosis">
              启动 AI 智能筛查大模型
            </el-button>
          </div>

          <div v-else-if="diagnosisResult" class="result-state">
            <div class="result-header">
              <div class="title-area">
                <el-icon color="#409EFF" :size="28" style="margin-right: 12px;"><Aim /></el-icon>
                <span class="report-title">AI 辅助诊断评估报告</span>
              </div>
              <el-button type="primary" plain :icon="Download">导出报告 PDF</el-button>
            </div>

            <el-row :gutter="24" class="core-conclusion">
              <el-col :span="8" style="text-align: center; border-right: 1px solid #ebeef5;">
                <el-text type="info">AI 预测患病概率</el-text>
                <div style="margin-top: 16px;">
                  <el-progress 
                    type="dashboard" 
                    :percentage="diagnosisResult.probability" 
                    :color="customColors" 
                    :width="140"
                    :stroke-width="12"
                  >
                    <template #default="{ percentage }">
                      <span class="percentage-value">{{ percentage }}%</span>
                      <br>
                      <span class="percentage-label">极高危</span>
                    </template>
                  </el-progress>
                </div>
              </el-col>
              <el-col :span="16" style="padding-left: 30px;">
                <el-text type="info">疑似疾病指向</el-text>
                <h2 class="disease-name">{{ diagnosisResult.diseaseName }}</h2>
                <el-descriptions :column="2" border size="small" style="margin-top: 16px;">
                  <el-descriptions-item label="鉴别诊断">遗传性血色病 (排除)、自身免疫性肝炎 (低可能)</el-descriptions-item>
                  <el-descriptions-item label="数据置信度">
                    <el-tag type="success" size="small">高 (0.94)</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="关键体征">角膜 K-F 环阳性、非对称性震颤</el-descriptions-item>
                </el-descriptions>
              </el-col>
            </el-row>

            <div class="section-card">
              <div class="section-title">特征性生化指标偏离分析</div>
              <div class="chart-container">
                <div class="chart-row" v-for="item in diagnosisResult.indicators" :key="item.name">
                  <div class="chart-label">{{ item.name }}</div>
                  <div class="chart-bar-area">
                    <el-progress 
                      :percentage="item.percentage" 
                      :status="item.status" 
                      :stroke-width="14" 
                      :show-text="false"
                    />
                  </div>
                  <div class="chart-value" :class="item.status">
                    {{ item.value }} {{ item.unit }}
                    <span class="ref-range">(参考: {{ item.normal }})</span>
                  </div>
                </div>
              </div>
            </div>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <div class="section-card suggestion-card">
                  <div class="section-title">
                    <el-icon><Microphone /></el-icon> 靶向基因变异预测
                  </div>
                  <div class="gene-tags">
                    <el-tag v-for="gene in diagnosisResult.genes" :key="gene" type="danger" effect="light" class="gene-tag">
                      {{ gene }}
                    </el-tag>
                  </div>
                  <el-alert
                    title="基因测序建议 (WES)"
                    type="warning"
                    :description="diagnosisResult.sequencing"
                    show-icon
                    :closable="false"
                    style="margin-top: 16px;"
                  />
                </div>
              </el-col>
              
              <el-col :span="12">
                <div class="section-card suggestion-card diet-card">
                  <div class="section-title">
                    <el-icon><Food /></el-icon> 专病膳食与干预建议
                  </div>
                  <p class="diet-text">{{ diagnosisResult.diet }}</p>
                  <div style="margin-top: 12px;">
                    <el-tag type="info" effect="plain" style="margin-right: 8px;">禁食坚果/巧克力</el-tag>
                    <el-tag type="info" effect="plain" style="margin-right: 8px;">避免内脏</el-tag>
                    <el-tag type="info" effect="plain">勿用铜制炊具</el-tag>
                  </div>
                </div>
              </el-col>
            </el-row>

          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Cpu, Aim, Download, Microphone, Food } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// --- 数据状态 ---
const isDiagnosing = ref(false)
const selectedPatient = ref(null)
const diagnosisResult = ref(null)

// 扩充到 20 条的模拟待诊患者列表
const patients = ref([
  { id: 'P001', name: '林建国', gender: '男', age: 58, avatar: 'https://randomuser.me/api/portraits/men/32.jpg', aiStatus: '未筛查' },
  { id: 'P002', name: '陈婉婷', gender: '女', age: 32, avatar: 'https://randomuser.me/api/portraits/women/44.jpg', aiStatus: '未筛查' },
  { id: 'P003', name: '张明远', gender: '男', age: 45, avatar: 'https://randomuser.me/api/portraits/men/67.jpg', aiStatus: '未筛查' },
  { id: 'P004', name: '王淑芬', gender: '女', age: 62, avatar: 'https://randomuser.me/api/portraits/women/68.jpg', aiStatus: '未筛查' },
  { id: 'P005', name: '李浩宇', gender: '男', age: 28, avatar: 'https://randomuser.me/api/portraits/men/22.jpg', aiStatus: '未筛查' },
  { id: 'P006', name: '赵雪梅', gender: '女', age: 51, avatar: 'https://randomuser.me/api/portraits/women/33.jpg', aiStatus: '未筛查' },
  { id: 'P007', name: '刘振华', gender: '男', age: 66, avatar: 'https://randomuser.me/api/portraits/men/45.jpg', aiStatus: '未筛查' },
  { id: 'P008', name: '周小雅', gender: '女', age: 24, avatar: 'https://randomuser.me/api/portraits/women/12.jpg', aiStatus: '未筛查' },
  { id: 'P009', name: '吴建强', gender: '男', age: 53, avatar: 'https://randomuser.me/api/portraits/men/51.jpg', aiStatus: '未筛查' },
  { id: 'P010', name: '郑丽丽', gender: '女', age: 38, avatar: 'https://randomuser.me/api/portraits/women/25.jpg', aiStatus: '未筛查' },
  { id: 'P011', name: '孙立军', gender: '男', age: 41, avatar: 'https://randomuser.me/api/portraits/men/18.jpg', aiStatus: '未筛查' },
  { id: 'P012', name: '马桂英', gender: '女', age: 71, avatar: 'https://randomuser.me/api/portraits/women/71.jpg', aiStatus: '未筛查' },
  { id: 'P013', name: '黄子韬', gender: '男', age: 31, avatar: 'https://randomuser.me/api/portraits/men/82.jpg', aiStatus: '未筛查' },
  { id: 'P014', name: '郭晓丹', gender: '女', age: 29, avatar: 'https://randomuser.me/api/portraits/women/55.jpg', aiStatus: '未筛查' },
  { id: 'P015', name: '冯伟', gender: '男', age: 48, avatar: 'https://randomuser.me/api/portraits/men/61.jpg', aiStatus: '未筛查' },
  { id: 'P016', name: '曹玉兰', gender: '女', age: 64, avatar: 'https://randomuser.me/api/portraits/women/62.jpg', aiStatus: '未筛查' },
  { id: 'P017', name: '彭志远', gender: '男', age: 36, avatar: 'https://randomuser.me/api/portraits/men/39.jpg', aiStatus: '未筛查' },
  { id: 'P018', name: '杨秀云', gender: '女', age: 55, avatar: 'https://randomuser.me/api/portraits/women/48.jpg', aiStatus: '未筛查' },
  { id: 'P019', name: '何文斌', gender: '男', age: 60, avatar: 'https://randomuser.me/api/portraits/men/74.jpg', aiStatus: '未筛查' },
  { id: 'P020', name: '许静', gender: '女', age: 27, avatar: 'https://randomuser.me/api/portraits/women/9.jpg', aiStatus: '未筛查' }
])

// AI 诊断进度条颜色阶梯
const customColors = [
  { color: '#67c23a', percentage: 30 },
  { color: '#e6a23c', percentage: 70 },
  { color: '#f56c6c', percentage: 100 },
]

// --- 方法 ---

// 1. 选择患者
const handleSelectPatient = (patient) => {
  if (isDiagnosing.value) {
    ElMessage.warning('AI 正在诊断中，请稍后再切换患者')
    return
  }
  selectedPatient.value = patient
  // 如果该患者还没有出报告，就重置右侧结果
  if (patient.aiStatus !== '已诊断') {
    diagnosisResult.value = null
  }
}

// 2. 启动 AI 诊断 (模拟 3 秒网络延迟)
const startDiagnosis = () => {
  isDiagnosing.value = true
  
  setTimeout(() => {
    // 模拟后端返回的 AI 诊断数据 (以典型的 Wilson 病为例)
    diagnosisResult.value = {
      diseaseName: '肝豆状核变性 (Wilson病)',
      probability: 92,
      // 模拟图表数据，percentage 控制进度条长度，status 控制颜色
      indicators: [
        { name: '血清铜蓝蛋白', value: 0.08, unit: 'g/L', normal: '0.20-0.60', percentage: 15, status: 'exception' }, 
        { name: '24h 尿铜排泄量', value: 215, unit: 'μg/24h', normal: '< 100', percentage: 85, status: 'exception' },
        { name: 'ALT (谷丙转氨酶)', value: 125, unit: 'U/L', normal: '9-50', percentage: 60, status: 'warning' },
        { name: 'AST (谷草转氨酶)', value: 98, unit: 'U/L', normal: '15-40', percentage: 55, status: 'warning' }
      ],
      genes: ['ATP7B (c.2333G>T, p.Arg778Leu)', 'ATP7B (c.2975C>T, p.Pro992Leu)'],
      diet: '建议立即启动严格低铜饮食（摄入量 < 1mg/d）。必须绝对禁食坚果类、巧克力、动物内脏及贝壳类软体海鲜。建议日常饮用纯净水。',
      sequencing: '高度怀疑 ATP7B 基因复合杂合突变。建议立即对先证者进行 全外显子组测序 (WES) 或针对 ATP7B 的 Sanger 测序验证，并建议对其一级亲属开展家系筛查。'
    }
    
    // 标记左侧列表状态
    if (selectedPatient.value) {
      selectedPatient.value.aiStatus = '已诊断'
    }
    
    isDiagnosing.value = false
    ElMessage.success('AI 辅助诊断已完成！')
  }, 3000) // 3000毫秒 = 3秒动画
}
</script>

<style scoped>
.ai-diagnosis-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

/* 左侧患者列表样式 */
.left-panel, .right-panel {
  height: 100%;
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.patient-list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.patient-list-item:hover {
  background-color: #f0f7ff;
}

.patient-list-item.is-active {
  background-color: #ecf5ff;
  border-left: 4px solid #409EFF;
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

.item-header .name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.item-sub {
  font-size: 13px;
  color: #909399;
}

/* 右侧工作台样式 */
:deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  padding: 24px;
}

.empty-state, .ready-state {
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
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

/* 诊断结果页样式 */
.result-state {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.report-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.title-area {
  display: flex;
  align-items: center;
}

.core-conclusion {
  background: #fafafa;
  padding: 24px 0;
  border-radius: 8px;
  margin-bottom: 24px;
}

.percentage-value {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
}

.percentage-label {
  font-size: 14px;
  color: #909399;
}

.disease-name {
  margin: 0 0 16px 0;
  color: #f56c6c;
  font-size: 24px;
}

/* 内部卡片样式 */
.section-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
}

.suggestion-card {
  height: 100%;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 纯 CSS 图表样式 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-row {
  display: flex;
  align-items: center;
}

.chart-label {
  width: 150px;
  font-size: 14px;
  color: #606266;
  text-align: right;
  padding-right: 16px;
}

.chart-bar-area {
  flex: 1;
  padding-right: 16px;
}

.chart-value {
  width: 180px;
  font-size: 14px;
  font-weight: bold;
}

.chart-value.exception { color: #f56c6c; }
.chart-value.warning { color: #e6a23c; }

.ref-range {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 6px;
}

/* 基因与膳食样式 */
.gene-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.gene-tag {
  font-size: 14px;
  padding: 6px 12px;
  height: auto;
}

.diet-card {
  background: linear-gradient(135deg, #fdfbfb 0%, #ebedee 100%);
  border: none;
}

.diet-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}
</style>