<template>
  <div class="plan-detail-page paper-texture">
    <!-- Header -->
    <header class="page-header">
      <div class="header-content">
        <router-link to="/plans" class="back-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回列表
        </router-link>

        <div class="header-main">
          <div class="header-info">
            <h1 class="page-title font-display">{{ plan.title || '旅行计划详情' }}</h1>
            <div class="plan-meta">
              <span class="plan-status" :class="plan.status">
                {{ getStatusText(plan.status) }}
              </span>
              <span class="plan-date">创建于 {{ formatDate(plan.createdAt) }}</span>
            </div>
          </div>

          <div class="header-actions">
            <button class="btn btn-outline" @click="continueChat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              继续对话
            </button>
            <button class="btn btn-ghost" @click="showEditModal = true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              编辑
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="page-main">
      <div class="detail-container">
        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>

        <!-- Plan Content -->
        <div v-else class="plan-content">
          <!-- Overview Section -->
          <section class="overview-section">
            <div class="overview-card card">
              <div class="overview-header">
                <div class="overview-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <h2 class="overview-title font-display">旅行概览</h2>
              </div>

              <div class="overview-grid">
                <div class="overview-item">
                  <span class="item-label">目的地</span>
                  <span class="item-value">{{ plan.destination || '待确定' }}</span>
                </div>
                <div class="overview-item">
                  <span class="item-label">出发日期</span>
                  <span class="item-value">{{ plan.startDate ? formatDate(plan.startDate) : '待确定' }}</span>
                </div>
                <div class="overview-item">
                  <span class="item-label">返回日期</span>
                  <span class="item-value">{{ plan.endDate ? formatDate(plan.endDate) : '待确定' }}</span>
                </div>
                <div class="overview-item">
                  <span class="item-label">旅行人数</span>
                  <span class="item-value">{{ plan.travelers || '待确定' }}</span>
                </div>
              </div>
            </div>
          </section>

          <!-- Itinerary Section -->
          <section class="itinerary-section" v-if="plan.itinerary && plan.itinerary.length > 0">
            <div class="section-header">
              <div class="section-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <h2 class="section-title font-display">行程安排</h2>
              <span class="ornament"></span>
            </div>

            <div class="itinerary-timeline">
              <div
                v-for="(day, index) in plan.itinerary"
                :key="index"
                class="timeline-day"
              >
                <div class="day-marker">
                  <span class="day-number">Day {{ index + 1 }}</span>
                  <div class="day-line"></div>
                </div>

                <div class="day-card card">
                  <div class="day-header">
                    <h3 class="day-title font-display">{{ day.title || `第 ${index + 1} 天` }}</h3>
                    <span class="day-date" v-if="day.date">{{ formatDate(day.date) }}</span>
                  </div>

                  <div class="day-activities">
                    <div
                      v-for="(activity, actIndex) in day.activities"
                      :key="actIndex"
                      class="activity-item"
                    >
                      <div class="activity-time" v-if="activity.time">
                        {{ activity.time }}
                      </div>
                      <div class="activity-content">
                        <span class="activity-type" :class="activity.type">
                          {{ getActivityTypeText(activity.type) }}
                        </span>
                        <h4 class="activity-title">{{ activity.name }}</h4>
                        <p class="activity-desc" v-if="activity.description">
                          {{ activity.description }}
                        </p>
                        <div class="activity-location" v-if="activity.location">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                            <circle cx="12" cy="10" r="3"/>
                          </svg>
                          {{ activity.location }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Notes Section -->
          <section class="notes-section" v-if="plan.notes">
            <div class="section-header">
              <div class="section-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </div>
              <h2 class="section-title font-display">旅行笔记</h2>
              <span class="ornament"></span>
            </div>

            <div class="notes-card card">
              <div class="notes-content" v-html="formatNotes(plan.notes)"></div>
            </div>
          </section>

          <!-- Empty Itinerary -->
          <section class="empty-itinerary" v-if="!plan.itinerary || plan.itinerary.length === 0">
            <div class="empty-card card">
              <div class="empty-illustration">
                <svg viewBox="0 0 100 100" fill="none" stroke="currentColor" stroke-width="1">
                  <circle cx="50" cy="50" r="40" opacity="0.3"/>
                  <path d="M50 20 L50 80" opacity="0.3"/>
                  <path d="M20 50 L80 50" opacity="0.3"/>
                  <path d="M50 30 L60 50 L50 70 L40 50 Z" fill="currentColor" opacity="0.2"/>
                </svg>
              </div>
              <h3 class="empty-title font-display">行程尚未生成</h3>
              <p class="empty-desc">继续与 AI 对话，完善你的旅行计划</p>
              <button class="btn btn-primary" @click="continueChat">
                开始对话
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
              </button>
            </div>
          </section>
        </div>
      </div>
    </main>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content card" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title font-display">编辑计划</h2>
          <button class="modal-close" @click="showEditModal = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="24" height="24">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <form @submit.prevent="saveEdit" class="modal-form">
          <div class="form-group">
            <label class="form-label">计划标题</label>
            <input v-model="editForm.title" type="text" class="input" placeholder="给旅行起个名字" />
          </div>

          <div class="form-group">
            <label class="form-label">目的地</label>
            <input v-model="editForm.destination" type="text" class="input" placeholder="旅行目的地" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">出发日期</label>
              <input v-model="editForm.startDate" type="date" class="input" />
            </div>
            <div class="form-group">
              <label class="form-label">返回日期</label>
              <input v-model="editForm.endDate" type="date" class="input" />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">状态</label>
            <select v-model="editForm.status" class="input">
              <option value="draft">草稿</option>
              <option value="planning">规划中</option>
              <option value="confirmed">已确认</option>
              <option value="completed">已完成</option>
            </select>
          </div>

          <div class="modal-actions">
            <button type="button" class="btn btn-ghost" @click="showEditModal = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '../api/chat'
import { marked } from 'marked'

const route = useRoute()
const router = useRouter()

const plan = ref({})
const loading = ref(false)
const saving = ref(false)
const showEditModal = ref(false)
const editForm = reactive({
  title: '',
  destination: '',
  startDate: '',
  endDate: '',
  status: 'draft'
})

onMounted(async () => {
  await loadPlan()
})

async function loadPlan() {
  loading.value = true
  try {
    const id = parseInt(route.params.id)
    const result = await chatApi.getPlan(id)
    plan.value = result || {}

    // Initialize edit form
    editForm.title = plan.value.title || ''
    editForm.destination = plan.value.destination || ''
    editForm.startDate = plan.value.startDate || ''
    editForm.endDate = plan.value.endDate || ''
    editForm.status = plan.value.status || 'draft'
  } catch (e) {
    console.error('Failed to load plan:', e)
  } finally {
    loading.value = false
  }
}

async function saveEdit() {
  saving.value = true
  try {
    await chatApi.updatePlan(plan.value.id, editForm)
    plan.value = { ...plan.value, ...editForm }
    showEditModal.value = false
  } catch (e) {
    console.error('Failed to save:', e)
    alert('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

function continueChat() {
  router.push(`/chat/${plan.value.id}`)
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

function getActivityTypeText(type) {
  const typeMap = {
    sight: '景点',
    food: '美食',
    hotel: '住宿',
    transport: '交通',
    activity: '活动',
    shopping: '购物'
  }
  return typeMap[type] || '活动'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function formatNotes(content) {
  return marked.parse(content || '')
}
</script>

<style scoped>
.plan-detail-page {
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
  max-width: 900px;
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

.header-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-xl);
}

.header-info {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: var(--space-md);
}

.plan-meta {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
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

.plan-date {
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.header-actions {
  display: flex;
  gap: var(--space-md);
}

/* Main Content */
.page-main {
  flex: 1;
  padding: var(--space-2xl);
}

.detail-container {
  max-width: 900px;
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

/* Overview Section */
.overview-section {
  margin-bottom: var(--space-2xl);
}

.overview-card {
  padding: var(--space-xl);
}

.overview-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}

.overview-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-parchment);
  border-radius: 50%;
  color: var(--color-primary);
}

.overview-icon svg {
  width: 24px;
  height: 24px;
}

.overview-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.item-label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.item-value {
  font-size: 1.1rem;
  color: var(--color-text);
}

/* Section Header */
.section-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}

.section-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-parchment);
  border-radius: 50%;
  color: var(--color-primary);
}

.section-icon svg {
  width: 20px;
  height: 20px;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

/* Itinerary Section */
.itinerary-section {
  margin-bottom: var(--space-2xl);
}

.itinerary-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.timeline-day {
  display: flex;
  gap: var(--space-lg);
}

.day-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}

.day-number {
  font-family: var(--font-display);
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-surface);
  padding: var(--space-sm) var(--space-md);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-md);
}

