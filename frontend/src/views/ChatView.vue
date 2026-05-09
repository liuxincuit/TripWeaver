<template>
  <div class="chat-page">
    <!-- Sidebar -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="sidebar-brand">
          <svg viewBox="0 0 40 40" class="brand-logo">
            <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5"/>
            <path d="M20 8 L24 20 L20 32 L16 20 Z" fill="currentColor" opacity="0.8"/>
            <circle cx="20" cy="20" r="2" fill="currentColor"/>
          </svg>
          <span class="brand-text font-display">TripWeaver</span>
        </router-link>
      </div>

      <div class="sidebar-content">
        <div class="journey-info">
          <div class="journey-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <div class="journey-text">
            <span class="journey-label">当前旅程</span>
            <span class="journey-title font-handwritten">新旅行计划</span>
          </div>
        </div>

        <div class="quick-tips">
          <h3 class="tips-title">快速开始</h3>
          <div class="tips-list">
            <button
              v-for="tip in quickTips"
              :key="tip"
              class="tip-item"
              @click="sendQuickTip(tip)"
            >
              {{ tip }}
            </button>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <button @click="goHome" class="btn btn-ghost btn-block">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
          返回首页
        </button>
      </div>
    </aside>

    <!-- Main Chat Area -->
    <main class="chat-main">
      <!-- Chat Header -->
      <header class="chat-header">
        <div class="header-info">
          <h1 class="header-title font-display">AI 旅行助手</h1>
          <span class="header-status">
            <span class="status-dot"></span>
            在线
          </span>
        </div>
        <div class="header-actions">
          <button class="btn btn-ghost" @click="clearChat" title="清空对话">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </header>

      <!-- Messages Area -->
      <div class="chat-messages" ref="messagesContainer" data-testid="message-list">
        <div class="messages-wrapper">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.role]"
            :data-testid="msg.role === 'assistant' ? 'assistant-message' : 'user-message'"
          >
            <div class="message-avatar">
              <template v-if="msg.role === 'assistant'">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M12 6v6l4 2"/>
                </svg>
              </template>
              <template v-else>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </template>
            </div>
            <div class="message-body">
              <div class="message-content" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="message assistant loading-message" data-testid="loading-indicator">
            <div class="message-avatar">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 6v6l4 2"/>
              </svg>
            </div>
            <div class="message-body">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            placeholder="描述你的旅行需求..."
            rows="1"
            ref="inputRef"
            class="chat-input"
            data-testid="chat-input"
          ></textarea>
          <button
            @click="sendMessage"
            :disabled="loading || !inputMessage.trim()"
            class="send-btn"
            title="发送消息"
            data-testid="send-button"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
        <p class="input-hint">按 Enter 发送，Shift + Enter 换行</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '../api/chat'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const planId = ref(null)
const messagesContainer = ref(null)
const inputRef = ref(null)

const quickTips = [
  '我想去日本旅行',
  '规划一个周末短途游',
  '推荐适合亲子游的目的地',
  '帮我设计蜜月旅行'
]

onMounted(async () => {
  const id = route.params.planId
  if (id === 'new') {
    const result = await chatApi.createNewPlan()
    planId.value = result.planId
    messages.value = [{
      role: 'assistant',
      content: '你好！我是 TripWeaver 旅行规划助手。🌍\n\n请告诉我你的旅行想法：想去哪里？什么时候出发？有什么特别的偏好？我会为你量身定制一份完美的旅行计划。'
    }]
  } else {
    planId.value = parseInt(id)
    try {
      const history = await chatApi.getHistory(planId.value)
      if (history && history.messages) {
        messages.value = JSON.parse(history.messages)
      }
    } catch (e) {
      console.error('Failed to load history:', e)
    }
  }
})

// Auto-resize textarea
watch(inputMessage, () => {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
    inputRef.value.style.height = Math.min(inputRef.value.scrollHeight, 150) + 'px'
  }
})

