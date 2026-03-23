import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios 实例配置
 */
const request = axios.create({
    baseURL: '/',
    timeout: 30000,
    headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code && res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            if (res.code === 401) {
                localStorage.removeItem('token')
                router.push('/login')
            }
            return Promise.reject(new Error(res.msg))
        }
        return res
    },
    error => {
        const msg = error.response?.data?.msg || error.message || '网络异常'
        ElMessage.error(msg)
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

export default request
