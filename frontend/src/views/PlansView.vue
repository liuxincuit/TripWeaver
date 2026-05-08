<template>
  <div class="plans-page paper-texture">
    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <router-link to="/" class="back-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回首页
        </router-link>
        <div class="header-title-section">
          <h1 class="page-title font-display">我的旅行计划</h1>
          <p class="page-subtitle font-handwritten">记录每一次精彩的旅程</p>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="page-main">
      <div class="plans-container">
        <!-- New Plan Card -->
        <div class="new-plan-card" @click="createNewPlan">
          <div class="new-plan-icon">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="24" cy="24" r="20"/>
              <path d="M24 14v20M14 24h20"/>
            </svg>
          </div>
          <h3 class="new-plan-title font-display">创建新计划</h3>
          <p class="new-plan-desc">开始规划你的下一次冒险</p>
        </div>

        <!-- Plans Grid -->
        <div v-if="plans.length > 0" class="plans-grid">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card card"
            @click="viewPlan(plan.id)"
          >
            <div class="plan-card-header">
              <div class="plan-status" :class="plan.status">
                {{ getStatusText(plan.status) }}
              </div>
              <button class="plan-menu-btn" @click.stop="showMenu($event, plan)">
                <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
                  <circle cx="12" cy="5" r="2"/>
                  <circle cx="12" cy="12" r="2"/>
                  <circle cx="12" cy="19" r="2"/>
                </svg>
              </button>
            </div>

            <div class="plan-card-body">
              <h3 class="plan-title font-display">{{ plan.title || '未命名旅行' }}</h3>
              <p class="plan-destination">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                {{ plan.destination || '目的地待定' }}
              </p>
              <div class="plan-dates" v-if="plan.startDate">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                {{ formatDate(plan.startDate) }}
                <template v-if="plan.endDate">
                  - {{ formatDate(plan.endDate) }}
                </template>
              </div>
            </div>

            <div class="plan-card-footer">
              <span class="plan-updated">
                更新于 {{ formatRelativeTime(plan.updatedAt) }}
              </span>
              <span class="plan-action">
                查看详情
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </span>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else-if="!loading" class="empty-state">
          <div class="empty-illustration">
            <svg viewBox="0 0 200 200" fill="none" stroke="currentColor" stroke-width="1">
              <circle cx="100" cy="100" r="80" opacity="0.3"/>
              <path d="M100 40 L100 160" opacity="0.3"/>
              <path d="M40 100 L160 100" opacity="0.3"/>
              <path d="M100 60 L115 100 L100 140 L85 100 Z" fill="currentColor" opacity="0.2"/>
              <circle cx="100" cy="100" r="8" fill="currentColor" opacity="0.3"/>
            </svg>
          </div>
          <h3 class="empty-title font-display">还没有旅行计划</h3>
          <p class="empty-desc">点击上方的"创建新计划"开始你的第一次旅程规划</p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
      </div>
    </main>

    <!-- Context Menu -->
    <div v-if="contextMenu.show" class="context-menu" :style="contextMenu.style" @click.stop>
      <button class="menu-item" @click="editPlan(contextMenu.plan)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        编辑
      </button>
      <button class="menu-item" @click="continueChat(contextMenu.plan)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        继续对话
      </button>
      <button class="menu-item danger" @click="deletePlan(contextMenu.plan)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <polyline points="3 6 5 6 21 6"/>
          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
        </svg>
        删除
      </button>
    </div>

    <!-- Overlay -->
    <div v-if="contextMenu.show" class="menu-overlay" @click="hideMenu"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { chatApi } from '../api/chat'

const router = useRouter()

const plans = ref([])
const loading = ref(false)
const contextMenu = ref({
  show: false,
  plan: null,
  style: {}
})

onMounted(async () => {
  await loadPlans()
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})

async function loadPlans() {
  loading.value = true
  try {
    const result = await chatApi.getPlans()
    plans.value = result || []
  } catch (e) {
    console.error('Failed to load plans:', e)
  } finally {
    loading.value = false
  }
}

function createNewPlan() {
  router.push('/chat/new')
}

function viewPlan(id) {
  router.push(`/plan/${id}`)
}

function showMenu(event, plan) {
  const rect = event.target.getBoundingClientRect()
  contextMenu.value = {
    show: true,
    plan: plan,
    style: {
      top: `${rect.bottom + 8}px`,
      right: `${window.innerWidth - rect.right}px`
    }
  }
}

function hideMenu() {
  contextMenu.value.show = false
  contextMenu.value.plan = null
}

function handleEscape(e) {
  if (e.key === 'Escape') {
    hideMenu()
  }
}

