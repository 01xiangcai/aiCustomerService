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
        :headers="{ Authorization: `Bearer ${token}` }"
        :before-upload="beforeUpload"
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

const route = useRoute()
const appId = route.params.id
const token = localStorage.getItem('token')
const documents = ref([])
const loading = ref(false)

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
  return true
}

const onUploadSuccess = () => {
  ElMessage.success('上传成功，正在解析中...')
  fetchDocuments()
}

const onUploadError = () => {
  ElMessage.error('上传失败，请重试')
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
</style>
