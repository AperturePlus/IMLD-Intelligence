<template>
  <div class="screening-data-container" v-loading="loading">
    <div class="header-actions">
      <div class="title-area">
        <el-text tag="b" size="large" class="main-title">
          <el-icon class="mr-2"><DataAnalysis /></el-icon> 人群筛查数据分析中心
        </el-text>
        <el-text type="info" size="small">数据更新时间：{{ currentTime }}</el-text>
      </div>
      <div class="action-area">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="large"
          style="margin-right: 16px; width: 320px;"
        />
        <el-button type="primary" :icon="Download" size="large" @click="exportData">
          导出筛查报表
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <el-text type="info">{{ card.title }}</el-text>
            <el-icon :color="card.color" :size="20"><component :is="card.icon" /></el-icon>
          </div>
          <div class="card-body">
            <el-statistic :value="card.value" :value-style="{ color: card.color, fontSize: '32px', fontWeight: 'bold' }">
              <template v-if="card.suffix" #suffix>{{ card.suffix }}</template>
            </el-statistic>
          </div>
          <div class="card-footer">
            <el-text size="small" type="info">较上月 </el-text>
            <el-text size="small" :type="card.trend > 0 ? 'danger' : 'success'">
              <el-icon><component :is="card.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
              {{ Math.abs(card.trend) }}%
            </el-text>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-title">风险等级人群分布</div>
          </template>
          <el-scrollbar height="100%">
            <div class="scroll-content risk-distribution">
              <div class="risk-item" v-for="item in riskDistribution" :key="item.level">
                <div class="risk-info">
                  <span class="risk-label">{{ item.level }}风险</span>
                  <span class="risk-count">{{ item.count }} 人</span>
                </div>
                <el-progress 
                  :percentage="item.percentage" 
                  :color="item.color" 
                  :stroke-width="12"
                  :show-text="false"
                />
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-title">高频致病基因变异分布</div>
          </template>
          <el-scrollbar height="100%">
            <div class="scroll-content gene-chart">
              <div class="gene-item" v-for="(gene, index) in topGenes" :key="gene.name">
                <div class="gene-rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                <div class="gene-name">
                  <div>{{ gene.name }}</div>
                  <div class="gene-desc">{{ gene.desc }}</div>
                </div>
                <div class="gene-bar">
                  <el-progress 
                    :percentage="gene.percentage" 
                    color="#409EFF" 
                    :stroke-width="14"
                  />
                </div>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-title">系统 AI 筛查效能</div>
          </template>
          <el-scrollbar height="100%">
            <div class="scroll-content ai-efficiency">
              <el-progress 
                type="dashboard" 
                :percentage="aiEfficiency.diagnosisMatchRate || 0" 
                color="#67c23a" 
                :width="160"
                :stroke-width="14"
              >
                <template #default>
                  <div class="efficiency-value">{{ aiEfficiency.diagnosisMatchRate || 0 }}%</div>
                  <div class="efficiency-label">诊断吻合率</div>
                </template>
              </el-progress>
              <div class="efficiency-stats">
                <div class="e-stat"><span class="label">漏诊率</span><span class="val success">{{ aiEfficiency.missRate || '0%' }}</span></div>
                <div class="e-stat"><span class="label">平均耗时</span><span class="val primary">{{ aiEfficiency.avgDuration || '--' }}</span></div>
              </div>
            </div>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="alert-table-card">
      <template #header>
        <div class="table-header">
          <div class="chart-title"><el-icon color="#f56c6c"><WarningFilled /></el-icon> 近期新增高危阳性预警台账</div>
          <el-button type="danger" plain size="small">查看全部预警</el-button>
        </div>
      </template>
      <el-table :data="highRiskPatients" border style="width: 100%" stripe>
        <el-table-column prop="date" label="筛查日期" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="clue" label="核心异常线索" />
        <el-table-column prop="aiSuggest" label="AI 倾向诊断" width="220">
          <template #default="scope">
            <el-tag type="danger" effect="light">{{ scope.row.aiSuggest }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleReview(scope.row)">建立随访</el-button>
            <el-button type="info" link size="small">发短信</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import {
  DataAnalysis, Download, WarningFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import managementApi from '../../api/management'
import type {
  AiEfficiencyMetrics,
  HighRiskPatientItem,
  RiskDistributionItem,
  ScreeningOverviewResponse,
  StatCard,
  TopGeneItem
} from '../../api/types'

const dateRange = ref<[Date | string, Date | string] | []>([])
const currentTime = ref('')
const loading = ref(false)

const statCards = ref<StatCard[]>([])
const riskDistribution = ref<RiskDistributionItem[]>([])
const topGenes = ref<TopGeneItem[]>([])
const aiEfficiency = ref<AiEfficiencyMetrics>({
  diagnosisMatchRate: 0,
  missRate: '0%',
  avgDuration: '--'
})
const highRiskPatients = ref<HighRiskPatientItem[]>([])

const fetchOverview = async () => {
  loading.value = true
  try {
    const [from, to] = Array.isArray(dateRange.value) ? dateRange.value : []
    const params = {
      from: from ? new Date(from).toISOString().slice(0, 10) : '',
      to: to ? new Date(to).toISOString().slice(0, 10) : ''
    }

    const res = await managementApi.getScreeningOverview(params)
    const payload: Partial<ScreeningOverviewResponse> = res.data ?? {}
    statCards.value = payload.statCards ?? []
    riskDistribution.value = payload.riskDistribution ?? []
    topGenes.value = payload.topGenes ?? []
    aiEfficiency.value = payload.aiEfficiency ?? { diagnosisMatchRate: 0, missRate: '0%', avgDuration: '--' }
    highRiskPatients.value = payload.highRiskPatients ?? []
    currentTime.value = payload.updatedAt ?? new Date().toLocaleString()
  } catch {
    ElMessage.error('加载筛查数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const exportData = () => {
  ElMessage.success('正在导出区域筛查与流行病学统计报表...')
}

const handleReview = (row: HighRiskPatientItem) => {
  ElMessage.info(`正在为患者 ${row.name} 建立干预随访档案...`)
}

watch(dateRange, () => {
  fetchOverview()
})

onMounted(() => {
  fetchOverview()
})
</script>

<style scoped>
.screening-data-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 顶部操作区 */
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.title-area {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
}

.mr-2 { margin-right: 8px; }

/* 核心指标卡片 */
.stat-row {
  margin-bottom: 24px;
}

.data-card {
  border-radius: 8px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-bottom: 12px;
}

.card-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  align-items: center;
}

/* 图表区公共样式 (修改重点：支持弹性滚动) */
.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 8px;
  height: 360px; /* 稍微增加了基础高度 */
  display: flex;
  flex-direction: column;
}

/* 深度重写 el-card 的 body 样式，让其完全交由 el-scrollbar 接管 */
.chart-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

/* el-scrollbar 内部真正包裹内容的容器 */
.scroll-content {
  padding: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 风险等级分布样式 */
.risk-distribution {
  display: flex;
  flex-direction: column;
  gap: 24px; /* 加大了间距 */
}

.risk-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.risk-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.risk-label { color: #606266; }
.risk-count { font-weight: bold; color: #303133; }

/* 基因突变排行样式 */
.gene-chart {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 加大了间距，配合滚动条 */
}

.gene-item {
  display: flex;
  align-items: center;
}

.gene-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #f4f4f5;
  color: #909399;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-size: 13px;
  margin-right: 12px;
  flex-shrink: 0;
}

/* 前三名突出显示 */
.rank-1 { background: #fef0f0; color: #f56c6c; }
.rank-2 { background: #fdf6ec; color: #e6a23c; }
.rank-3 { background: #f0f9eb; color: #67c23a; }

.gene-name {
  width: 140px;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.gene-desc {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-top: 2px;
}

.gene-bar {
  flex: 1;
}

/* AI 效能样式 */
.ai-efficiency {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 250px;
}

.efficiency-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.efficiency-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.efficiency-stats {
  display: flex;
  width: 100%;
  justify-content: space-around;
  margin-top: 24px;
  padding: 0 10px;
}

.e-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #f5f7fa;
  padding: 10px 20px;
  border-radius: 6px;
}

.e-stat .label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.e-stat .val { font-size: 16px; font-weight: bold; }
.e-stat .val.success { color: #67c23a; }
.e-stat .val.primary { color: #409EFF; }

/* 底部预警台账 */
.alert-table-card {
  border-radius: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
