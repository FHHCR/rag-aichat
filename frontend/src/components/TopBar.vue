<template>
  <header class="topbar">
    <div class="brand">Rag-AiChat</div>
    <div class="user-area">
      <span v-if="authStore.user" class="user-name">{{ authStore.user.displayName }}</span>
      <button @click="logout" v-if="authStore.isAuthenticated">退出</button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const router = useRouter();

function logout() {
  authStore.clearAuth();
  router.push('/login');
}
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: var(--panel);
  border-bottom: 1px solid var(--border);
}

.brand {
  font-weight: 700;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  color: var(--muted);
}
</style>