.day-line {
  flex: 1;
  width: 2px;
  background: linear-gradient(to bottom, var(--color-primary), var(--color-border-light));
  margin-top: var(--space-sm);
}

.day-card {
  flex: 1;
  padding: var(--space-lg);
}

.day-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--color-border-light);
}

.day-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}

.day-date {
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.day-activities {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.activity-item {
  display: flex;
  gap: var(--space-md);
}

.activity-time {
  min-width: 60px;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text-light);
}

.activity-content {
  flex: 1;
}

.activity-type {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-sm);
}

.activity-type.sight {
  background: rgba(74, 124, 124, 0.1);
  color: var(--color-primary);
}

.activity-type.food {
  background: rgba(201, 145, 93, 0.1);
  color: var(--color-accent);
}

.activity-type.hotel {
  background: rgba(139, 154, 107, 0.1);
  color: var(--color-sage);
}

.activity-type.transport {
  background: rgba(166, 93, 63, 0.1);
  color: var(--color-rust);
}

.activity-type.activity {
  background: var(--color-parchment-dark);
  color: var(--color-text-light);
}

.activity-title {
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--space-xs);
}

.activity-desc {
  font-size: 0.9rem;
  color: var(--color-text-light);
  line-height: 1.5;
  margin-bottom: var(--space-sm);
}

