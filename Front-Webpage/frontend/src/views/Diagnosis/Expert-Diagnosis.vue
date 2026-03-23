<template>
  <div class="report-manage-container" v-loading="loadingReports">
    <el-row :gutter="20" class="full-height">
      
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <div class="panel-header">
            <el-input
              v-model="searchQuery"
              placeholder="搜索患者姓名或门诊号"
              :prefix-icon="Search"
              clearable
              size="large"
              style="margin-bottom: 16px;"
            />
            <el-radio-group v-model="reportStatusFilter" size="large" style="width: 100%;">
              <el-radio-button label="待签发" style="flex: 1; text-align: center;" />
              <el-radio-button label="已签发" style="flex: 1; text-align: center;" />
            </el-radio-group>
          </div>
          
          <el-scrollbar height="calc(100vh - 220px)" style="margin-top: 12px;">
            <div 
              v-for="report in filteredReports" 
              :key="report.id"
              class="report-list-item"
              :class="{ 'is-active': selectedReport?.id === report.id }"
              @click="handleSelectReport(report)"
            >
              <div class="item-main">
                <div class="item-title">
                  <span class="name">{{ report.patientName }}</span>
                  <el-tag 
                    :type="report.status === '已签发' ? 'success' : 'warning'" 
                    size="default" 
                    effect="light"
                  >
                    {{ report.status }}
                  </el-tag>
                </div>
                <div class="item-sub">
                  <span>{{ report.gender }} | {{ report.age }}岁</span>
                  <span style="margin-left: 8px;">门诊号: {{ report.visitId }}</span>
                </div>
                <div class="item-time">筛查时间: {{ report.date }}</div>
              </div>
            </div>
            <el-empty v-if="filteredReports.length === 0" description="暂无相关报告任务" :image-size="100" />
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18" class="full-height">
        <el-card class="right-panel" shadow="never" :body-style="{ padding: '0', height: '100%', display: 'flex', flexDirection: 'column' }">
          
          <div v-if="!selectedReport" class="empty-state">
            <el-empty description="请从左侧选择一份报告进行审核与签发" />
          </div>

          <template v-else>
            <div class="toolbar">
              <div class="toolbar-left">
                <el-text tag="b" size="large" style="font-size: 18px;">专家审核工作台</el-text>
                
                <el-divider direction="vertical" style="margin: 0 20px;" />
                
                <el-text type="info" style="margin-right: 12px;">视图缩放:</el-text>
                <el-button-group>
                  <el-button :icon="ZoomOut" @click="handleZoomOut" title="缩小报告" />
                  <el-button style="width: 70px; font-weight: bold;" @click="handleResetZoom" title="重置比例">
                    {{ Math.round(paperScale * 100) }}%
                  </el-button>
                  <el-button :icon="ZoomIn" @click="handleZoomIn" title="放大报告" />
                </el-button-group>
              </div>

              <div class="toolbar-right">
                <el-button :icon="Printer" size="large" @click="handlePrint">打印报告</el-button>
                <el-button :icon="Download" size="large" @click="handleExport">导出 PDF</el-button>
                <el-button 
                  v-if="selectedReport.status === '待签发'"
                  type="primary" 
                  size="large"
                  :icon="EditPen" 
                  @click="handleSign"
                >
                  专家签名签发
                </el-button>
                <el-button 
                  v-else
                  type="success" 
                  size="large"
                  plain 
                  :icon="Check" 
                  disabled
                >
                  报告已生效
                </el-button>
              </div>
            </div>

            <div class="paper-viewport">
              <el-scrollbar height="100%" class="custom-scrollbar">
                <div class="scale-wrapper" :style="{ width: `${960 * paperScale}px`, height: `${1200 * paperScale}px` }">
                  
                  <div 
                    class="a4-paper" 
                    id="print-area"
                    :style="{ transform: `scale(${paperScale})`, transformOrigin: 'top left' }"
                  >
                    <div class="paper-header">
                      <h2>数智肝循中心</h2>
                      <h1>遗传代谢性肝病 - AI 辅助筛查与专家评估报告</h1>
                      <div class="header-divider"></div>
                    </div>

                    <el-descriptions class="patient-info-table" :column="4" border size="default">
                      <el-descriptions-item label="姓名"><strong>{{ selectedReport.patientName }}</strong></el-descriptions-item>
                      <el-descriptions-item label="性别">{{ selectedReport.gender }}</el-descriptions-item>
                      <el-descriptions-item label="年龄">{{ selectedReport.age }} 岁</el-descriptions-item>
                      <el-descriptions-item label="就诊科室">肝病医学科</el-descriptions-item>
                      <el-descriptions-item label="门诊号">{{ selectedReport.visitId }}</el-descriptions-item>
                      <el-descriptions-item label="筛查日期">{{ selectedReport.date }}</el-descriptions-item>
                      <el-descriptions-item label="AI 报告单号" :span="2">{{ selectedReport.id }}</el-descriptions-item>
                    </el-descriptions>

                    <div class="paper-section">
                      <div class="section-title"><el-icon><Monitor /></el-icon> AI 辅助评估客观所见</div>
                      <div class="section-content read-only-box">
                        <p><strong>关键生化异常：</strong>{{ selectedReport.aiFindings.biochemical }}</p>
                        <p><strong>临床表型特征：</strong>{{ selectedReport.aiFindings.clinical }}</p>
                        <p>
                          <strong>多模态数据提示：</strong>模型预测患病概率 {{ selectedReport.aiFindings.probability }}%，
                          高度指向 <span class="highlight-disease">{{ selectedReport.aiFindings.disease }}</span>。
                        </p>
                      </div>
                    </div>

                    <div class="paper-section">
                      <div class="section-title"><el-icon><Edit /></el-icon> 专家审核结论</div>
                      <el-input
                        v-model="selectedReport.expertConclusion"
                        type="textarea"
                        :rows="5"
                        :disabled="selectedReport.status === '已签发'"
                        placeholder="请结合 AI 提示与临床经验，输入最终诊断结论..."
                        class="custom-textarea"
                      />
                    </div>

                    <div class="paper-section">
                      <div class="section-title"><el-icon><FirstAidKit /></el-icon> 诊疗干预与随访计划</div>
                      <el-input
                        v-model="selectedReport.treatmentPlan"
                        type="textarea"
                        :rows="5"
                        :disabled="selectedReport.status === '已签发'"
                        placeholder="请输入针对该患者的用药、饮食及随访计划..."
                        class="custom-textarea"
                      />
                    </div>

                    <div class="paper-footer">
                      <div class="footer-row">
                        <span>报告出具机构：数智肝循医学中心</span>
                        <span>报告日期：{{ currentDate }}</span>
                      </div>
                      <div class="footer-row signature-area">
                        <span>审核专家签名：</span>
                        <div class="signature-line">
                          <span v-if="selectedReport.status === '已签发'" class="handwriting">李时珍 (主治医师)</span>
                        </div>
                      </div>
                    </div>

                  </div> </div> </el-scrollbar>
            </div>
          </template>

        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Search, Printer, Download, EditPen, Check, Monitor, Edit, FirstAidKit,
  ZoomIn, ZoomOut
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import diagnosisApi from '../../api/diagnosis'

