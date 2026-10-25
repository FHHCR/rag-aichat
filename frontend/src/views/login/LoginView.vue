<template>
  <div class="login-page">
    <form class="panel login-card" @submit.prevent="submitLogin">
      <h1>登录 Rag-AiChat</h1>
      <p class="muted">管理员维护知识库，普通用户使用聊天页面。</p>

      <label>
        用户名
        <input v-model.trim="username" autocomplete="username" />
      </label>

      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" />
      </label>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button class="primary" type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login } from '../../api/auth';
import { useAuthStore } from '../../stores/auth';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const username = ref('admin');
const password = ref('admin123');
const loading = ref(false);
const errorMessage = ref('');

async function submitLogin() {
  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const result = await login(username.value, password.value);
    authStore.setAuth(result.token, result.user);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : authStore.isAdmin ? '/admin' : '/chat';
    router.push(redirect);
  } catch (error) {
    errorMessage.value = axios.isAxiosError(error) && error.response?.data?.message
      ? error.response.data.message
      : '登录失败，请检查账号密码';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

form label {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  color: var(--muted);
  font-size: 14px;
}

button {
  width: 100%;
  margin-top: 18px;
}

.error {
  color: var(--danger);
  margin-top: 12px;
}
</style>
