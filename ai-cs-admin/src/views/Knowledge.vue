<template>
  <div class="knowledge-page">
    <div class="page-header">
      <div>
        <h2>知识库管理</h2>
        <p class="subtitle">上传文档让 AI 了解你的业务，回答更精准</p>
      </div>
      <el-upload
        ref="uploadRef"
        :action="`/api/knowledge/${appId}/upload`"
        :headers="{ Authorization: `Bearer ${userStore.token}` }"
        :before-upload="beforeUpload"
        :on-progress="onProgress"
        :on-success="onUploadSuccess"
        :on-error="onUploadError"
        :show-file-list="false"
        accept=".pdf,.txt,.doc,.docx,.md"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>
          上传文档
        </el-button>
      </el-upload>
    </div>

    <!-- 上传进度弹窗 -->
    <el-dialog
      v-model="uploading"
      title="正在上传"
      width="360px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      align-center
    >
      <div class="upload-progress-container">
        <el-progress
          type="circle"
          :percentage="uploadProgress"
          :status="uploadStatus"
          :width="140"
          :stroke-width="8"
          color="#0f172a"
        >
          <template #default="{ percentage }">
            <span class="progress-value">{{ percentage }}%</span>
            <span class="progress-text">{{ uploadStatus === 'success' ? '上传完成' : (percentage >= 99 ? '正在保存...' : '上传中...') }}</span>
          </template>
        </el-progress>
      </div>
    </el-dialog>

    <!-- 支持格式提示 -->
    <el-alert type="info" :closable="false" class="tips" show-icon>
      支持 PDF、Word、TXT、Markdown 格式，单个文件最大 50MB。上传后将自动解析和向量化。
    </el-alert>

    <!-- 文档列表 -->
    <div class="card">
      <el-table :data="documents" v-loading="loading" empty-text="暂无文档，请上传知识库文档">
        <el-table-column prop="fileName" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon :size="18" color="#6366f1"><Document /></el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="80" />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="分块数" width="80">
          <template #default="{ row }">{{ row.chunkCount || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.parseStatus)" size="small">
              {{ statusText(row.parseStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="170">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认删除此文档?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" text size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { knowledgeApi } from '@/api'
import { useUserStore } from '@/stores'

const route = useRoute()
const appId = route.params.id
const userStore = useUserStore()
const documents = ref([])
const loading = ref(false)

// 上传进度状态
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')

onMounted(() => fetchDocuments())

const fetchDocuments = async () => {
  loading.value = true
  try {
    const res = await knowledgeApi.listDocuments(appId, { page: 1, size: 100 })
    documents.value = res.data.records || []
  } catch (e) { /* 拦截器已处理 */ } finally { loading.value = false }
}

const beforeUpload = (file) => {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过 50MB')
    return false
  }
  uploadProgress.value = 0
  uploadStatus.value = ''
  // 延迟一点显示弹窗，防止一闪而过
  setTimeout(() => { uploading.value = true }, 100)
  return true
}

const onProgress = (event) => {
  // event.percent 是 ElementPlus 原生进度 0-100
  const p = Math.floor(event.percent)
  // 如果到 100% 但接口还没返回 HTTP 200，卡在 99% 等待后端保存
  uploadProgress.value = (p >= 100) ? 99 : p
}

const onUploadSuccess = () => {
  uploadProgress.value = 100
  uploadStatus.value = 'success'
  setTimeout(() => {
    uploading.value = false
    ElMessage.success('上传成功，文档已进入解析队列')
    fetchDocuments()
  }, 600)
}

const onUploadError = (err) => {
  uploadStatus.value = 'exception'
  setTimeout(() => {
    uploading.value = false
    const msg = err?.message || '上传失败，请重试'
    ElMessage.error(msg.includes('403') ? '登录状态已失效或无权限' : '上传失败，请重试')
  }, 1000)
}

const handleDelete = async (docId) => {
  try {
    await knowledgeApi.deleteDocument(appId, docId)
    ElMessage.success('已删除')
    fetchDocuments()
  } catch (e) { /* 拦截器已处理 */ }
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) { size /= 1024; i++ }
  return size.toFixed(1) + ' ' + units[i]
}

const statusText = (s) => ['待解析', '解析中', '已完成', '失败'][s] || '未知'
const statusType = (s) => ['info', 'warning', 'success', 'danger'][s] || 'info'
</script>

<style scoped>
.tips { margin-bottom: 16px; }

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 上传进度条弹窗样式 */
.upload-progress-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
}

.progress-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.progress-text {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
