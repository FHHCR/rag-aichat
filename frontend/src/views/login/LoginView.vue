<template>
  <div class="login-page">
    <form class="panel login-card" @submit.prevent="submit">
      <h1>{{ mode === 'register' ? '注册 Rag-AiChat' : '登录 Rag-AiChat' }}</h1>
      <p class="muted">管理员维护知识库，普通用户使用聊天页面。</p>

      <label>
        用户名
        <input v-model.trim="username" autocomplete="username" :disabled="mode === 'login'" />
      </label>

      <label>
        密码
        <input v-model="password" type="password" :autocomplete="mode === 'register' ? 'new-password' : 'current-password'" />
      </label>

      <label v-if="mode === 'register'">
        昵称
        <input v-model.trim="displayName" maxlength="100" />
      </label>

      <label v-if="mode === 'register'">
        确认密码
        <input v-model="confirmPassword" type="password" autocomplete="new-password" />
      </label>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <button class="primary" type="submit" :disabled="loading">
        {{ loading ? loadingText : submitLabel }}
      </button>

      <button class="secondary" type="button" :disabled="loading" @click="toggleMode">
        {{ toggleLabel }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login, register } from '../../api/auth';
import { useAuthStore } from '../../stores/auth';
import axios from 'axios';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const username = ref('admin');
const password = ref('admin123');
const displayName = ref('');
const confirmPassword = ref('');
const mode = ref<'login' | 'register'>('login');
const loading = ref(false);
const errorMessage = ref('');

const submitLabel = ref('登录');
const loadingText = ref('登录中...');
const toggleLabel = ref('没有账号？注册');

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login';
  errorMessage.value = '';

  if (mode.value === 'register') {
    username.value = '';
    password.value = '';
    displayName.value = '';
    confirmPassword.value = '';
    submitLabel.value = '注册';
    loadingText.value = '注册中...';
    toggleLabel.value = '已有账号？登录';
    return;
  }

  username.value = 'admin';
  password.value = 'admin123';
  submitLabel.value = '登录';
  loadingText.value = '登录中...';
  toggleLabel.value = '没有账号？注册';
}

async function submit() {
  if (!username.value || !password.value) {
    errorMessage.value = mode.value === 'register' ? '请输入用户名、密码和昵称' : '请输入用户名和密码';
    return;
  }

  if (mode.value === 'register' && (!displayName.value || displayName.value.length > 100)) {
    errorMessage.value = '昵称不能为空，且不能超过 100 个字符';
    return;
  }

  if (mode.value === 'register' && password.value !== confirmPassword.value) {
    errorMessage.value = '两次输入的密码不一致';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const result = mode.value === 'register'
      ? await register({
          username: username.value,
          password: password.value,
          displayName: displayName.value,
          confirmPassword: confirmPassword.value
        })
      : await login(username.value, password.value);
    authStore.setAuth(result.token, result.user);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : authStore.isAdmin ? '/admin' : '/chat';
    router.push(redirect);
  } catch (error) {
    errorMessage.value = axios.isAxiosError(error) && error.response?.data?.message
      ? error.response.data.message
      : mode.value === 'register' ? '注册失败，请检查输入信息' : '登录失败，请检查账号密码';
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

button.secondary {
  margin-top: 10px;
  background: transparent;
  border: 1px solid var(--line);
  color: var(--muted);
}

.error {
  color: var(--danger);
  margin-top: 12px;
}
</style>
