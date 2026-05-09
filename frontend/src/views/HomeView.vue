<template>
  <div class="home-page paper-texture">
    <!-- Navigation -->
    <nav class="nav-bar">
      <div class="nav-brand">
        <svg viewBox="0 0 40 40" class="nav-logo">
          <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5"/>
          <path d="M20 8 L24 20 L20 32 L16 20 Z" fill="currentColor" opacity="0.8"/>
          <circle cx="20" cy="20" r="2" fill="currentColor"/>
        </svg>
        <span class="nav-title font-display">TripWeaver</span>
      </div>
      <div class="nav-actions">
        <button @click="viewPlans" class="btn btn-ghost">我的计划</button>
        <button @click="logout" class="btn btn-outline">退出</button>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-decoration">
        <div class="compass-large"></div>
        <div class="map-dots"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge font-handwritten">AI 驱动的旅行规划</div>
        <h1 class="hero-title font-display">
          让每一次旅行<br>
          <span class="text-accent">都成为精彩故事</span>
        </h1>
        <p class="hero-subtitle">
          告诉 TripWeaver 你的旅行梦想，AI 将为你编织完美的行程方案
        </p>

        <div class="hero-actions">
          <button @click="startNewPlan" class="btn btn-primary btn-large" data-testid="new-plan-button">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            开始规划新旅程
          </button>
          <button @click="viewPlans" class="btn btn-outline btn-large">
            查看我的计划
          </button>
        </div>
      </div>

      <!-- Floating Cards -->
      <div class="floating-cards">
        <div class="float-card card-1">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
          </div>
          <span class="card-label">目的地推荐</span>
        </div>
        <div class="float-card card-2">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <span class="card-label">行程安排</span>
        </div>
        <div class="float-card card-3">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <span class="card-label">时间规划</span>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="section-header">
        <span class="ornament"></span>
        <h2 class="section-title font-display">为什么选择 TripWeaver</h2>
        <span class="ornament"></span>
      </div>

      <div class="features-grid">
        <div class="feature-card card">
          <div class="feature-icon-wrapper">
            <svg class="feature-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="24" cy="24" r="20"/>
              <path d="M24 8v32M8 24h32"/>
              <path d="M24 14l6 10-6 10-6-10z" fill="currentColor" opacity="0.2"/>
            </svg>
          </div>
          <h3 class="feature-title font-display">智能对话规划</h3>
          <p class="feature-desc">
            用自然语言描述你的旅行想法，AI 会理解你的需求并生成个性化方案
          </p>
        </div>

        <div class="feature-card card">
          <div class="feature-icon-wrapper">
            <svg class="feature-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M4 24c0-11 8.5-20 20-20s20 9 20 20"/>
              <path d="M4 24c0 11 8.5 20 20 20s20-9 20-20"/>
              <circle cx="24" cy="24" r="8"/>
              <circle cx="24" cy="24" r="3" fill="currentColor"/>
            </svg>
          </div>
          <h3 class="feature-title font-display">全方位探索</h3>
          <p class="feature-desc">
            从景点推荐到美食攻略，从交通安排到住宿建议，一站式解决所有问题
          </p>
        </div>

        <div class="feature-card card">
          <div class="feature-icon-wrapper">
            <svg class="feature-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M8 40V16l16-8 16 8v24"/>
              <path d="M8 16l16 8 16-8"/>
              <path d="M24 24v16"/>
              <circle cx="24" cy="12" r="4" fill="currentColor" opacity="0.2"/>
            </svg>
          </div>
          <h3 class="feature-title font-display">灵活调整</h3>
          <p class="feature-desc">
            随时修改行程细节，AI 会根据你的反馈持续优化旅行计划
          </p>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-card">
        <div class="cta-content">
          <h2 class="cta-title font-display">准备好开始了吗？</h2>
          <p class="cta-subtitle font-handwritten">你的下一次冒险正在等待</p>
          <button @click="startNewPlan" class="btn btn-accent btn-large">
            立即开始规划
            <svg class="btn-icon-right" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
        </div>
        <div class="cta-decoration">
          <svg viewBox="0 0 200 200" class="cta-pattern">
            <circle cx="100" cy="100" r="80" fill="none" stroke="currentColor" stroke-width="0.5"/>
            <circle cx="100" cy="100" r="60" fill="none" stroke="currentColor" stroke-width="0.5"/>
            <circle cx="100" cy="100" r="40" fill="none" stroke="currentColor" stroke-width="0.5"/>
            <path d="M100 20 L100 180" stroke="currentColor" stroke-width="0.5"/>
            <path d="M20 100 L180 100" stroke="currentColor" stroke-width="0.5"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="page-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="font-display">TripWeaver</span>
          <span class="font-handwritten">探索 · 发现 · 记录</span>
        </div>
        <p class="footer-copy">© 2026 TripWeaver. AI 驱动的智能旅行规划</p>
      </div>
    </footer>
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
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Navigation */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-lg) var(--space-2xl);
  position: relative;
  z-index: 100;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.nav-logo {
  width: 36px;
  height: 36px;
  color: var(--color-primary);
}

