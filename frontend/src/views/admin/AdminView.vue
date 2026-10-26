<template>
  <BaseLayout>
    <section class="page">
      <header class="page-header">
        <div>
          <h1>知识库管理</h1>
          <p class="muted">创建、编辑和删除知识库。文件上传与索引将在后续版本完成。</p>
        </div>
        <button class="primary" type="button" @click="openCreate">新建知识库</button>
      </header>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="panel admin-panel">
        <table v-if="knowledgeBases.length" class="kb-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>描述</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="knowledgeBase in knowledgeBases" :key="knowledgeBase.id">
              <td>{{ knowledgeBase.name }}</td>
              <td>{{ knowledgeBase.description || '-' }}</td>
              <td>{{ knowledgeBase.status }}</td>
              <td>{{ formatTime(knowledgeBase.updatedAt) }}</td>
              <td class="actions">
                <button type="button" @click="openEdit(knowledgeBase)">编辑</button>
                <button type="button" class="danger" @click="confirmDelete(knowledgeBase)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="muted empty-state">
          {{ loading ? '正在加载知识库...' : '还没有知识库，点击“新建知识库”开始。' }}
        </p>
      </div>

      <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
        <div class="panel modal">
          <h2>{{ editingKnowledgeBase ? '编辑知识库' : '新建知识库' }}</h2>
          <p v-if="formErrorMessage" class="error">{{ formErrorMessage }}</p>
          <form @submit.prevent="submitForm">
            <label>
              名称
              <input v-model.trim="formState.name" maxlength="120" required />
            </label>
            <label>
              描述
              <textarea v-model="formState.description" maxlength="500" rows="4"></textarea>
            </label>
            <div class="modal-actions">
              <button type="button" @click="closeForm">取消</button>
              <button class="primary" type="submit" :disabled="saving">
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </BaseLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import BaseLayout from '../../layouts/BaseLayout.vue';
import {
  createKnowledgeBase,
  deleteKnowledgeBase,
  listKnowledgeBases,
  updateKnowledgeBase,
  type KnowledgeBase,
  type KnowledgeBaseInput
} from '../../api/knowledgeBases';

const router = useRouter();
const knowledgeBases = ref<KnowledgeBase[]>([]);
const loading = ref(false);
const saving = ref(false);
const showForm = ref(false);
const errorMessage = ref('');
const formErrorMessage = ref('');
const editingKnowledgeBase = ref<KnowledgeBase | null>(null);
const formState = reactive<KnowledgeBaseInput>({
  name: '',
  description: ''
});

onMounted(loadKnowledgeBases);

async function loadKnowledgeBases() {
  loading.value = true;
  errorMessage.value = '';

  try {
    knowledgeBases.value = await listKnowledgeBases();
  } catch (error) {
    if (!shouldRedirect(error)) {
      errorMessage.value = getErrorMessage(error, '知识库加载失败');
    }
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  editingKnowledgeBase.value = null;
  formState.name = '';
  formState.description = '';
  formErrorMessage.value = '';
  showForm.value = true;
}

function openEdit(knowledgeBase: KnowledgeBase) {
  editingKnowledgeBase.value = knowledgeBase;
  formState.name = knowledgeBase.name;
  formState.description = knowledgeBase.description || '';
  formErrorMessage.value = '';
  showForm.value = true;
}

function closeForm() {
  if (saving.value) {
    return;
  }
  showForm.value = false;
}

async function submitForm() {
  saving.value = true;
  formErrorMessage.value = '';

  try {
    if (editingKnowledgeBase.value) {
      const updated = await updateKnowledgeBase(editingKnowledgeBase.value.id, {
        name: formState.name,
        description: formState.description
      });
      knowledgeBases.value = knowledgeBases.value.map((item) => item.id === updated.id ? updated : item);
    } else {
      const created = await createKnowledgeBase({
        name: formState.name,
        description: formState.description
      });
      knowledgeBases.value = [created, ...knowledgeBases.value];
    }

    showForm.value = false;
  } catch (error) {
    if (!shouldRedirect(error)) {
      formErrorMessage.value = getErrorMessage(error, '知识库保存失败');
    }
  } finally {
    saving.value = false;
  }
}

async function confirmDelete(knowledgeBase: KnowledgeBase) {
  if (!window.confirm(`确定删除知识库“${knowledgeBase.name}”吗？`)) {
    return;
  }

  errorMessage.value = '';

  try {
    await deleteKnowledgeBase(knowledgeBase.id);
    knowledgeBases.value = knowledgeBases.value.filter((item) => item.id !== knowledgeBase.id);
  } catch (error) {
    if (!shouldRedirect(error)) {
      errorMessage.value = getErrorMessage(error, '知识库删除失败');
    }
  }
}

function shouldRedirect(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return false;
  }

  if (error.response?.status === 403) {
    router.replace('/chat');
    return true;
  }

  if (error.response?.status === 401) {
    router.replace('/login');
    return true;
  }

  return false;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;
    if (typeof message === 'string' && message) {
      return message;
    }
  }

  return fallback;
}

function formatTime(value: string) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ');
}
</script>

<style scoped>
.page {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;
}

.page-header h1,
.modal h2 {
  margin: 0 0 6px;
}

.admin-panel {
  padding: 0;
  overflow: hidden;
}

.kb-table {
  width: 100%;
  border-collapse: collapse;
}

.kb-table th,
.kb-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--border);
  vertical-align: top;
}

.kb-table th {
  color: var(--muted);
  font-weight: 600;
  font-size: 13px;
}

.kb-table tr:last-child td {
  border-bottom: 0;
}

.actions {
  display: flex;
  gap: 8px;
}

button.danger {
  color: var(--danger);
  border-color: #fecaca;
}

.empty-state {
  padding: 32px 16px;
  text-align: center;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal {
  width: 100%;
  max-width: 520px;
}

form label {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  color: var(--muted);
  font-size: 14px;
}

textarea {
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 9px 10px;
  width: 100%;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.error {
  color: var(--danger);
  margin-top: 12px;
}

@media (max-width: 760px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .kb-table {
    display: block;
    overflow-x: auto;
  }

  .kb-table th {
    position: sticky;
    top: 0;
    background: var(--panel);
  }
}
</style>
