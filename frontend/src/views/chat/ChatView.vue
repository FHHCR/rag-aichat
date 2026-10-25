<template>
  <BaseLayout>
    <section class="page">
      <h1>聊天</h1>
      <div class="panel chat-placeholder">
        <p class="muted">聊天能力将在 v0.9 接入 RAG 检索、LLM 回答和引用来源。</p>
      </div>
    </section>
  </BaseLayout>
</template>

<script setup lang="ts">
import BaseLayout from '../../layouts/BaseLayout.vue';
</script>

<style scoped>
.page {
  max-width: 880px;
  margin: 0 auto;
}

.chat-placeholder {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
