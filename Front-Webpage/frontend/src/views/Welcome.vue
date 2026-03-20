<template>
  <div class="welcome-page" :style="containerStyle">
    <div class="welcome-content">
      <h1 class="welcome-title">{{ username }}, 欢迎您</h1>
      <p class="welcome-subtitle">点击侧边栏开始管理吧</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue';

const username = ref('用户');

// 侧边栏折叠逻辑（保持不变）
const isCollapse = inject('isCollapse', ref(false)); // 假如 inject 失败，默认 false

// 注意：这里只控制 layout，背景图在 css 类中控制
const containerStyle = computed(() => ({
  marginLeft: isCollapse.value ? '64px' : '0px', // 根据侧边栏宽度调整左边距
  transition: 'margin-left 0.2s',
  width: isCollapse.value ? 'calc(100vw - 64px)' : 'calc(100vw - 220px)' // 建议加上宽度限制，防止溢出
}));

onMounted(() => {
  const storedName = localStorage.getItem('username');
  if (storedName) {
    username.value = storedName;
  }
});
</script>

<style scoped>
.welcome-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  box-sizing: border-box;
  
  /* --- 核心修改部分 --- */
  /* 1. linear-gradient: 添加一个 90% 透明度的白色遮罩，防止背景图太花导致文字看不清
     2. url: 引用图片
     3. no-repeat center center fixed: 不平铺、居中、固定
  */
  background: 
    linear-gradient(rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.1)), 
    url("../assets/login.webp") no-repeat center center;
    
  /* 确保背景图覆盖整个屏幕 */
  background-size: cover; 
}

.welcome-content {
  text-align: center;
  color: #333;
  /* 给内容区域加一点点阴影，让它更有层次感（可选） */
  padding: 40px;
  border-radius: 12px;
}

.welcome-title {
  font-size: 60px;
  font-weight: bold;
  margin-bottom: 16px;
  /* 标题渐变色保持不变 */
  background: linear-gradient(90deg, #f80ab1, #5dbdf8);
  -webkit-background-clip: text; 
  background-clip: text; 
  -webkit-text-fill-color: transparent; 
}

.welcome-subtitle {
  font-size: 36px ;
  color: #fffefe; /* 稍微加深一点颜色，提升在图片上的对比度 */
  font-weight: bold;
}
</style>