   <template>
  <BaseLayout>
    <section class="page">
      <header class="page-header">
        <div>
          <h1>管理后台</h1>
          <p class="muted">知识库、文件索引、用户管理和系统状态。</p>
        </div>
      </header>

      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="admin-tabs" role="tablist">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          role="tab"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
        </button>
      </div>

      <section v-show="activeTab === 'kb'" class="panel admin-panel">
        <div class="panel-head">
          <h2>知识库</h2>
          <button class="primary" type="button" @click="openCreate">新建知识库</button>
        </div>
        <table v-if="knowledgeBases.length" class="admin-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>描述</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="kb in knowledgeBases" :key="kb.id">
              <td>{{ kb.name }}</td>
              <td>{{ kb.description || '-' }}</td>
              <td>{{ kb.status }}</td>
              <td class="actions">
                <button type="button" @click="openEdit(kb)">编辑</button>
                <button type="button" class="danger" @click="confirmDelete(kb)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty-state">{{ loading ? '加载中...' : '还没有知识库。' }}</p>
      </section>

      <section v-show="activeTab === 'files'" class="panel admin-panel">
        <div class="panel-head">
          <h2>文件索引</h2>
        </div>
        <div class="file-toolbar">
          <label>
            知识库
<select v-model="fileKnowledgeBaseId" :disabled="fileBusy" @change="loadFiles">
              <option v-if="fileKnowledgeBaseId === ''" value="">请选择</option>
              <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">{{ kb.name }}</option>
            </select>
          </label>
          <input type="file" :disabled="!fileKnowledgeBaseId || fileBusy" @change="handleFile" />
          <button type="button" :disabled="!fileKnowledgeBaseId || fileBusy" @click="loadKnowledgeBasesAndFiles">
            {{ fileBusy ? '加载中...' : '刷新' }}
          </button>
        </div>
        <p v-if="fileErrorMessage" class="error">{{ fileErrorMessage }}</p>
        <table v-if="files.length" class="admin-table">
          <thead>
            <tr>
              <th>文件</th>
              <th>状态</th>
              <th>Chunk</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="file in files" :key="file.id">
              <td>
                <div>{{ file.fileName }}</div>
                <small v-if="file.parseError">{{ file.parseError }}</small>
              </td>
              <td>{{ file.status }}</td>
              <td>{{ file.chunkCount }}</td>
              <td>{{ formatTime(file.updatedAt) }}</td>
              <td class="actions">
                <button type="button" :disabled="fileBusy" @click="reindexFile(file)">重新索引</button>
                <button type="button" class="danger" :disabled="fileBusy" @click="deleteFile(file)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else-if="fileBusy" class="empty-state">加载中...</p>
        <p v-else-if="!fileKnowledgeBaseId" class="empty-state">请选择知识库并上传文件。</p>
        <p v-else class="empty-state">当前知识库暂无文件，请先上传文档。</p>
      </section>

      <section v-show="activeTab === 'users'" class="panel admin-panel">
        <div class="panel-head">
          <h2>用户管理</h2>
          <button type="button" :disabled="busy" @click="loadUsers">刷新</button>
        </div>
        <p v-if="userErrorMessage" class="error">{{ userErrorMessage }}</p>
        <table v-if="users.length" class="admin-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>昵称</th>
              <th>角色</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.username }}</td>
              <td>{{ user.displayName || '-' }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.status }}</td>
              <td class="actions">
                <select v-model="user.role" :disabled="busy" @change="saveUser(user)">
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
                <select v-model="user.status" :disabled="busy" @change="saveUser(user)">
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="DISABLED">DISABLED</option>
                </select>
                <button type="button" :disabled="busy" @click="resetPassword(user)">重置密码</button>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty-state">{{ busy ? '加载中...' : '暂无用户。' }}</p>
      </section>

      <section v-show="activeTab === 'status'" class="panel admin-panel">
        <div class="panel-head">
          <h2>系统状态</h2>
          <button type="button" :disabled="busy" @click="loadStatus">刷新</button>
        </div>
        <div v-if="status" class="status-grid">
          <div class="status-card">
            <strong>{{ status.appVersion }}</strong>
            <span>{{ formatTime(status.timestamp) }}</span>
          </div>
        </div>
        <div class="status-grid">
          <div v-for="component in status?.components || []" :key="component.name" class="status-card">
            <strong>{{ component.name }}</strong>
            <span :class="['status-value', component.status === 'UP' ? 'ok' : 'bad']">{{ component.status }}</span>
            <p>{{ component.detail || '-' }}</p>
          </div>
        </div>
      </section>

      <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
        <div class="panel modal">
          <h2>{{ editingKnowledgeBase ? '编辑知识库' : '新建知识库' }}</h2>
          <p v-if="formErrorMessage" class="error">{{ formErrorMessage }}</p>
          <form @submit.preventDefault="submitForm">
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
              <button class="primary" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </BaseLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import BaseLayout from '../../layouts/BaseLayout.vue';
