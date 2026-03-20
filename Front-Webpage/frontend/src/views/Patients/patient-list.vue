<template>
  <div style="padding: 24px; background-color: #f5f7fa; min-height: 100vh;">
    
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

    <el-empty v-if="filteredPatients.length === 0" description="未找到匹配的患者信息" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// --- 状态管理 ---
const searchQuery = ref('')

// --- 扩充后的真实模拟数据 (20条) ---
const patients = ref([
  { id: 'P001', name: '林建国', gender: '男', age: 58, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/men/32.jpg' },
  { id: 'P002', name: '陈婉婷', gender: '女', age: 32, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/women/44.jpg' },
  { id: 'P003', name: '张明远', gender: '男', age: 45, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/men/67.jpg' },
  { id: 'P004', name: '王淑芬', gender: '女', age: 62, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/women/68.jpg' },
  { id: 'P005', name: '李浩宇', gender: '男', age: 28, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/men/22.jpg' },
  { id: 'P006', name: '赵雪梅', gender: '女', age: 51, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/women/33.jpg' },
  { id: 'P007', name: '刘振华', gender: '男', age: 66, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/men/45.jpg' },
  { id: 'P008', name: '周小雅', gender: '女', age: 24, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/women/12.jpg' },
  { id: 'P009', name: '吴建强', gender: '男', age: 53, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/men/51.jpg' },
  { id: 'P010', name: '郑丽丽', gender: '女', age: 38, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/women/25.jpg' },
  { id: 'P011', name: '孙立军', gender: '男', age: 41, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/men/18.jpg' },
  { id: 'P012', name: '马桂英', gender: '女', age: 71, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/women/71.jpg' },
  { id: 'P013', name: '黄子韬', gender: '男', age: 31, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/men/82.jpg' },
  { id: 'P014', name: '郭晓丹', gender: '女', age: 29, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/women/55.jpg' },
  { id: 'P015', name: '冯伟', gender: '男', age: 48, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/men/61.jpg' },
  { id: 'P016', name: '曹玉兰', gender: '女', age: 64, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/women/62.jpg' },
  { id: 'P017', name: '彭志远', gender: '男', age: 36, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/men/39.jpg' },
  { id: 'P018', name: '杨秀云', gender: '女', age: 55, riskLevel: '中', avatar: 'https://randomuser.me/api/portraits/women/48.jpg' },
  { id: 'P019', name: '何文斌', gender: '男', age: 60, riskLevel: '高', avatar: 'https://randomuser.me/api/portraits/men/74.jpg' },
  { id: 'P020', name: '许静', gender: '女', age: 27, riskLevel: '低', avatar: 'https://randomuser.me/api/portraits/women/9.jpg' }
])

// --- 计算属性：过滤患者列表 ---
const filteredPatients = computed(() => {
  if (!searchQuery.value) return patients.value
  const query = searchQuery.value.toLowerCase()
  return patients.value.filter(patient => 
    patient.name.includes(query)
  )
})

// --- 方法 ---

// 根据风险等级匹配 Tag 的颜色类型
const getRiskTagType = (level) => {
  const map = {
    '高': 'danger',   // 红色
    '中': 'warning',  // 橙色
    '低': 'success'   // 绿色
  }
  return map[level] || 'info'
}

// 外部系统添加按钮事件
const handleExternalAdd = () => {
  ElMessage({
    message: '正在拉起外部医疗数据库检索模块...',
    type: 'success',
    duration: 2000
  })
}
</script>

<style scoped>
/* 全部交由 Element Plus 处理，零 CSS 负担 */
</style>