<template>
  <div class="diet-plan-container" v-loading="loadingPatients">
    <el-row :gutter="20" class="full-height">
      
      <el-col :span="6" class="full-height">
        <el-card class="left-panel" shadow="never">
          <div class="panel-header">
            <el-text tag="b" size="large">慢病膳食管理</el-text>
            <el-input
              v-model="searchQuery"
              placeholder="搜索患者姓名"
              :prefix-icon="Search"
              clearable
              style="margin-top: 12px;"
            />
          </div>
          
          <el-scrollbar height="calc(100vh - 160px)" style="margin-top: 12px;">
            <div 
              v-for="patient in filteredPatients" 
              :key="patient.id"
              class="patient-list-item"
              :class="{ 'is-active': selectedPatient?.id === patient.id }"
              @click="handleSelectPatient(patient)"
            >
              <el-avatar :size="46" :src="patient.avatar" />
              <div class="item-info">
                <div class="item-header">
                  <span class="name">{{ patient.name }}</span>
                  <el-tag :type="getComplianceType(patient.compliance)" size="small" effect="dark">
                    依从性: {{ patient.compliance }}
                  </el-tag>
                </div>
                <div class="item-sub">{{ patient.disease }}</div>
              </div>
            </div>
            <el-empty v-if="filteredPatients.length === 0" description="未找到匹配的患者" />
          </el-scrollbar>
        </el-card>
      </el-col>

      <el-col :span="18" class="full-height">
        <el-card class="right-panel" shadow="never" v-loading="loadingPlan">
          
          <div v-if="!selectedPatient" class="empty-state">
            <el-empty description="请从左侧选择慢病患者，制定个性化膳食方案" />
          </div>

          <div v-else class="diet-dashboard">
            <div class="dashboard-header">
              <div class="header-left">
                <el-avatar :size="64" :src="selectedPatient.avatar" />
                <div class="header-text">
                  <h2>{{ selectedPatient.name }} <span class="age-gender">{{ selectedPatient.gender }} | {{ selectedPatient.age }}岁</span></h2>
                  <p class="disease-tag">管理病种：<el-tag effect="plain" type="danger">{{ selectedPatient.disease }}</el-tag></p>
                </div>
              </div>
              <div class="header-right">
                <el-button type="primary" :icon="Position" size="large" @click="pushToPatient">
                  一键推送至患者端
                </el-button>
                <el-button :icon="Printer" size="large" @click="printDietPlan">
                  打印膳食处方
                </el-button>
              </div>
            </div>

            <el-divider />

            <div class="section-container">
              <el-text tag="b" class="section-title"><el-icon><Aim /></el-icon> 核心生化干预目标</el-text>
              <el-row :gutter="20" style="margin-top: 16px;">
                <el-col :span="8" v-for="target in selectedPatient.targets" :key="target.label">
                  <el-card shadow="hover" class="target-card">
                    <div class="target-label">{{ target.label }}</div>
                    <div class="target-value" :style="{ color: target.color }">
                      {{ target.value }} <span class="target-unit">{{ target.unit }}</span>
                    </div>
                    <div class="target-desc">{{ target.desc }}</div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <div class="section-container" style="margin-top: 30px;">
              <el-text tag="b" class="section-title"><el-icon><Warning /></el-icon> 专病食物红黄绿灯法则</el-text>
              <el-row :gutter="20" style="margin-top: 16px;">
                <el-col :span="8">
                  <el-card class="traffic-card red-light" shadow="never">
                    <template #header>
                      <div class="traffic-header">
                        <span class="dot red-dot"></span> 绝对禁忌 (红灯)
                      </div>
                    </template>
                    <div class="food-tags">
                      <el-tag v-for="food in selectedPatient.foods.red" :key="food" type="danger" effect="dark" class="food-tag">
                        {{ food }}
                      </el-tag>
                    </div>
                  </el-card>
                </el-col>

                <el-col :span="8">
                  <el-card class="traffic-card yellow-light" shadow="never">
                    <template #header>
                      <div class="traffic-header">
                        <span class="dot yellow-dot"></span> 严格限量 (黄灯)
                      </div>
                    </template>
                    <div class="food-tags">
                      <el-tag v-for="food in selectedPatient.foods.yellow" :key="food" type="warning" effect="dark" class="food-tag">
                        {{ food }}
                      </el-tag>
                    </div>
                  </el-card>
                </el-col>

                <el-col :span="8">
                  <el-card class="traffic-card green-light" shadow="never">
                    <template #header>
                      <div class="traffic-header">
                        <span class="dot green-dot"></span> 推荐食用 (绿灯)
                      </div>
                    </template>
                    <div class="food-tags">
                      <el-tag v-for="food in selectedPatient.foods.green" :key="food" type="success" effect="dark" class="food-tag">
                        {{ food }}
                      </el-tag>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <div class="section-container" style="margin-top: 30px;">
              <div class="meal-header">
                <el-text tag="b" class="section-title"><el-icon><Food /></el-icon> AI 智能每日食谱推荐</el-text>
                <el-button type="primary" plain :icon="Refresh" size="small" @click="regenerateMeal">重新生成食谱</el-button>
              </div>
              
              <el-table :data="selectedPatient.mealPlan" border style="width: 100%; margin-top: 16px;" header-cell-class-name="meal-table-header">
                <el-table-column prop="time" label="餐次" width="120" align="center" cell-class-name="meal-type-cell">
                  <template #default="scope">
                    <el-tag size="default" :type="scope.row.type" effect="dark">{{ scope.row.time }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="menu" label="推荐菜品" />
                <el-table-column prop="nutrition" label="核心营养估算" width="280">
                  <template #default="scope">
                    <el-text type="info" size="small">{{ scope.row.nutrition }}</el-text>
                  </template>
                </el-table-column>
              </el-table>
            </div>

          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Search, Position, Printer, Aim, Warning, Food, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import managementApi from '../../api/management'

const searchQuery = ref('')
const selectedPatient = ref(null)
const loadingPatients = ref(false)
const loadingPlan = ref(false)
const patients = ref([])
let searchTimer = null

const filteredPatients = computed(() => patients.value)

const fetchPatients = async () => {
  loadingPatients.value = true
  try {
    const res = await managementApi.getDietPatients({
      keyword: searchQuery.value.trim()
    })
    patients.value = res.data.items || []
  } catch {
    ElMessage.error('加载慢病患者列表失败，请稍后重试')
  } finally {
    loadingPatients.value = false
  }
}

const handleSelectPatient = async (patient) => {
  loadingPlan.value = true
  try {
    const res = await managementApi.getDietPlan(patient.id)
    selectedPatient.value = {
      ...patient,
      ...res.data
    }
  } catch {
    ElMessage.error('加载患者饮食方案失败，请稍后重试')
  } finally {
    loadingPlan.value = false
  }
}

const getComplianceType = (level) => {
  const map = { 极佳: 'success', 良好: 'primary', 一般: 'warning', 差: 'danger' }
  return map[level] || 'info'
}

const regenerateMeal = async () => {
  if (!selectedPatient.value) return

  try {
    const res = await managementApi.regenerateDietPlan(selectedPatient.value.id)
    selectedPatient.value.mealPlan = res.data.mealPlan || []
    ElMessage.success('食谱已重新生成')
  } catch {
    ElMessage.error('生成失败，请稍后重试')
  }
}

const pushToPatient = async () => {
  if (!selectedPatient.value) return

  try {
    await ElMessageBox.confirm(
      `确认将《${selectedPatient.value.disease} 个性化膳食处方》推送至患者【${selectedPatient.value.name}】端吗？`,
      '推送确认',
      {
        confirmButtonText: '确认发送',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
  } catch {
    return
  }

  try {
    await managementApi.pushDietPlan(selectedPatient.value.id)
    ElMessage({ type: 'success', message: '已成功送达患者端，系统将开启饮食打卡监督。' })
  } catch {
    ElMessage.error('推送失败，请稍后重试')
  }
}

const printDietPlan = () => {
  ElMessage.info('准备生成 PDF 打印件...')
}

watch(searchQuery, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    fetchPatients()
  }, 250)
})