.activity-location {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 0.85rem;
  color: var(--color-text-light);
}

/* Notes Section */
.notes-section {
  margin-bottom: var(--space-2xl);
}

.notes-card {
  padding: var(--space-xl);
}

.notes-content {
  line-height: 1.7;
  color: var(--color-text);
}

.notes-content :deep(p) {
  margin: 0.5rem 0;
}

.notes-content :deep(h1),
.notes-content :deep(h2),
.notes-content :deep(h3) {
  font-family: var(--font-display);
  margin: 1rem 0 0.5rem;
}

.notes-content :deep(ul),
.notes-content :deep(ol) {
  padding-left: 1.5rem;
}

/* Empty Itinerary */
.empty-itinerary {
  margin-bottom: var(--space-2xl);
}

.empty-card {
  padding: var(--space-3xl);
  text-align: center;
}

.empty-illustration {
  width: 100px;
  height: 100px;
  margin: 0 auto var(--space-xl);
  color: var(--color-border);
}

.empty-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-md);
}

.empty-desc {
  color: var(--color-text-light);
  margin-bottom: var(--space-xl);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(61, 43, 31, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: var(--space-lg);
}

.modal-content {
  width: 100%;
  max-width: 480px;
  padding: var(--space-xl);
  animation: fadeInUp 0.3s ease forwards;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-xl);
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.modal-close {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--color-text-light);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.modal-close:hover {
  color: var(--color-text);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-md);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-md);
  margin-top: var(--space-xl);
  padding-top: var(--space-xl);
  border-top: 1px solid var(--color-border-light);
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    padding: var(--space-lg);
  }

  .header-main {
    flex-direction: column;
    gap: var(--space-lg);
  }

  .header-actions {
    width: 100%;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .page-main {
    padding: var(--space-lg);
  }

  .timeline-day {
    flex-direction: column;
  }

  .day-marker {
    min-width: auto;
    flex-direction: row;
    gap: var(--space-md);
  }

  .day-line {
    display: none;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>