async function sendMessage() {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }

  try {
    const result = await chatApi.sendMessage(planId.value, userMessage)
    messages.value.push({ role: 'assistant', content: result.response })
  } catch (e) {
    messages.value.push({
      role: 'assistant',
      content: '抱歉，出现了一些问题，请稍后重试。如果问题持续存在，请检查网络连接。'
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

function sendQuickTip(tip) {
  inputMessage.value = tip
  sendMessage()
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function formatMessage(content) {
  return marked.parse(content)
}

function goHome() {
  router.push('/')
}

function clearChat() {
  if (confirm('确定要清空当前对话吗？')) {
    messages.value = [{
      role: 'assistant',
      content: '对话已清空。有什么新的旅行想法想要讨论吗？'
    }]
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100vh;
  background: var(--color-background);
}

/* Sidebar */
.chat-sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border-light);
}

.sidebar-header {
  padding: var(--space-lg);
  border-bottom: 1px solid var(--color-border-light);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  text-decoration: none;
}

.brand-logo {
  width: 36px;
  height: 36px;
  color: var(--color-primary);
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.sidebar-content {
  flex: 1;
  padding: var(--space-lg);
  overflow-y: auto;
}

.journey-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--color-parchment);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-xl);
}

.journey-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  border-radius: 50%;
  color: white;
}

.journey-icon svg {
  width: 20px;
  height: 20px;
}

.journey-text {
  display: flex;
  flex-direction: column;
}

.journey-label {
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.journey-title {
  font-size: 1.1rem;
  color: var(--color-text);
}

.quick-tips {
  margin-bottom: var(--space-xl);
}

.tips-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-md);
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.tip-item {
  padding: var(--space-sm) var(--space-md);
  background: transparent;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  font-size: 0.9rem;
  color: var(--color-text);
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tip-item:hover {
  background: var(--color-parchment);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.sidebar-footer {
  padding: var(--space-lg);
  border-top: 1px solid var(--color-border-light);
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Chat Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-lg) var(--space-xl);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-light);
}

.header-info {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.header-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.header-status {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.status-dot {
  width: 8px;
  height: 8px;
  background: var(--color-sage);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

/* Messages Area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xl);
}

.messages-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.message {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
  animation: fadeInUp 0.3s ease forwards;
}

.message-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.message.user .message-avatar {
  background: var(--color-accent);
  color: white;
}

.message.assistant .message-avatar {
  background: var(--color-primary);
  color: white;
}

.message-avatar svg {
  width: 18px;
  height: 18px;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-content {
  padding: var(--space-md) var(--space-lg);
  border-radius: var(--radius-lg);
  line-height: 1.7;
}

.message.user .message-content {
  background: var(--color-accent);
  color: white;
  border-bottom-right-radius: var(--radius-sm);
}

.message.assistant .message-content {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-bottom-left-radius: var(--radius-sm);
}

.message-content :deep(p) {
  margin: 0.5rem 0;
}

.message-content :deep(p:first-child) {
  margin-top: 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3) {
  margin: 1rem 0 0.5rem;
  font-family: var(--font-display);
}

.message-content :deep(ul),
.message-content :deep(ol) {
  padding-left: 1.5rem;
  margin: 0.5rem 0;
}

.message-content :deep(code) {
  background: rgba(0,0,0,0.05);
  padding: 0.1rem 0.3rem;
  border-radius: 3px;
  font-size: 0.9em;
}

.message.user .message-content :deep(code) {
  background: rgba(255,255,255,0.2);
}

/* Typing Indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: var(--space-md);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--color-text-light);
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

/* Input Area */
.chat-input-area {
  padding: var(--space-lg) var(--space-xl);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
}

.input-wrapper {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  align-items: flex-end;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--color-parchment);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(74, 124, 124, 0.1);
}

.chat-input {
  flex: 1;
  border: none;
  background: transparent;
  font-family: var(--font-body);
  font-size: 1rem;
  line-height: 1.5;
  color: var(--color-text);
  resize: none;
  outline: none;
  min-height: 24px;
  max-height: 150px;
}

.chat-input::placeholder {
  color: var(--color-text-light);
}

.send-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: scale(1.05);
}

.send-btn:disabled {
  background: var(--color-border);
  cursor: not-allowed;
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

.input-hint {
  max-width: 800px;
  margin: var(--space-sm) auto 0;
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-align: center;
}

/* Responsive */
@media (max-width: 768px) {
  .chat-sidebar {
    display: none;
  }

  .chat-header {
    padding: var(--space-md);
  }

  .chat-messages {
    padding: var(--space-md);
  }

  .chat-input-area {
    padding: var(--space-md);
  }
}
</style>