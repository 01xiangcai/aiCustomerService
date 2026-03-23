<template>
  <div class="settings-page">
    <div class="page-header">
      <div>
        <h2>账号设置</h2>
        <p class="subtitle">管理你的账号信息</p>
      </div>
    </div>

    <div class="card" style="max-width: 600px">
      <h3 class="section-title">基本信息</h3>
      <div class="info-row">
        <span class="info-label">租户名称</span>
        <span class="info-value">{{ userStore.tenantName }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">租户 ID</span>
        <span class="info-value mono">{{ userStore.tenantId }}</span>
      </div>

      <el-divider />

      <h3 class="section-title">外观设置</h3>
      <div class="info-row clickable" @click="appStore.toggleTheme">
        <span class="info-label">主题模式</span>
        <div class="theme-toggle">
          <span>{{ appStore.isDark ? '深色模式' : '浅色模式' }}</span>
          <el-switch v-model="appStore.isDark" @change="appStore.toggleTheme" />
        </div>
      </div>

      <el-divider />

      <h3 class="section-title">接入指南</h3>
      <div class="code-block">
        <p class="code-title">Spring Boot Starter 接入</p>
        <pre><code>&lt;dependency&gt;
    &lt;groupId&gt;com.aics&lt;/groupId&gt;
    &lt;artifactId&gt;ai-cs-starter&lt;/artifactId&gt;
    &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
      </div>

      <div class="code-block">
        <p class="code-title">前端 Widget 接入</p>
        <pre><code>&lt;script src="https://cdn.ai-cs.com/widget.min.js"&gt;&lt;/script&gt;
&lt;script&gt;AiCS.init({ appKey: 'your-app-key' })&lt;/script&gt;</code></pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore, useAppStore } from '@/stores'

const userStore = useUserStore()
const appStore = useAppStore()
</script>

<style scoped>
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
}

.info-row.clickable {
  cursor: pointer;
}

.info-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.info-value {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

.info-value.mono {
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  font-size: 13px;
}

.theme-toggle {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--text-primary);
}

.code-block {
  background: var(--bg-main);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  padding: 16px;
  margin-bottom: 12px;
}

.code-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.code-block pre {
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
  overflow-x: auto;
}
</style>
