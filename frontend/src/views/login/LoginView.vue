<template>
  <div class="login-page">
    <form class="panel login-card" @submit.prevent="submit">
      <h1>{{ title }}</h1>
      <p class="muted">{{ hint }}</p>

      <div class="segmented" role="tablist" aria-label="登录方式">
        <button
          type="button"
          role="tab"
          :aria-selected="mode === 'login'"
          :class="{ active: mode === 'login' }"
          :disabled="loading"
          @click="selectMode('login')"
        >
          登录
        </button>
        <button
          type="button"
          role="tab"
          :aria-selected="mode === 'register'"
          :class="{ active: mode === 'register' }"
          :disabled="loading"
          @click="selectMode('register')"
        >
          注册
        </button>
      </div>

      <label>
        用户名
        <input v-model.trim="username" autocomplete="username" :disabled="mode === 'register'" />
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
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login, register } from '../../api/auth';
import { useAuthStore } from '../../stores/auth';
import axios from 'axios';

type LoginMode = 'login' | 'register';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const displayName = ref('');
const confirmPassword = ref('');
const mode = ref<LoginMode>('login');
const loading = ref(false);
const errorMessage = ref('');
const title = ref('登录 Rag-AiChat');
const hint = ref('登录成功后自动进入对应页面。');
const submitLabel = ref('登录');
const loadingText = ref('登录中...');

function selectMode(nextMode: LoginMode) {
  if (mode.value === nextMode) {
    return;
  }

  mode.value = nextMode;
  errorMessage.value = '';

  if (mode.value === 'register') {
    username.value = '';
    password.value = '';
    displayName.value = '';
    confirmPassword.value = '';
    title.value = '注册账号';
    hint.value = '注册账号只能登录聊天页面。';
    submitLabel.value = '注册';
    loadingText.value = '注册中...';
  } else {
    title.value = '登录 Rag-AiChat';
    hint.value = '登录成功后自动进入对应页面。';
    submitLabel.value = '登录';
    loadingText.value = '登录中...';
  }
}

async function submit() {
  if (!username.value || !password.value) {
    errorMessage.value = mode.value === 'register' ? '请输入用户名、密码、昵称和确认密码' : '请输入用户名和密码';
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

.segmented {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 6px;
  margin-top: 18px;
  padding: 4px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
}

.segmented button {
  padding: 8px 4px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--muted);
  font-size: 13px;
}

.segmented button.active {
  background: var(--accent);
  color: #fff;
}

form label {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  color: var(--muted);
  font-size: 14px;
}

button.primary,
button.secondary {
  width: 100%;
  margin-top: 18px;
}

button.secondary {
  background: transparent;
  border: 1px solid var(--line);
  color: var(--muted);
}

.error {
  color: var(--danger);
  margin-top: 12px;
}
</style>