import {
  createKnowledgeBase,
  deleteKnowledgeBase,
  deleteKnowledgeBaseFile,
  listKnowledgeBaseFiles,
  listKnowledgeBases,
  reindexKnowledgeBaseFile,
  updateKnowledgeBase,
  uploadKnowledgeBaseFile,
  type DocumentFile,
  type KnowledgeBase,
  type KnowledgeBaseInput
} from '../../api/knowledgeBases';
import {
  listAdminUsers,
  updateAdminUser,
  type AdminUser,
  type UpdateUserPayload
} from '../../api/admin';
import { getSystemStatus, type SystemStatus } from '../../api/status';

type AdminTab = 'kb' | 'files' | 'users' | 'status';

const router = useRouter();
const tabs = [
  { key: 'kb' as AdminTab, label: '知识库' },
  { key: 'files' as AdminTab, label: '文件索引' },
  { key: 'users' as AdminTab, label: '用户管理' },
  { key: 'status' as AdminTab, label: '系统状态' }
];

const activeTab = ref<AdminTab>('files');
const knowledgeBases = ref<KnowledgeBase[]>([]);
const files = ref<DocumentFile[]>([]);
const users = ref<AdminUser[]>([]);
const status = ref<SystemStatus | null>(null);
const fileKnowledgeBaseId = ref('');
const loading = ref(false);
const saving = ref(false);
const busy = ref(false);
const fileBusy = ref(false);
const showForm = ref(false);
const errorMessage = ref('');
const fileErrorMessage = ref('');
const userErrorMessage = ref('');
const formErrorMessage = ref('');
const editingKnowledgeBase = ref<KnowledgeBase | null>(null);
const formState = reactive<KnowledgeBaseInput>({
  name: '',
  description: ''
});

onMounted(async () => {
  await loadKnowledgeBases();
  if (!fileKnowledgeBaseId.value && knowledgeBases.value.length > 0) {
    fileKnowledgeBaseId.value = knowledgeBases.value[0].id;
  }
  await Promise.all([loadUsers(), loadStatus()]);
  await loadFiles();
});

watch(activeTab, async (tab) => {
  if (tab === 'files') {
    await loadFiles();
  }
  if (tab === 'users') {
    await loadUsers();
  } else if (tab === 'status') {
    await loadStatus();
  }
});

watch(fileKnowledgeBaseId, async () => {
  if (activeTab.value === 'files') {
    await loadFiles();
  }
});

