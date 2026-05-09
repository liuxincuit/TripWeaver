<template>
  <div class="register-page paper-texture">
    <!-- Decorative Background Elements -->
    <div class="bg-decoration">
      <div class="compass-rose"></div>
      <div class="map-lines"></div>
    </div>

    <div class="register-container">
      <!-- Left Panel - Form -->
      <div class="form-panel">
        <div class="form-card card">
          <div class="form-header">
            <h2 class="form-title font-display">创建账户</h2>
            <p class="form-subtitle">加入 TripWeaver，开启探索之旅</p>
          </div>

          <form @submit.prevent="handleRegister" class="register-form">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <input
                  v-model="form.username"
                  type="text"
                  class="input"
                  placeholder="请输入用户名"
                  data-testid="username-input"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">邮箱地址</label>
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
                <input
                  v-model="form.email"
                  type="email"
                  class="input"
                  placeholder="请输入邮箱地址"
                  data-testid="email-input"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">密码</label>
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                <input
                  v-model="form.password"
                  type="password"
                  class="input"
                  placeholder="请输入密码（至少6位）"
                  data-testid="password-input"
                  required
                  minlength="6"
                />
              </div>
              <p class="form-hint">密码长度至少6个字符</p>
            </div>

            <div v-if="error" class="error-message" data-testid="error-message">
              <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              {{ error }}
            </div>

            <button type="submit" class="btn btn-accent btn-block" :disabled="loading" data-testid="register-button">
              <span v-if="loading" class="loading-spinner"></span>
              <span>{{ loading ? '注册中...' : '注册账户' }}</span>
            </button>
          </form>

          <div class="form-footer">
            <p>已有账户？</p>
            <router-link to="/login" class="link-accent" data-testid="login-link">立即登录</router-link>
          </div>
        </div>
      </div>

      <!-- Right Panel - Branding -->
      <div class="brand-panel">
        <div class="brand-content">
          <div class="brand-icon">
            <svg viewBox="0 0 80 80" class="logo-svg">
              <circle cx="40" cy="40" r="38" fill="none" stroke="currentColor" stroke-width="1.5"/>
              <path d="M40 10 L40 70" stroke="currentColor" stroke-width="1" opacity="0.5"/>
              <path d="M10 40 L70 40" stroke="currentColor" stroke-width="1" opacity="0.5"/>
              <path d="M40 20 L50 35 L40 50 L30 35 Z" fill="currentColor" opacity="0.8"/>
              <circle cx="40" cy="40" r="4" fill="currentColor"/>
              <text x="40" y="75" text-anchor="middle" class="logo-text">N</text>
            </svg>
          </div>
          <h1 class="brand-title">TripWeaver</h1>
          <p class="brand-tagline font-handwritten">让每一次旅行都成为故事</p>

          <div class="journey-steps">
            <div class="step-item">
              <div class="step-number">01</div>
              <div class="step-content">
                <h3 class="step-title">描述梦想</h3>
                <p class="step-desc">告诉 AI 你想去哪里</p>
              </div>
            </div>
            <div class="step-item">
              <div class="step-number">02</div>
              <div class="step-content">
                <h3 class="step-title">智能规划</h3>
                <p class="step-desc">获得个性化行程方案</p>
              </div>
            </div>
            <div class="step-item">
              <div class="step-number">03</div>
              <div class="step-content">
                <h3 class="step-title">启程探索</h3>
                <p class="step-desc">带上计划，出发吧</p>
              </div>
            </div>
          </div>
        </div>

        <div class="brand-footer">
          <span class="ornament"></span>
          <span class="font-handwritten">你的旅程从这里开始</span>
          <span class="ornament"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  email: '',
  password: ''
})
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  loading.value = true
  error.value = ''
  try {
    await userStore.register(form.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-lg);
  position: relative;
  overflow: hidden;
}

/* Background Decorations */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.compass-rose {
  position: absolute;
  top: -100px;
  left: -100px;
  width: 400px;
  height: 400px;
  opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200'%3E%3Ccircle cx='100' cy='100' r='95' fill='none' stroke='%233d2b1f' stroke-width='2'/%3E%3Cpath d='M100 10 L100 190' stroke='%233d2b1f' stroke-width='1'/%3E%3Cpath d='M10 100 L190 100' stroke='%233d2b1f' stroke-width='1'/%3E%3Cpath d='M100 20 L120 100 L100 180 L80 100 Z' fill='%233d2b1f' opacity='0.3'/%3E%3Cpath d='M20 100 L100 80 L180 100 L100 120 Z' fill='%233d2b1f' opacity='0.2'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
}