function editPlan(plan) {
  hideMenu()
  router.push(`/plan/${plan.id}`)
}

function continueChat(plan) {
  hideMenu()
  router.push(`/chat/${plan.id}`)
}

async function deletePlan(plan) {
  if (confirm(`确定要删除"${plan.title || '未命名旅行'}"吗？此操作不可撤销。`)) {
    try {
      await chatApi.deletePlan(plan.id)
      plans.value = plans.value.filter(p => p.id !== plan.id)
    } catch (e) {
      console.error('Failed to delete plan:', e)
      alert('删除失败，请稍后重试')
    }
  }
  hideMenu()
}

function getStatusText(status) {
  const statusMap = {
    draft: '草稿',
    planning: '规划中',
    confirmed: '已确认',
    completed: '已完成'
  }
  return statusMap[status] || '草稿'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.plans-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Header */
.page-header {
  padding: var(--space-xl) var(--space-2xl);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-light);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--color-text-light);
  font-size: 0.9rem;
  margin-bottom: var(--space-lg);
  transition: color var(--transition-fast);
}

.back-link:hover {
  color: var(--color-primary);
}

.header-title-section {
  display: flex;
  align-items: baseline;
  gap: var(--space-lg);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text);
}

.page-subtitle {
  font-size: 1.2rem;
  color: var(--color-accent);
}

/* Main Content */
.page-main {
  flex: 1;
  padding: var(--space-2xl);
}

.plans-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* New Plan Card */
.new-plan-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl);
  background: var(--color-surface);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: var(--space-2xl);
}

.new-plan-card:hover {
  border-color: var(--color-primary);
  background: var(--color-parchment);
}

.new-plan-card:hover .new-plan-icon {
  color: var(--color-primary);
  transform: scale(1.1);
}

.new-plan-icon {
  width: 64px;
  height: 64px;
  color: var(--color-text-light);
  margin-bottom: var(--space-md);
  transition: all var(--transition-base);
}

.new-plan-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
}

.new-plan-desc {
  color: var(--color-text-light);
  font-size: 0.95rem;
}

/* Plans Grid */
.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-xl);
}

/* Plan Card */
.plan-card {
  padding: 0;
  cursor: pointer;
  transition: all var(--transition-base);
  overflow: visible;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.plan-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md) var(--space-lg);
  border-bottom: 1px solid var(--color-border-light);
}

.plan-status {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
}

.plan-status.draft {
  background: var(--color-parchment-dark);
  color: var(--color-text-light);
}

.plan-status.planning {
  background: rgba(74, 124, 124, 0.1);
  color: var(--color-primary);
}

.plan-status.confirmed {
  background: rgba(139, 154, 107, 0.1);
  color: var(--color-sage);
}

.plan-status.completed {
  background: rgba(201, 145, 93, 0.1);
  color: var(--color-accent);
}

.plan-menu-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 50%;
  color: var(--color-text-light);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.plan-menu-btn:hover {
  background: var(--color-parchment);
  color: var(--color-text);
}

.plan-card-body {
  padding: var(--space-lg);
}

.plan-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-md);
}

.plan-destination,
.plan-dates {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  color: var(--color-text-light);
  font-size: 0.9rem;
  margin-bottom: var(--space-sm);
}

.plan-destination svg,
.plan-dates svg {
  flex-shrink: 0;
}

.plan-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md) var(--space-lg);
  background: var(--color-parchment);
  border-top: 1px solid var(--color-border-light);
}

.plan-updated {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.plan-action {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-primary);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: var(--space-3xl);
}

.empty-illustration {
  width: 200px;
  height: 200px;
  margin: 0 auto var(--space-xl);
  color: var(--color-border);
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-md);
}

.empty-desc {
  color: var(--color-text-light);
  max-width: 400px;
  margin: 0 auto;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-3xl);
  color: var(--color-text-light);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--space-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Context Menu */
.context-menu {
  position: fixed;
  z-index: 1000;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: var(--space-sm);
  min-width: 160px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  width: 100%;
  padding: var(--space-sm) var(--space-md);
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  color: var(--color-text);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.menu-item:hover {
  background: var(--color-parchment);
}

.menu-item.danger {
  color: var(--color-rust);
}

.menu-item.danger:hover {
  background: rgba(166, 93, 63, 0.1);
}

.menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    padding: var(--space-lg);
  }

  .header-title-section {
    flex-direction: column;
    gap: var(--space-sm);
  }

  .page-title {
    font-size: 1.5rem;
  }

  .page-main {
    padding: var(--space-lg);
  }

  .plans-grid {
    grid-template-columns: 1fr;
  }
}
</style>