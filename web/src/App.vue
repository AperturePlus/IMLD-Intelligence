<script setup lang="ts">
import { computed, ref } from "vue";
import { ElConfigProvider } from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import DesktopTitlebar from "@/components/organisms/DesktopTitlebar.vue";

const locale = ref(zhCn);
const isFramelessShell = computed(
  () =>
    typeof window !== "undefined" && Boolean(window.electron?.shell?.frameless)
);
</script>

<template>
  <el-config-provider :locale="locale">
    <DesktopTitlebar v-if="isFramelessShell" />
    <router-view />
  </el-config-provider>
</template>