.map-lines {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 200px;
  opacity: 0.03;
  background-image:
    linear-gradient(0deg, var(--color-espresso) 1px, transparent 1px),
    linear-gradient(90deg, var(--color-espresso) 1px, transparent 1px);
  background-size: 50px 50px;
}

/* Register Container */
.register-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  position: relative;
  animation: fadeInUp 0.6s ease forwards;
}

/* Form Panel */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl);
  background: var(--color-surface);
}

.form-card {
  width: 100%;
  max-width: 360px;
  padding: var(--space-xl);
  background: transparent;
  border: none;
  box-shadow: none;
}

.form-card::before {
  display: none;
}

.form-header {
  text-align: center;
  margin-bottom: var(--space-2xl);
}

.form-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
}

.form-subtitle {
  color: var(--color-text-light);
  font-size: 0.95rem;
}

/* Form Styles */
.register-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.form-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.02em;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: var(--space-md);
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--color-text-light);
  pointer-events: none;
}

.input-wrapper .input {
  padding-left: calc(var(--space-md) * 2 + 18px);
}

.form-hint {
  font-size: 0.8rem;
  color: var(--color-text-light);
  font-style: italic;
}

.error-message {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  background: rgba(166, 93, 63, 0.1);
  border: 1px solid var(--color-rust);
  border-radius: var(--radius-md);
  color: var(--color-rust);
  font-size: 0.9rem;
}

.error-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.btn-block {
  width: 100%;
  padding: var(--space-md) var(--space-lg);
  font-size: 1rem;
  margin-top: var(--space-sm);
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  margin-top: var(--space-xl);
  padding-top: var(--space-xl);
  border-top: 1px solid var(--color-border-light);
  color: var(--color-text-light);
  font-size: 0.9rem;
}

.link-accent {
  color: var(--color-accent);
  font-weight: 500;
}

.link-accent:hover {
  color: var(--color-accent-dark);
}

/* Brand Panel */
.brand-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-3xl);
  background: linear-gradient(
    135deg,
    var(--color-amber-dark) 0%,
    var(--color-amber) 50%,
    var(--color-amber-light) 100%
  );
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  opacity: 0.05;
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-icon {
  width: 80px;
  height: 80px;
  margin-bottom: var(--space-xl);
}

.logo-svg {
  width: 100%;
  height: 100%;
  color: white;
}

.logo-text {
  font-family: var(--font-display);
  font-size: 12px;
  fill: white;
}

.brand-title {
  font-family: var(--font-display);
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--space-sm);
  letter-spacing: -0.02em;
}

.brand-tagline {
  font-size: 1.5rem;
  opacity: 0.9;
  margin-bottom: var(--space-2xl);
}

/* Journey Steps */
.journey-steps {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  animation: slideInLeft 0.5s ease forwards;
  animation-delay: calc(var(--index, 0) * 0.1s);
  opacity: 0;
}

.step-item:nth-child(1) { --index: 1; }
.step-item:nth-child(2) { --index: 2; }
.step-item:nth-child(3) { --index: 3; }

.step-number {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 700;
  opacity: 0.6;
  line-height: 1;
}

.step-content {
  flex: 1;
}

.step-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: var(--space-xs);
}

.step-desc {
  font-size: 0.85rem;
  opacity: 0.8;
}

.brand-footer {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  position: relative;
  z-index: 1;
}

.brand-footer .ornament {
  flex: 1;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.5), transparent);
}

.brand-footer span:not(.ornament) {
  font-size: 1.1rem;
  opacity: 0.8;
}

/* Responsive */
@media (max-width: 768px) {
  .register-container {
    flex-direction: column-reverse;
    max-width: 400px;
  }

  .brand-panel {
    padding: var(--space-xl);
    min-height: auto;
  }

  .brand-title {
    font-size: 2rem;
  }

  .journey-steps {
    display: none;
  }

  .brand-footer {
    display: none;
  }

  .form-panel {
    padding: var(--space-lg);
  }
}
</style>