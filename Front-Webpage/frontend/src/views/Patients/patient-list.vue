<template>
  <div v-loading="loading" style="padding: 24px; background-color: #f5f7fa; min-height: 100vh;">
    <el-card shadow="never" style="margin-bottom: 24px;">
      <el-row justify="space-between" align="middle">
        <el-col :xs="16" :sm="12" :md="8">
          <el-input
            v-model="searchQuery"
            placeholder="请输入患者姓名或拼音搜索..."
            clearable
            :prefix-icon="Search"
            size="large"
          />
        </el-col>
        <el-col :span="8" style="text-align: right;">
          <el-button type="primary" :icon="Plus" size="large" @click="handleExternalAdd">
            在医疗系统中添加
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20">
      <el-col
        v-for="patient in filteredPatients"
        :key="patient.id"
        :xs="24" :sm="12" :md="12" :lg="8" :xl="6"
        style="margin-bottom: 20px;"
      >
        <el-card shadow="hover" :body-style="{ padding: '24px 20px' }">
          <el-row :gutter="16" align="middle">
            <el-col :span="6" style="text-align: center;">
              <el-avatar :size="64" :src="patient.avatar" style="border: 2px solid #e4e7ed;" />
            </el-col>

            <el-col :span="18">
              <div style="margin-bottom: 12px;">
                <el-text tag="b" style="margin-right: 12px; color: #303133; font-size: 20px;">
                  {{ patient.name }}
                </el-text>
                <el-text type="info" style="font-size: 15px;">
                  {{ patient.gender }} | {{ patient.age }} 岁
                </el-text>
              </div>

              <div style="display: flex; align-items: center;">
                <el-text type="info" style="margin-right: 8px; font-size: 14px;">
                  遗传代谢性肝病风险:
                </el-text>
                <el-tag
                  :type="getRiskTagType(patient.riskLevel)"
                  effect="light"
                  round
                >
                  {{ patient.riskLevel }}风险
                </el-tag>
              </div>
            </el-col>
          </el-row>

          <template #footer>
            <el-row justify="end">
              <el-button link type="primary">查看详情</el-button>
              <el-button link type="info">电子病历</el-button>
            </el-row>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && filteredPatients.length === 0" description="未找到匹配的患者信息" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import patientApi from '../../api/patient'

const searchQuery = ref('')
const loading = ref(false)
const patients = ref([])
let searchTimer = null

const filteredPatients = computed(() => patients.value)

const fetchPatients = async () => {
  loading.value = true
  try {
    const res = await patientApi.list({
      keyword: searchQuery.value.trim()
    })
    patients.value = res.data.items || []
  } catch {
    ElMessage.error('加载患者列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getRiskTagType = (level) => {
  const map = {
    高: 'danger',
    中: 'warning',
    低: 'success'
  }
  return map[level] || 'info'
}

const handleExternalAdd = () => {
  ElMessage({
    message: '正在拉起外部医疗数据库检索模块...',
    type: 'success',
    duration: 2000
  })
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
/* 全部交由 Element Plus 处理，零 CSS 负担 */
</style>