.nav-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
}

.nav-actions {
  display: flex;
  gap: var(--space-md);
}

/* Hero Section */
.hero-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl);
  position: relative;
  overflow: hidden;
}

.hero-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.compass-large {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 800px;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200'%3E%3Ccircle cx='100' cy='100' r='95' fill='none' stroke='%233d2b1f' stroke-width='1'/%3E%3Cpath d='M100 10 L100 190' stroke='%233d2b1f' stroke-width='0.5'/%3E%3Cpath d='M10 100 L190 100' stroke='%233d2b1f' stroke-width='0.5'/%3E%3Cpath d='M100 20 L120 100 L100 180 L80 100 Z' fill='%233d2b1f' opacity='0.2'/%3E%3Cpath d='M20 100 L100 80 L180 100 L100 120 Z' fill='%233d2b1f' opacity='0.1'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
}

.map-dots {
  position: absolute;
  inset: 0;
  opacity: 0.02;
  background-image: radial-gradient(circle, var(--color-espresso) 1px, transparent 1px);
  background-size: 30px 30px;
}

.hero-content {
  text-align: center;
  max-width: 700px;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.8s ease forwards;
}

.hero-badge {
  display: inline-block;
  padding: var(--space-sm) var(--space-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: 100px;
  font-size: 1.1rem;
  color: var(--color-accent);
  margin-bottom: var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  color: var(--color-text);
  margin-bottom: var(--space-lg);
  letter-spacing: -0.02em;
}

.text-accent {
  color: var(--color-accent);
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--color-text-light);
  margin-bottom: var(--space-2xl);
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: var(--space-lg);
  justify-content: center;
}

.btn-large {
  padding: var(--space-md) var(--space-xl);
  font-size: 1.05rem;
}

.btn-icon {
  width: 20px;
  height: 20px;
}

.btn-icon-right {
  width: 20px;
  height: 20px;
  margin-left: var(--space-sm);
}

/* Floating Cards */
.floating-cards {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  animation: float 6s ease-in-out infinite;
}

.float-card .card-icon {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.float-card .card-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--color-text);
}

.card-1 {
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.card-2 {
  top: 30%;
  right: 8%;
  animation-delay: 2s;
}

.card-3 {
  bottom: 25%;
  left: 15%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

/* Features Section */
.features-section {
  padding: var(--space-3xl) var(--space-2xl);
  background: var(--color-surface);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-lg);
  margin-bottom: var(--space-2xl);
}

.section-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--color-text);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-xl);
  max-width: 1100px;
  margin: 0 auto;
}

.feature-card {
  padding: var(--space-xl);
  text-align: center;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.feature-icon-wrapper {
  width: 72px;
  height: 72px;
  margin: 0 auto var(--space-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-parchment), var(--color-parchment-dark));
  border-radius: 50%;
}

.feature-icon {
  width: 40px;
  height: 40px;
  color: var(--color-primary);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-md);
}

.feature-desc {
  color: var(--color-text-light);
  line-height: 1.7;
}

/* CTA Section */
.cta-section {
  padding: var(--space-3xl) var(--space-2xl);
}

.cta-card {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-3xl);
  background: linear-gradient(
    135deg,
    var(--color-teal-dark) 0%,
    var(--color-teal) 100%
  );
  border-radius: var(--radius-xl);
  position: relative;
  overflow: hidden;
}

.cta-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 2rem;
  font-weight: 600;
  color: white;
  margin-bottom: var(--space-sm);
}

.cta-subtitle {
  font-size: 1.3rem;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: var(--space-xl);
}

.cta-decoration {
  position: absolute;
  right: -50px;
  bottom: -50px;
  width: 250px;
  height: 250px;
  opacity: 0.1;
}

.cta-pattern {
  width: 100%;
  height: 100%;
  color: white;
}

/* Footer */
.page-footer {
  padding: var(--space-xl) var(--space-2xl);
  border-top: 1px solid var(--color-border-light);
  background: var(--color-surface);
}

.footer-content {
  text-align: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.footer-brand span:first-child {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
}

.footer-brand span:last-child {
  font-size: 1rem;
  color: var(--color-text-light);
}

.footer-copy {
  font-size: 0.85rem;
  color: var(--color-text-light);
}

/* Responsive */
@media (max-width: 768px) {
  .nav-bar {
    padding: var(--space-md) var(--space-lg);
  }

  .hero-title {
    font-size: 2.25rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .floating-cards {
    display: none;
  }

  .features-section {
    padding: var(--space-2xl) var(--space-lg);
  }

  .cta-card {
    padding: var(--space-xl);
  }

  .cta-title {
    font-size: 1.5rem;
  }
}
</style>