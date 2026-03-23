<template>
  <div class="login-page">
    <div class="bg-gradient"></div>
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
    </div>

    <div class="login-card">
      <div class="card-header">
        <div class="logo-icon">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <h1>创建账号</h1>
        <p>注册后即可使用 AI 客服中间件</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
        <el-form-item prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="企业/项目名称" size="large" :prefix-icon="OfficeBuilding" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" size="large" :prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" size="large" :prefix-icon="Lock" show-password />
        </el-form-item>

        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleRegister">
          注 册
        </el-button>
      </el-form>

      <div class="card-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Lock, OfficeBuilding } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ tenantName: '', email: '', password: '' })

const rules = {
  tenantName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authApi.register(form)
    userStore.setUser(res.data)
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (e) { /* 拦截器处理 */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 复用 Login.vue 样式 */
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; background: #0f0b2e; }
.bg-gradient { position: absolute; inset: 0; background: linear-gradient(135deg, #0f0b2e, #1a1145 30%, #1e1b4b 60%, #0f172a); }
.bg-orbs .orb { position: absolute; border-radius: 50%; filter: blur(80px); animation: float 8s ease-in-out infinite; }
.orb-1 { width: 350px; height: 350px; background: rgba(139, 92, 246, 0.15); top: -80px; left: -60px; }
.orb-2 { width: 280px; height: 280px; background: rgba(99, 102, 241, 0.12); bottom: -50px; right: -50px; animation-delay: -4s; }
@keyframes float { 0%, 100% { transform: translate(0, 0) scale(1); } 50% { transform: translate(20px, -20px) scale(1.03); } }

.login-card { width: 420px; background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(24px); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 20px; padding: 48px 40px; position: relative; z-index: 10; box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3); }
.card-header { text-align: center; margin-bottom: 32px; }
.card-header .logo-icon { width: 56px; height: 56px; border-radius: 16px; background: linear-gradient(135deg, #6366f1, #8b5cf6); display: inline-flex; align-items: center; justify-content: center; color: white; margin-bottom: 16px; }
.card-header h1 { font-size: 24px; font-weight: 700; color: #f1f5f9; margin-bottom: 6px; }
.card-header p { color: #94a3b8; font-size: 14px; }

.login-card :deep(.el-input__wrapper) { background: rgba(255, 255, 255, 0.06); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 10px; box-shadow: none; }
.login-card :deep(.el-input__wrapper:hover), .login-card :deep(.el-input__wrapper.is-focus) { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15); }
.login-card :deep(.el-input__inner) { color: #f1f5f9; }
.login-card :deep(.el-input__inner::placeholder) { color: #64748b; }
.login-card :deep(.el-input__prefix .el-icon) { color: #64748b; }

.login-btn { width: 100%; height: 46px; border-radius: 10px; font-size: 16px; font-weight: 600; background: linear-gradient(135deg, #6366f1, #8b5cf6); border: none; margin-top: 8px; transition: all 0.3s ease; }
.login-btn:hover { transform: translateY(-1px); box-shadow: 0 8px 25px rgba(99, 102, 241, 0.35); }
.card-footer { text-align: center; margin-top: 24px; font-size: 14px; color: #94a3b8; }
.card-footer a { color: #818cf8; font-weight: 500; margin-left: 4px; }
</style>
