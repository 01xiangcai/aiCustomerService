<template>
  <div class="apps-page">
    <div class="page-header">
      <div>
        <h2>机器人管理</h2>
        <p class="subtitle">创建和管理 AI 客服机器人</p>
      </div>
      <el-button type="primary" @click="showCreate = true">
        <el-icon><Plus /></el-icon>
        创建机器人
      </el-button>
    </div>

    <!-- 应用列表 -->
    <div class="app-grid">
      <div class="card app-card" v-for="app in apps" :key="app.id">
        <div class="app-card-header">
          <div class="app-icon" :style="{ background: app.themeColor || '#6366f1' }">
            <el-icon :size="24"><Monitor /></el-icon>
          </div>
          <div class="app-info">
            <h3>{{ app.appName }}</h3>
            <el-tag :type="app.status === 1 ? 'success' : 'danger'" size="small">
              {{ app.status === 1 ? '运行中' : '已停用' }}
            </el-tag>
          </div>
        </div>

        <div class="app-key-section">
          <div class="key-row">
            <span class="key-label">AppKey</span>
            <code class="key-value">{{ app.appKey }}</code>
            <el-button text size="small" @click="copyText(app.appKey)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
          <div class="key-row">
            <span class="key-label">模型</span>
            <span class="key-value model-tag">{{ app.modelType }}</span>
          </div>
        </div>

        <div class="app-actions">
          <el-button size="small" @click="$router.push(`/apps/${app.id}/knowledge`)">
            <el-icon><Document /></el-icon> 知识库
          </el-button>
          <el-button size="small" @click="$router.push(`/apps/${app.id}/records`)">
            <el-icon><ChatDotRound /></el-icon> 对话记录
          </el-button>
          <el-button size="small" @click="editApp(app)">
            <el-icon><Edit /></el-icon> 配置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 创建对话框 -->
    <el-dialog v-model="showCreate" title="创建机器人" width="480px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="createForm.appName" placeholder="例如：产品客服、售后助手" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog v-model="showEdit" title="机器人配置" width="560px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="editForm.appName" />
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input v-model="editForm.welcomeMsg" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="兜底回复">
          <el-input v-model="editForm.fallbackMsg" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="模型">
          <el-select v-model="editForm.modelType" style="width: 100%">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="通义千问" value="qwen" />
            <el-option label="GPT-4o" value="gpt4o" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题色">
          <el-color-picker v-model="editForm.themeColor" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { appApi } from '@/api'

const apps = ref([])
const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const updating = ref(false)

const createForm = reactive({ appName: '' })
const editForm = reactive({ id: null, appName: '', welcomeMsg: '', fallbackMsg: '', modelType: '', themeColor: '' })

onMounted(() => fetchApps())

const fetchApps = async () => {
  try {
    const res = await appApi.list({ page: 1, size: 100 })
    apps.value = res.data.records || []
  } catch (e) { /* 拦截器已处理 */ }
}

const handleCreate = async () => {
  if (!createForm.appName) return ElMessage.warning('请输入机器人名称')
  creating.value = true
  try {
    await appApi.create({ appName: createForm.appName })
    ElMessage.success('创建成功')
    showCreate.value = false
    createForm.appName = ''
    fetchApps()
  } catch (e) { /* 拦截器已处理 */ } finally { creating.value = false }
}

const editApp = (app) => {
  Object.assign(editForm, app)
  showEdit.value = true
}

const handleUpdate = async () => {
  updating.value = true
  try {
    await appApi.update(editForm.id, editForm)
    ElMessage.success('保存成功')
    showEdit.value = false
    fetchApps()
  } catch (e) { /* 拦截器已处理 */ } finally { updating.value = false }
}

const copyText = (text) => {
  navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}
</script>

<style scoped>
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.app-card {
  padding: 20px;
}

.app-card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.app-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.app-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.app-key-section {
  background: var(--bg-main);
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 16px;
}

.key-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.key-row + .key-row {
  margin-top: 6px;
}

.key-label {
  color: var(--text-muted);
  min-width: 50px;
}

.key-value {
  font-family: 'SF Mono', 'Cascadia Code', monospace;
  color: var(--text-secondary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.model-tag {
  font-family: inherit;
  color: var(--primary);
  font-weight: 500;
}

.app-actions {
  display: flex;
  gap: 8px;
}
</style>