onMounted(() => {
  fetchPatients()
})

onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})
</script>

<style scoped>
.diet-plan-container {
  padding: 24px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

/* ======== 左侧患者列表 ======== */
.left-panel {
  height: 100%;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}

.patient-list-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.patient-list-item:hover {
  background-color: #f9fafc;
}

.patient-list-item.is-active {
  background-color: #ecf5ff;
  border-left-color: #409EFF;
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
  color: #606266;
}

/* ======== 右侧工作台 ======== */
.right-panel {
  height: 100%;
  border-radius: 8px;
}

:deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
  padding: 0;
}

.empty-state {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.diet-dashboard {
  padding: 30px;
  height: 100%;
  overflow-y: auto;
}

/* 头部档案 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-text h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.age-gender {
  font-size: 16px;
  color: #909399;
  font-weight: normal;
  margin-left: 12px;
}

.disease-tag {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 模块通用标题 */
.section-title {
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 核心目标卡片 */
.target-card {
  text-align: center;
  border-radius: 8px;
}

.target-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.target-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.target-unit {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.target-desc {
  font-size: 12px;
  color: #909399;
}

/* 交通灯食物库 */
.traffic-card {
  border-radius: 8px;
  height: 100%;
}

.traffic-header {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.red-dot { background-color: #f56c6c; box-shadow: 0 0 8px rgba(245, 108, 108, 0.6); }
.yellow-dot { background-color: #e6a23c; box-shadow: 0 0 8px rgba(230, 162, 60, 0.6); }
.green-dot { background-color: #67c23a; box-shadow: 0 0 8px rgba(103, 194, 58, 0.6); }

/* 微调卡片头部背景色 */
.red-light :deep(.el-card__header) { background-color: #fef0f0; border-bottom: 1px solid #fde2e2; }
.yellow-light :deep(.el-card__header) { background-color: #fdf6ec; border-bottom: 1px solid #faecd8; }
.green-light :deep(.el-card__header) { background-color: #f0f9eb; border-bottom: 1px solid #e1f3d8; }

.food-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.food-tag {
  font-size: 14px;
  padding: 6px 14px;
  height: auto;
  border-radius: 20px;
}

/* 排餐表格 */
.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.meal-table-header) {
  background-color: #f5f7fa !important;
  color: #303133;
  font-weight: bold;
}

/* ***以下修改了针对特定单元格的深度选择器样式*** */
/* 作用于 meal-type-cell 单元格，完美垂直居中 */
:deep(.meal-type-cell) {
  padding: 0px 0 !important; /* 彻底移除标准单元格内边距 */
}

/* 使单元格内的 cell div 成为 flex 容器，完美居中，不再爆满 */
:deep(.meal-type-cell .cell) {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 0 !important; /* 彻底移除 cell div 内边距 */
}
</style>

