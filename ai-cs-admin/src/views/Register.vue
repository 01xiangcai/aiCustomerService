<template>
  <div class="login-page">
    <!-- 极简背景 -->
    <div class="bg-abstract"></div>

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
/* 复用 Login.vue 极简样式 */
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; background: var(--bg-main); }
.bg-abstract { position: absolute; top: 0; left: 0; right: 0; height: 100%; background-image: radial-gradient(circle at top right, var(--border-color) 0%, transparent 40%), radial-gradient(circle at bottom left, var(--border-color) 0%, transparent 40%); opacity: 0.6; }

.login-card { width: 420px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 48px 40px; position: relative; z-index: 10; box-shadow: var(--shadow-lg); }
.card-header { text-align: center; margin-bottom: 32px; }
.card-header .logo-icon { width: 56px; height: 56px; border-radius: 12px; background: var(--primary); display: inline-flex; align-items: center; justify-content: center; color: var(--bg-main); margin-bottom: 16px; }
.card-header h1 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; letter-spacing: -0.03em; }
.card-header p { color: var(--text-secondary); font-size: 14px; }

.login-card :deep(.el-input__wrapper) { background: var(--bg-main); border: 1px solid var(--border-color); border-radius: 8px; box-shadow: none; color: var(--text-primary); transition: var(--transition); }
.login-card :deep(.el-input__wrapper:hover), .login-card :deep(.el-input__wrapper.is-focus) { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-bg); }
.login-card :deep(.el-input__inner) { color: var(--text-primary); }
.login-card :deep(.el-input__inner::placeholder) { color: var(--text-muted); }
.login-card :deep(.el-input__prefix .el-icon) { color: var(--text-secondary); }

.login-btn { width: 100%; height: 46px; border-radius: 8px; font-size: 16px; font-weight: 600; background: var(--primary); color: var(--bg-main); border: none; margin-top: 8px; transition: all 0.3s ease; }
.login-btn:hover { transform: translateY(-1px); background: var(--primary-light); box-shadow: var(--shadow-md); }
.card-footer { text-align: center; margin-top: 24px; font-size: 14px; color: var(--text-secondary); }
.card-footer a { color: var(--primary); font-weight: 600; margin-left: 4px; }
.card-footer a:hover { text-decoration: underline; }
</style>
