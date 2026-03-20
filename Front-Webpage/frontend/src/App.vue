<script setup lang="ts">
import { ref } from 'vue'
import { ElConfigProvider } from 'element-plus'
// 引入中文包
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 将语言设置为中文
const locale = ref(zhCn)
</script>

<template>
   <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<style scoped>

</style>