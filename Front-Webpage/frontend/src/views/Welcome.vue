<template>
  <div class="welcome-page">
    <section class="hero-card">
      <div class="hero-content">
        <p class="hero-kicker">Clinical Workspace</p>
        <h1>{{ username }}，欢迎回来</h1>
        <p class="hero-subtitle">
          今天可以继续处理患者档案、AI 诊断和慢病管理任务。所有核心功能可在院内网络下独立运行。
        </p>
        <div class="hero-tags">
          <el-tag effect="light" type="info">离线可运行</el-tag>
          <el-tag effect="light" type="success">审计可追溯</el-tag>
          <el-tag effect="light" type="warning">最小数据出域</el-tag>
        </div>
      </div>

      <div class="hero-actions">
        <el-button type="primary" size="large" @click="go('/center/patient-list')">
          进入患者列表
        </el-button>
        <el-button size="large" @click="go('/center/patient-record')">
          新建病历
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in stats" :key="item.label" class="stat-card">
        <div class="stat-top">
          <span class="icon-badge">
            <el-icon :size="18">
              <component :is="item.icon" />
            </el-icon>
          </span>
          <span class="stat-trend">{{ item.trend }}</span>
        </div>
        <p class="stat-value">{{ item.value }}</p>
        <p class="stat-label">{{ item.label }}</p>
      </article>
    </section>

    <section class="quick-section">
      <h2>快捷入口</h2>
      <div class="quick-grid">
        <button
          v-for="action in quickActions"
          :key="action.title"
          class="quick-item"
          type="button"
          @click="go(action.path)"
        >
          <span class="quick-title">{{ action.title }}</span>
          <span class="quick-desc">{{ action.desc }}</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, DataLine, Cpu, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const username = ref('医生')

const stats = [
  { label: '待随访患者', value: '126', trend: '较昨日 +8', icon: UserFilled },
  { label: 'AI 预警病例', value: '19', trend: '高危 7 例', icon: Cpu },
  { label: '本周筛查完成率', value: '87%', trend: '达标', icon: DataLine },
  { label: '未读通知', value: '12', trend: '含 3 条重点', icon: Bell }
]

const quickActions = [
  { title: '患者列表', desc: '按风险等级快速检索', path: '/center/patient-list' },
  { title: '病历录入', desc: '结构化录入临床信息', path: '/center/patient-record' },
  { title: '智能诊断', desc: '启动 AI 辅助诊断流程', path: '/center/ai-diagnosis' },
  { title: '筛查数据', desc: '查看数据质控与出域状态', path: '/center/data-screening' }
]

const go = (path: string) => {
  router.push(path)
}

const storedName = localStorage.getItem('username')
if (storedName) {
  username.value = storedName
}
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(circle at 8% 8%, rgba(34, 163, 159, 0.18), transparent 38%),
    radial-gradient(circle at 90% 0%, rgba(14, 110, 141, 0.18), transparent 40%),
    #f3f7fb;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  border-radius: 20px;
  background: linear-gradient(135deg, #0f6d8d, #1f8e98 64%, #22a39f);
  color: #f5fbff;
  box-shadow: 0 18px 36px rgba(15, 109, 141, 0.24);
  animation: riseIn 0.55s ease-out;
}

.hero-content {
  flex: 1;
}

.hero-kicker {
  margin: 0 0 8px;
  font-size: 0.82rem;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  opacity: 0.9;
}

.hero-card h1 {
  margin: 0;
  font-size: clamp(1.7rem, 3vw, 2.2rem);
  font-weight: 700;
}

.hero-subtitle {
  margin: 12px 0 0;
  line-height: 1.7;
  max-width: 640px;
  color: rgba(237, 247, 255, 0.93);
}

.hero-tags {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-actions {
  min-width: 190px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e5edf4;
  padding: 16px;
  box-shadow: 0 10px 24px rgba(23, 48, 66, 0.05);
  animation: riseIn 0.55s ease-out;
}

.stat-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon-badge {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: #e6f4f8;
  color: #0f6d8d;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.stat-trend {
  font-size: 0.8rem;
  color: #5d7388;
}

.stat-value {
  margin: 14px 0 2px;
  font-size: 2rem;
  font-weight: 700;
  color: #1c2d3f;
}

.stat-label {
  margin: 0;
  color: #667b8f;
  font-size: 0.88rem;
}

.quick-section {
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e5edf4;
  padding: 20px;
  box-shadow: 0 10px 24px rgba(23, 48, 66, 0.05);
  animation: riseIn 0.6s ease-out;
}

.quick-section h2 {
  margin: 0 0 14px;
  font-size: 1.05rem;
  color: #264056;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-item {
  border: 1px solid #dbe7f1;
  background: linear-gradient(180deg, #fbfdff, #f4f8fc);
  border-radius: 14px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.quick-item:hover {
  transform: translateY(-2px);
  border-color: #8bcad0;
  box-shadow: 0 10px 20px rgba(15, 109, 141, 0.12);
}

.quick-title {
  display: block;
  font-weight: 600;
  color: #183449;
}

.quick-desc {
  display: block;
  margin-top: 6px;
  font-size: 0.82rem;
  color: #5d7488;
  line-height: 1.5;
}

@keyframes riseIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .stats-grid,
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .welcome-page {
    padding: 18px;
  }

  .hero-card {
    flex-direction: column;
    padding: 20px;
  }

  .hero-actions {
    min-width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }

  .hero-actions :deep(.el-button) {
    flex: 1;
  }

  .stats-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
