<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </div>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">AI 客服</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <span v-show="!appStore.sidebarCollapsed">{{ item.title }}</span>
        </router-link>
      </nav>

      <!-- 底部 -->
      <div class="sidebar-footer">
        <div class="nav-item" @click="appStore.toggleTheme">
          <el-icon :size="20">
            <Sunny v-if="appStore.isDark" />
            <Moon v-else />
          </el-icon>
          <span v-show="!appStore.sidebarCollapsed">{{ appStore.isDark ? '浅色' : '深色' }}</span>
        </div>
        <div class="nav-item" @click="handleLogout">
          <el-icon :size="20"><SwitchButton /></el-icon>
          <span v-show="!appStore.sidebarCollapsed">退出登录</span>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <el-icon class="toggle-btn" :size="20" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute?.meta?.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <span class="tenant-name">{{ userStore.tenantName }}</span>
          <el-avatar :size="32" class="avatar">
            {{ userStore.tenantName?.charAt(0) }}
          </el-avatar>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore, useAppStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const currentRoute = computed(() => route)

const menuItems = [
  { path: '/dashboard', title: '控制台', icon: 'Odometer' },
  { path: '/apps', title: '机器人管理', icon: 'Monitor' },
  { path: '/settings', title: '账号设置', icon: 'Setting' }
]

const isActive = (path) => route.path.startsWith(path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--bg-main);
  flex-shrink: 0;
}

.logo-text {
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  letter-spacing: -0.03em;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 16px;
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
  white-space: nowrap;
  font-size: 14px;
}

.nav-item:hover {
  background: var(--bg-sidebar-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--primary);
  color: var(--bg-main);
  font-weight: 500;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sidebar.collapsed + .main-content,
.sidebar.collapsed ~ .main-content {
  margin-left: 72px;
}

.topbar {
  height: 60px;
  background: var(--bg-sidebar);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
}

.toggle-btn:hover {
  color: var(--primary);
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tenant-name {
  font-size: 14px;
  color: var(--text-secondary);
}

.avatar {
  background: var(--primary);
  color: var(--bg-main);
  font-weight: 600;
  cursor: pointer;
}

.page-content {
  flex: 1;
  padding: 24px;
}
</style>
