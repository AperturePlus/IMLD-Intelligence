<!-- filepath: d:\Codes\JieBao\VictoryReport\VictoryReportFrontend\src\views\center.vue -->
<template>
  <div class="center-layout">
    <Leftaside />
    <div class="center-main">
      <router-view />
      <!-- 你可以在这里继续添加其他内容 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import Leftaside from '../components/Leftaside.vue'
</script>

<style scoped>
.center-layout {
  display: flex;
  height: 100vh; /* 保证侧边栏和内容区高度一致 */
  overflow: hidden;
}
.center-main {
  flex: 1;
  height: 100vh;
  background: #f5f6fa;
  overflow-y: auto; /* 内容区可滚动 */
  box-sizing: border-box;
}
</style>