<template>
  <div class="center-layout">
    <Leftaside />
    <div class="center-main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import Leftaside from '../components/Leftaside.vue'
</script>

<style scoped>
.center-layout {
  display: flex;
  min-height: 100vh;
  background: #0f1f2c;
}

.center-main {
  flex: 1;
  min-height: 100vh;
  overflow-y: auto;
  background:
    radial-gradient(circle at 2% 2%, rgba(34, 163, 159, 0.14), transparent 32%),
    radial-gradient(circle at 98% 0%, rgba(15, 109, 141, 0.14), transparent 34%),
    #eef3f8;
}
</style>