const searchQuery = ref('')
const reportStatusFilter = ref('待签发')
const selectedReport = ref(null)
const loadingReports = ref(false)

const paperScale = ref(1.0)
const currentDate = new Date().toISOString().split('T')[0]
const reports = ref([])

const fetchReports = async () => {
  loadingReports.value = true
  try {
    const res = await diagnosisApi.getExpertReports()
    reports.value = res.data.items || []

    if (selectedReport.value) {
      const matched = reports.value.find((item) => item.id === selectedReport.value.id)
      selectedReport.value = matched || null
    }
  } catch {
    ElMessage.error('加载专家报告失败，请稍后重试')
  } finally {
    loadingReports.value = false
  }
}

const handleZoomIn = () => {
  if (paperScale.value < 2.0) {
    paperScale.value = Number((paperScale.value + 0.1).toFixed(1))
  }
}

const handleZoomOut = () => {
  if (paperScale.value > 0.5) {
    paperScale.value = Number((paperScale.value - 0.1).toFixed(1))
  }
}

const handleResetZoom = () => {
  paperScale.value = 1.0
}

const filteredReports = computed(() => {
  return reports.value.filter((report) => {
    const matchStatus = report.status === reportStatusFilter.value
    const keyword = searchQuery.value.trim()
    const matchSearch = !keyword || report.patientName.includes(keyword) || report.visitId.includes(keyword)
    return matchStatus && matchSearch
  })
})

const handleSelectReport = (report) => {
  selectedReport.value = report
  paperScale.value = 1.0
}

