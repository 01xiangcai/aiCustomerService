<template>
  <div class="records-page">
    <div class="page-header">
      <div>
        <h2>对话记录</h2>
        <p class="subtitle">查看用户与 AI 的对话历史</p>
      </div>
    </div>

    <div class="records-layout">
      <!-- 左侧会话列表 -->
      <div class="sessions-panel card">
        <div class="panel-header">
          <span>会话列表</span>
          <el-tag type="info" size="small">{{ total }} 条</el-tag>
        </div>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="5" animated />
        </div>

        <el-empty v-else-if="!sessions.length" description="暂无对话记录" :image-size="80" />

        <div v-else class="session-list">
          <div
            class="session-item"
            v-for="session in sessions"
            :key="session.id"
            @click="viewSession(session)"
            :class="{ active: activeSession?.id === session.id }"
          >
            <el-avatar :size="36" class="user-avatar">
              {{ (session.userIdentifier || '匿名').charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="session-info">
              <span class="session-name">{{ session.userIdentifier || '匿名用户' }}</span>
              <span class="session-meta">{{ session.messageCount }} 条消息 · {{ formatTime(session.lastActiveAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            small
            @current-change="loadSessions"
          />
        </div>
      </div>

      <!-- 右侧消息详情 -->
      <div class="messages-panel card" v-if="activeSession">
        <div class="panel-header">
          <span>对话详情</span>
          <span class="session-user-tag">{{ activeSession.userIdentifier || '匿名用户' }}</span>
        </div>

        <div v-if="msgLoading" class="loading-wrap">
          <el-skeleton :rows="6" animated />
        </div>

        <div v-else class="chat-messages">
          <div
            class="chat-msg"
            v-for="msg in messages"
            :key="msg.id"
            :class="msg.role"
          >
            <div class="msg-bubble">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>

      <!-- 未选择时的占位 -->
      <div class="messages-panel card empty-panel" v-else>
        <el-empty description="点击左侧会话查看对话详情" :image-size="100" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const appId = route.params.id

const sessions = ref([])
const messages = ref([])
const activeSession = ref(null)
const loading = ref(false)
const msgLoading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20

// 加载会话列表
const loadSessions = async (page = 1) => {
  loading.value = true
  try {
    const res = await request.get(`/api/records/${appId}/sessions`, {
      params: { page, size: pageSize }
    })
    sessions.value = res.data?.records || []
    total.value = Number(res.data?.total) || 0
  } catch (e) {
    console.error('加载会话列表失败', e)
  } finally {
    loading.value = false
  }
}

// 查看某会话的消息记录
const viewSession = async (session) => {
  activeSession.value = session
  msgLoading.value = true
  try {
    const msgs = await request.get(`/api/records/sessions/${session.id}/messages`)
    messages.value = msgs.data || []
  } catch (e) {
    console.error('加载消息失败', e)
  } finally {
    msgLoading.value = false
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.records-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  height: calc(100vh - 180px);
}

.sessions-panel,
.messages-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  flex-shrink: 0;
}

.session-user-tag {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 400;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
}

.session-item:hover,
.session-item.active {
  background: var(--primary-bg);
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}

.session-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.session-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.pagination-wrap {
  padding: 12px;
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}

/* 消息面板 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-msg {
  max-width: 80%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-msg.user {
  align-self: flex-end;
}

.chat-msg.assistant {
  align-self: flex-start;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-msg.user .msg-bubble {
  background: var(--primary);
  color: white;
  border-bottom-right-radius: 4px;
}

.chat-msg.assistant .msg-bubble {
  background: var(--bg-main);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: var(--text-muted);
  padding: 0 4px;
}

.chat-msg.user .msg-time {
  text-align: right;
}

.empty-panel {
  align-items: center;
  justify-content: center;
}

.loading-wrap {
  padding: 16px;
}
</style>
