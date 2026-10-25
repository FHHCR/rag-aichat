<template>
  <BaseLayout>
    <section class="page">
      <h1>管理后台</h1>
      <div class="panel admin-placeholder">
        <p class="muted">知识库管理、文件上传和系统状态将在后续版本完成。</p>
      </div>
    </section>
  </BaseLayout>
</template>

<script setup lang="ts">
import BaseLayout from '../../layouts/BaseLayout.vue';
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
}

.admin-placeholder {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
