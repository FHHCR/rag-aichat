<template>
  <div class="app-shell">
    <TopBar />
    <main class="app-main">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import TopBar from '../components/TopBar.vue';
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 20px;
}
</style>
