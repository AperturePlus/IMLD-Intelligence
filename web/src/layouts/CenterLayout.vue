<template>
  <div class="center-layout">
    <AppSidebar />
    <div class="center-main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from "@/components/organisms/AppSidebar.vue";
</script>

<style scoped>
.center-layout {
  display: flex;
  height: calc(100vh - var(--electron-titlebar-safe-top));
  background: #0f1f2c;
  overflow: hidden;
}

.center-main {
  flex: 1;
  height: calc(100vh - var(--electron-titlebar-safe-top));
  overflow-y: auto;
  background: radial-gradient(
      circle at 2% 2%,
      rgba(34, 163, 159, 0.14),
      transparent 32%
    ),
    radial-gradient(circle at 98% 0%, rgba(15, 109, 141, 0.14), transparent 34%),
    #eef3f8;
}
</style>