const handleSign = async () => {
  if (!selectedReport.value.expertConclusion || !selectedReport.value.treatmentPlan) {
    ElMessage.warning('请填写完整的专家审核结论与干预计划')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认签发患者 ${selectedReport.value.patientName} 的专家报告吗？签发后报告将锁定且不可修改。`,
      '签发确认',
      {
        confirmButtonText: '确认签发',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    const res = await diagnosisApi.signExpertReport(selectedReport.value.id, {
      expertConclusion: selectedReport.value.expertConclusion,
      treatmentPlan: selectedReport.value.treatmentPlan
    })

    const report = res.data.report
    const idx = reports.value.findIndex((item) => item.id === report.id)
    if (idx >= 0) {
      reports.value[idx] = report
      selectedReport.value = reports.value[idx]
    }

    reportStatusFilter.value = '已签发'
    ElMessage.success('报告签发成功，已完成同步')
  } catch {
    ElMessage.error('报告签发失败，请稍后重试')
  }
}

const handlePrint = () => {
  ElMessage.success('正在调用打印机控件...')
}

const handleExport = () => {
  if (!selectedReport.value) return
  ElMessage.success(`正在生成 ${selectedReport.value.patientName}_评估报告.pdf ...`)
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.report-manage-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

/* ======== 左侧面板样式 (全面放大字体和间距) ======== */
.left-panel {
  height: 100%;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.report-list-item {
  padding: 20px 16px; /* 增加上下内边距 */
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
}

.report-list-item:hover {
  background-color: #f9fafc;
}

.report-list-item.is-active {
  background-color: #ecf5ff;
  border-left-color: #409EFF;
}

.item-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.item-title .name {
  font-size: 20px; /* 姓名显著放大 */
  font-weight: bold;
  color: #303133;
}

.item-sub {
  font-size: 15px; /* 副标题放大 */
  color: #606266;
  margin-bottom: 6px;
}

.item-time {
  font-size: 13px;
  color: #909399;
}

/* ======== 右侧工作台及工具栏 ======== */
.right-panel {
  height: 100%;
  border-radius: 8px;
  background: #e4e7ed; /* 背景调暗，凸显白色图纸 */
}

.empty-state {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #fff;
}

.toolbar {
  height: 70px; /* 加高工具栏 */
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #dcdfe6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  flex-shrink: 0;
  z-index: 10;
}

.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
}

/* ======== 仿真 A4 视图视口区 (支持缩放与滚动) ======== */
.paper-viewport {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 用于包裹 scale，动态计算宽高以撑开 el-scrollbar */
.scale-wrapper {
  margin: 40px auto; /* 纸张与滚动区域边缘留出空隙 */
  transition: width 0.2s ease, height 0.2s ease;
}

/* 核心：逼真的大尺寸 A4 电子报告纸 */
.a4-paper {
  width: 960px; /* 宽度从 820px 增加到 960px */
  min-height: 1200px; /* 相应增加高度 */
  background: #ffffff;
  box-shadow: 0 12px 32px rgba(0,0,0,0.15); /* 加深阴影更加立体 */
  padding: 70px 80px; /* 增大内边距 */
  box-sizing: border-box;
  transition: transform 0.2s ease; /* 缩放动画 */
}

/* ======== A4 纸内部排版样式 (全面放大) ======== */
.paper-header {
  text-align: center;
  margin-bottom: 40px;
}

.paper-header h2 {
  font-size: 24px; /* 放大 */
  color: #606266;
  margin: 0 0 12px 0;
  font-weight: normal;
  letter-spacing: 4px;
}

.paper-header h1 {
  font-size: 32px; /* 放大 */
  color: #303133;
  margin: 0 0 24px 0;
  letter-spacing: 2px;
}

.header-divider {
  height: 3px;
  background: #303133;
  width: 100%;
  border-bottom: 1px solid #303133;
  margin-bottom: 6px;
}

/* 报告表格与内容区 */
.patient-info-table {
  margin-bottom: 40px;
}

:deep(.el-descriptions__body) {
  border-color: #303133 !important;
  font-size: 16px !important; /* 表格字体放大至 16px */
}

:deep(.el-descriptions__cell) {
  border-color: #303133 !important;
  color: #303133 !important;
  padding: 12px 16px !important; /* 加大单元格内边距 */
}

:deep(.el-descriptions__label) {
  background-color: #f5f7fa !important;
  font-weight: bold !important;
  width: 120px;
}

.paper-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px; /* 栏目标题放大 */
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.read-only-box {
  background: #fafafa;
  padding: 20px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  color: #303133;
  font-size: 16px; /* 文本放大 */
  line-height: 1.8;
}

.read-only-box p {
  margin: 0 0 10px 0;
}
.read-only-box p:last-child {
  margin: 0;
}

.highlight-disease {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}

/* 自定义输入框样式，融入打印纸风格 */
.custom-textarea :deep(.el-textarea__inner) {
  font-size: 16px; /* 输入框字体放大 */
  line-height: 1.8;
  color: #303133;
  padding: 16px;
  box-shadow: none;
  border: 1px solid #c0c4cc;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
}

.custom-textarea :deep(.el-textarea__inner:disabled) {
  background-color: transparent;
  color: #303133;
  border-color: transparent; /* 签发后隐藏边框 */
  cursor: default;
  padding: 0;
}

/* 报告页脚 */
.paper-footer {
  margin-top: 80px;
  padding-top: 24px;
  border-top: 2px solid #303133;
  color: #303133;
  font-size: 16px;
}

.footer-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.signature-area {
  justify-content: flex-end;
  align-items: flex-end;
  font-weight: bold;
}

.signature-line {
  width: 180px;
  border-bottom: 1px solid #303133;
  margin-left: 12px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 模拟手写签名的字体 */
.handwriting {
  font-family: "KaiTi", "楷体", serif;
  font-size: 28px; /* 签名显著放大 */
  font-weight: bold;
  color: #303133;
}
</style>
