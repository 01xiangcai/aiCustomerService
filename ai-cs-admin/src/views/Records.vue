<template>
  <div class="records-page">
    <div class="page-header">
      <div>
        <h2>对话记录</h2>
        <p class="subtitle">查看用户与 AI 的对话历史</p>
      </div>
    </div>

    <div class="card">
      <el-empty v-if="!sessions.length" description="暂无对话记录" />

      <div v-else class="session-list">
        <div
          class="session-item"
          v-for="session in sessions"
          :key="session.id"
          @click="viewSession(session)"
          :class="{ active: activeSession?.id === session.id }"
        >
          <div class="session-user">
            <el-avatar :size="36" class="user-avatar">
              {{ (session.userIdentifier || '匿名').charAt(0) }}
            </el-avatar>
            <div class="session-info">
              <span class="session-name">{{ session.userIdentifier || '匿名用户' }}</span>
              <span class="session-meta">{{ session.messageCount }} 条消息 · {{ session.lastActiveAt }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话详情抽屉 -->
    <el-drawer v-model="showDetail" title="对话详情" size="480px">
      <div class="chat-messages">
        <div
          class="chat-msg"
          v-for="msg in messages"
          :key="msg.id"
          :class="msg.role"
        >
          <div class="msg-bubble">{{ msg.content }}</div>
          <div class="msg-time">{{ msg.createdAt }}</div>
        </div>
      </div>
    </el-drawer>
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
const showDetail = ref(false)

onMounted(async () => {
  // 暂时直接查消息表（后续可加会话列表接口）
  // 这里先用空数据占位
})

const viewSession = async (session) => {
  activeSession.value = session
  showDetail.value = true
  // 这里可以调用历史接口
}
</script>

<style scoped>
.session-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-item {
  padding: 14px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: var(--transition);
}

.session-item:hover,
.session-item.active {
  background: var(--primary-bg);
}

.session-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  font-weight: 600;
}

.session-name {
  display: block;
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.session-meta {
  font-size: 12px;
  color: var(--text-muted);
}

/* 对话消息 */
.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-msg {
  max-width: 85%;
}

.chat-msg.user {
  align-self: flex-end;
}

.chat-msg.assistant {
  align-self: flex-start;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
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
  margin-top: 4px;
  padding: 0 4px;
}

.chat-msg.user .msg-time {
  text-align: right;
}
</style>
