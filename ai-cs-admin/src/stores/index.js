import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const tenantId = ref(localStorage.getItem('tenantId') || '')
    const tenantName = ref(localStorage.getItem('tenantName') || '')

    /** 设置登录信息 */
    function setUser(data) {
        token.value = data.token
        tenantId.value = data.tenantId
        tenantName.value = data.tenantName
        localStorage.setItem('token', data.token)
        localStorage.setItem('tenantId', data.tenantId)
        localStorage.setItem('tenantName', data.tenantName)
    }

    /** 退出登录 */
    function logout() {
        token.value = ''
        tenantId.value = ''
        tenantName.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('tenantId')
        localStorage.removeItem('tenantName')
    }

    /** 是否已登录 */
    const isLoggedIn = () => !!token.value

    return { token, tenantId, tenantName, setUser, logout, isLoggedIn }
})

/**
 * 应用状态管理
 */
export const useAppStore = defineStore('app', () => {
    const isDark = ref(localStorage.getItem('theme') === 'dark')
    const sidebarCollapsed = ref(false)
    const currentApp = ref(null)

    /** 切换主题 */
    function toggleTheme() {
        isDark.value = !isDark.value
        document.documentElement.classList.toggle('dark', isDark.value)
        localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    }

    /** 初始化主题 */
    function initTheme() {
        document.documentElement.classList.toggle('dark', isDark.value)
    }

    /** 切换侧边栏 */
    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value
    }

    return { isDark, sidebarCollapsed, currentApp, toggleTheme, initTheme, toggleSidebar }
})