async function loadKnowledgeBases() {
  loading.value = true;
  errorMessage.value = '';
  try {
    knowledgeBases.value = await listKnowledgeBases();
  } catch (error) {
    if (!redirectIfBlocked(error)) {
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
  if (!saving.value) {
    showForm.value = false;
  }
}

async function loadKnowledgeBasesAndFiles() {
  await loadKnowledgeBases();
  if (!fileKnowledgeBaseId.value && knowledgeBases.value.length > 0) {
    fileKnowledgeBaseId.value = knowledgeBases.value[0].id;
  }
  await loadFiles();
}

async function submitForm() {
  saving.value = true;
  formErrorMessage.value = '';
  try {
    const payload = { name: formState.name, description: formState.description };
    if (editingKnowledgeBase.value) {
      const updated = await updateKnowledgeBase(editingKnowledgeBase.value.id, payload);
      knowledgeBases.value = knowledgeBases.value.map((item) => (item.id === updated.id ? updated : item));
    } else {
      const created = await createKnowledgeBase(payload);
      knowledgeBases.value = [created, ...knowledgeBases.value];
      fileKnowledgeBaseId.value = created.id;
      activeTab.value = 'files';
    }
    showForm.value = false;
  } catch (error) {
    formErrorMessage.value = getErrorMessage(error, '知识库保存失败');
  } finally {
    saving.value = false;
  }
}

async function confirmDelete(knowledgeBase: KnowledgeBase) {
  if (!window.confirm(`确定删除知识库“${knowledgeBase.name}”吗？`)) {
    return;
  }
  try {
    await deleteKnowledgeBase(knowledgeBase.id);
    knowledgeBases.value = knowledgeBases.value.filter((item) => item.id !== knowledgeBase.id);
    if (fileKnowledgeBaseId.value === knowledgeBase.id) {
      fileKnowledgeBaseId.value = '';
      files.value = [];
      fileErrorMessage.value = '';
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '知识库删除失败');
  }
}

async function loadFiles() {
  if (!fileKnowledgeBaseId.value) {
    fileErrorMessage.value = '';
    files.value = [];
    return;
  }
  fileBusy.value = true;
  fileErrorMessage.value = '';
  try {
    files.value = await listKnowledgeBaseFiles(fileKnowledgeBaseId.value);
  } catch (error) {
    fileErrorMessage.value = getErrorMessage(error, '文件列表加载失败');
  } finally {
    fileBusy.value = false;
  }
}

async function handleFile(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file || !fileKnowledgeBaseId.value) {
    return;
  }

  fileBusy.value = true;
  fileErrorMessage.value = '';
  try {
    const uploaded = await uploadKnowledgeBaseFile(fileKnowledgeBaseId.value, file);
    await loadFiles();
    fileErrorMessage.value = uploaded.status === 'INDEXED' ? '' : uploaded.parseError || uploaded.status;
  } catch (error) {
    fileErrorMessage.value = getErrorMessage(error, '文件上传或索引失败');
  } finally {
    fileBusy.value = false;
  }
}

async function reindexFile(file: DocumentFile) {
  if (!fileKnowledgeBaseId.value) {
    return;
  }
  fileBusy.value = true;
  try {
    await reindexKnowledgeBaseFile(fileKnowledgeBaseId.value, file.id);
    await loadFiles();
  } catch (error) {
    fileErrorMessage.value = getErrorMessage(error, '重新索引失败');
  } finally {
    fileBusy.value = false;
  }
}

async function deleteFile(file: DocumentFile) {
  if (!fileKnowledgeBaseId.value || !window.confirm(`确定删除文件“${file.fileName}”吗？`)) {
    return;
  }
  fileBusy.value = true;
  try {
    await deleteKnowledgeBaseFile(fileKnowledgeBaseId.value, file.id);
    files.value = files.value.filter((item) => item.id !== file.id);
  } catch (error) {
    fileErrorMessage.value = getErrorMessage(error, '文件删除失败');
  } finally {
    fileBusy.value = false;
  }
}

async function loadUsers() {
  busy.value = true;
  userErrorMessage.value = '';
  try {
    users.value = await listAdminUsers();
  } catch (error) {
    if (!redirectIfBlocked(error)) {
      userErrorMessage.value = getErrorMessage(error, '用户列表加载失败');
    }
  } finally {
    busy.value = false;
  }
}

async function saveUser(user: AdminUser) {
  busy.value = true;
  userErrorMessage.value = '';
  try {
    const payload: UpdateUserPayload = {
      role: user.role,
      status: user.status
    };
    await updateAdminUser(user.id, payload);
    users.value = users.value.map((item) => (item.id === user.id ? { ...item, ...payload } : item));
  } catch (error) {
    userErrorMessage.value = getErrorMessage(error, '用户更新失败');
    await loadUsers();
  } finally {
    busy.value = false;
  }
}

async function resetPassword(user: AdminUser) {
  const password = window.prompt(`为 ${user.username} 输入新密码，至少 8 位：`);
  if (!password) {
    return;
  }
  if (password.length < 8) {
    userErrorMessage.value = '密码至少 8 位';
    return;
  }

  busy.value = true;
  try {
    await updateAdminUser(user.id, { password });
  } catch (error) {
    userErrorMessage.value = getErrorMessage(error, '密码重置失败');
  } finally {
    busy.value = false;
  }
}

async function loadStatus() {
  busy.value = true;
  try {
    status.value = await getSystemStatus();
  } catch (error) {
    if (!redirectIfBlocked(error)) {
      errorMessage.value = getErrorMessage(error, '系统状态加载失败');
    }
  } finally {
    busy.value = false;
  }
}

function switchTab(tab: AdminTab) {
  activeTab.value = tab;
}

function redirectIfBlocked(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return false;
  }
  if (error.response?.status === 403) {
    router.replace('/chat');
  }
  if (error.response?.status === 401) {
    router.replace('/login');
  }
  return error.response?.status === 401 || error.response?.status === 403;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === 'string' && error.response.data.message) {
    return error.response.data.message;
  }
  return fallback;
}

function formatTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-';
}
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.admin-tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-bottom: 14px;
}

.admin-tabs button {
  padding: 9px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
}

.admin-tabs button.active {
  background: #dbeafe;
  border-color: #93c5fd;
}

.admin-panel {
  padding: 0;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 10px 14px;
  text-align: left;
  border-bottom: 1px solid var(--border);
  vertical-align: top;
}

.admin-table th {
  color: var(--muted);
  font-size: 13px;
}

.actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.file-toolbar {
  display: grid;
  grid-template-columns: 1.2fr 1fr auto;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}

.file-toolbar label {
  display: grid;
  gap: 6px;
  color: var(--muted);
  font-size: 13px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  padding: 16px;
}

.status-card {
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
}

.status-card p {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.status-value.ok {
  color: var(--accent);
}

.status-value.bad {
  color: var(--danger);
}

.empty-state {
  padding: 32px 16px;
  text-align: center;
  color: var(--muted);
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
  width: 100%;
  resize: vertical;
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 9px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.error {
  color: var(--danger);
  margin-top: 10px;
}

@media (max-width: 760px) {
  .admin-tabs {
    grid-template-columns: 1fr 1fr;
  }

  .file-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
