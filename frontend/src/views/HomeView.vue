<template>
  <div class="home">
    <h1>欢迎使用 TripWeaver</h1>
    <p>AI 智能旅行规划助手</p>
    <button @click="startNewPlan">开始规划新旅程</button>
    <button @click="viewPlans">查看我的计划</button>
    <button @click="logout">退出登录</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

function startNewPlan() {
  router.push('/chat/new')
}

function viewPlans() {
  router.push('/plans')
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}
h1 {
  color: #1890ff;
  margin-bottom: 1rem;
}
p {
  color: #666;
  margin-bottom: 2rem;
}
button {
  margin: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background: #40a9ff;
}
</style>