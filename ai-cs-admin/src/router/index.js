import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录', public: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '注册', public: true }
    },
    {
        path: '/',
        component: () => import('@/layout/DefaultLayout.vue'),
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '控制台', icon: 'Odometer' }
            },
            {
                path: 'apps',
                name: 'Apps',
                component: () => import('@/views/Apps.vue'),
                meta: { title: '机器人管理', icon: 'Monitor' }
            },
            {
                path: 'apps/:id/knowledge',
                name: 'Knowledge',
                component: () => import('@/views/Knowledge.vue'),
                meta: { title: '知识库管理', icon: 'Document' }
            },
            {
                path: 'apps/:id/records',
                name: 'Records',
                component: () => import('@/views/Records.vue'),
                meta: { title: '对话记录', icon: 'ChatDotRound' }
            },
            {
                path: 'settings',
                name: 'Settings',
                component: () => import('@/views/Settings.vue'),
                meta: { title: '账号设置', icon: 'Setting' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (!to.meta.public && !token) {
        next('/login')
    } else {
        document.title = `${to.meta.title || 'AI 客服'} · AI 客服中间件`
        next()
    }
})

export default router
