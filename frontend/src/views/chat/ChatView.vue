<template>
  <BaseLayout>
    <section class="chat-page">
      <aside class="session-sidebar panel">
        <div class="sidebar-header">
          <h2>会话</h2>
          <button class="primary" type="button" :disabled="busy" @click="createSession">新建</button>
        </div>

        <p v-if="error" class="error">{{ error }}</p>

        <div v-if="sessions.length === 0" class="empty">暂无会话，点击右上角新建。</div>

        <div v-else class="session-list">
          <button
            v-for="session in sessions"
            :key="session.id"
            type="button"
            class="session-item"
            :class="{ active: session.id === currentSession?.id }"
            :disabled="busy"
            @click="selectSession(session.id)"
          >
            <span class="session-title">{{ session.title }}</span>
            <span class="session-delete" title="删除会话" @click.stop="removeSession(session.id)">删除</span>
          </button>
        </div>
      </aside>

      <main class="chat-main panel">
        <div v-if="!currentSession" class="empty centered">选择或新建会话后开始聊天。</div>

        <template v-else>
          <div class="chat-header">
            <h2>{{ currentSession.title }}</h2>
            <p class="muted">当前为持久化消息流程，AI 回答将在后续版本接入。</p>
          </div>

          <div ref="messagesRef" class="messages">
            <div v-if="messages.length === 0" class="empty">输入内容，发送第一条消息。</div>

            <article
              v-for="message in messages"
              :key="message.id"
              class="message"
              :class="message.role"
            >
              <span class="role">{{ message.role === 'user' ? '你' : '助手' }}</span>
              <p>{{ message.content }}</p>
            </article>
          </div>

          <form class="composer" @submit.prevent="sendMessage">
            <textarea
              v-model="draft"
              rows="3"
              placeholder="输入聊天内容"
              :disabled="busy"
            ></textarea>
            <button class="primary" type="submit" :disabled="busy || !canSend">发送</button>
          </form>
        </template>
      </main>
    </section>
  </BaseLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue';
import BaseLayout from '../../layouts/BaseLayout.vue';
import {
  createChatMessage,
  createChatSession,
  deleteChatSession,
  fetchChatMessages,
  fetchChatSessions
} from '../../api/chat';
import type { ChatMessage, ChatSession } from '../../api/chat';

const sessions = ref<ChatSession[]>([]);
const currentSession = ref<ChatSession | null>(null);
const messages = ref<ChatMessage[]>([]);
const draft = ref('');
const error = ref('');
const busy = ref(false);
const messagesRef = ref<HTMLElement | null>(null);

const canSend = computed(() => draft.value.trim().length > 0 && Boolean(currentSession.value));

onMounted(loadSessions);

async function loadSessions() {
  busy.value = true;
  error.value = '';
  try {
    sessions.value = await fetchChatSessions();
    if (!currentSession.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0].id);
      return;
    }
    await loadMessages();
  } catch (nextError) {
    error.value = '加载会话失败';
    console.error(nextError);
  } finally {
    busy.value = false;
  }
}

async function createSession() {
  busy.value = true;
  error.value = '';
  try {
    const session = await createChatSession();
    sessions.value = [session, ...sessions.value];
    currentSession.value = session;
    await loadMessages();
  } catch (nextError) {
    error.value = '创建会话失败';
    console.error(nextError);
  } finally {
    busy.value = false;
  }
}

async function selectSession(sessionId: string) {
  const session = sessions.value.find((item) => item.id === sessionId);
  if (!session) return;

  busy.value = true;
  error.value = '';
  try {
    currentSession.value = session;
    await loadMessages();
  } catch (nextError) {
    error.value = '加载消息失败';
    console.error(nextError);
  } finally {
    busy.value = false;
  }
}

async function removeSession(sessionId: string) {
  busy.value = true;
  error.value = '';
  try {
    await deleteChatSession(sessionId);
    sessions.value = sessions.value.filter((session) => session.id !== sessionId);
    if (currentSession.value?.id === sessionId) {
      currentSession.value = sessions.value[0] ?? null;
      if (currentSession.value) {
        await loadMessages();
      } else {
        messages.value = [];
      }
    }
  } catch (nextError) {
    error.value = '删除会话失败';
    console.error(nextError);
  } finally {
    busy.value = false;
  }
}

async function loadMessages() {
  if (!currentSession.value) {
    messages.value = [];
    return;
  }

  messages.value = await fetchChatMessages(currentSession.value.id);
  await scrollMessagesToBottom();
}

async function sendMessage() {
  const content = draft.value.trim();
  if (!currentSession.value || !content || busy.value) return;

  busy.value = true;
  error.value = '';
  draft.value = '';

  try {
    const userMessage = await createChatMessage(currentSession.value.id, 'user', content);
    messages.value = [...messages.value, userMessage];
    await loadSessions();

    const assistantMessage = await createChatMessage(
      currentSession.value.id,
      'assistant',
      '我已记录你的消息。AI 回答将在后续版本接入。'
    );
    messages.value = [...messages.value, assistantMessage];
    await loadSessions();
    await scrollMessagesToBottom();
  } catch (nextError) {
    error.value = '发送消息失败';
    console.error(nextError);
  } finally {
    busy.value = false;
  }
}

async function scrollMessagesToBottom() {
  await nextTick();
  messagesRef.value?.scrollIntoView({ block: 'end' });
}
</script>

<style scoped>
 .chat-page {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  height: calc(100vh - 112px);
}

 .session-sidebar,
 .chat-main {
  display: flex;
  flex-direction: column;
  min-height: 0;
 }

 .sidebar-header,
 .chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
 }

 .sidebar-header h2,
 .chat-header h2 {
  margin: 0;
  font-size: 18px;
 }

 .chat-header p {
  margin: 4px 0 0;
  font-size: 12px;
  text-align: right;
 }

 .session-list,
 .messages {
  flex: 1;
  overflow-y: auto;
  padding-top: 14px;
 }

 .session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  margin-bottom: 8px;
  text-align: left;
  background: #f8fafc;
 }

 .session-item.active {
  background: #dbeafe;
  border-color: #93c5fd;
 }

 .session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
 }

 .session-delete {
  flex: none;
  padding: 2px 6px;
  border: none;
  border-radius: 4px;
  color: var(--danger);
  background: transparent;
  font-size: 12px;
 }

 .messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
 }

 .message {
  max-width: 76%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
 }

 .message.user {
  align-self: flex-end;
  background: #eff6ff;
  border-color: #bfdbfe;
 }

 .message .role {
  display: block;
  margin-bottom: 4px;
  color: var(--muted);
  font-size: 12px;
 }

 .message p {
  margin: 0;
  white-space: pre-wrap;
 }

 .composer {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
  margin-top: 12px;
 }

 textarea {
  resize: vertical;
  border: 1px solid var(--border);
  border-radius: 6px;
  padding: 10px;
 }

 .error {
  color: var(--danger);
  font-size: 13px;
  margin: 12px 0 0;
 }

 .empty {
  color: var(--muted);
  padding: 18px 0;
  line-height: 1.6;
 }

 .empty.centered {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
 }

 @media (max-width: 860px) {
  .chat-page {
    grid-template-columns: 1fr;
    height: auto;
  }

  .session-sidebar,
  .chat-main {
    min-height: 420px;
  }

  .composer {
    grid-template-columns: 1fr;
  }
 }
</style>
