<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>TripWeaver</h1>
      <button @click="goHome">返回首页</button>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index"
           :class="['message', msg.role]">
        <div class="message-content" v-html="formatMessage(msg.content)"></div>
      </div>
      <div v-if="loading" class="message assistant">
        <div class="message-content">正在思考中...</div>
      </div>
    </div>

    <div class="chat-input">
      <textarea
        v-model="inputMessage"
        @keydown.enter.exact.prevent="sendMessage"
        placeholder="描述你的旅行需求..."
        rows="3"
      ></textarea>
      <button @click="sendMessage" :disabled="loading || !inputMessage.trim()">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
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

onMounted(async () => {
  const id = route.params.planId
  if (id === 'new') {
    const result = await chatApi.createNewPlan()
    planId.value = result.planId
    messages.value = [{ role: 'assistant', content: '你好！我是 TripWeaver 旅行规划助手。请告诉我你的旅行需求，我会为你规划一次完美的旅行。' }]
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

async function sendMessage() {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  loading.value = true

  await nextTick()
  scrollToBottom()

  try {
    const result = await chatApi.sendMessage(planId.value, userMessage)
    messages.value.push({ role: 'assistant', content: result.response })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，出现了一些问题，请稍后重试。' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
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
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: white;
  border-bottom: 1px solid #eee;
}

.chat-header h1 {
  color: #1890ff;
  margin: 0;
}

.chat-header button {
  padding: 0.5rem 1rem;
  background: #f0f0f0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.message {
  margin-bottom: 1rem;
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.user .message-content {
  background: #1890ff;
  color: white;
}

.message.assistant .message-content {
  background: white;
  color: #333;
}

.message-content {
  max-width: 70%;
  padding: 1rem;
  border-radius: 12px;
  line-height: 1.6;
}

.message-content :deep(p) {
  margin: 0.5rem 0;
}

.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3) {
  margin: 1rem 0 0.5rem;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  padding-left: 1.5rem;
}

.chat-input {
  display: flex;
  padding: 1rem;
  background: white;
  border-top: 1px solid #eee;
}

.chat-input textarea {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  font-size: 1rem;
}

.chat-input button {
  margin-left: 0.5rem;
  padding: 0 1.5rem;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.chat-input button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>