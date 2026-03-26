<template>
  <view class="ai-widget-wrapper">
    <!-- 1. 悬浮气泡按钮 -->
    <view 
      v-if="!showChat"
      class="ai-bubble"
      :style="{ backgroundColor: themeColor }"
      @tap="toggleChat"
    >
      <text class="i-chat">💬</text>
    </view>

    <!-- 2. 聊天浮窗 (使用弹窗组件模拟 Widget 体验) -->
    <view v-if="showChat" class="ai-chat-window" :class="{ 'expanded': isExpanded }">
      <!-- 头部 -->
      <view class="ai-header" :style="{ backgroundColor: themeColor }">
        <text class="title">{{ appName || 'AI 客服' }}</text>
        <view class="controls">
          <text class="btn-icon" @tap="toggleExpand">{{ isExpanded ? '❐' : '⬜' }}</text>
          <text class="btn-icon close" @tap="toggleChat">✕</text>
        </view>
      </view>

      <!-- 消息列表 -->
      <scroll-view class="ai-messages" scroll-y :scroll-into-view="lastId" scroll-with-animation>
        <view class="msg-list">
          <view v-for="(msg, index) in messages" :key="index" :class="['msg-item', msg.role]" :id="'msg-' + index">
            <view class="bubble" :style="getBubbleStyle(msg.role)">
              <text>{{ msg.content }}</text>
            </view>
          </view>
          <view v-if="isTyping" class="msg-item assistant" id="msg-typing">
            <view class="bubble">正在思考...</view>
          </view>
        </view>
      </scroll-view>

      <!-- 输入框 -->
      <view class="ai-input-bar">
        <textarea 
          class="input" 
          v-model="inputText" 
          placeholder="输入问题..." 
          fixed 
          :auto-height="true" 
          maxlength="300" 
        />
        <text class="send" :style="{ color: themeColor }" @tap="doSend">发送</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

/**
 * 【开箱即用】UniApp 仿 Web Widget 悬浮客服组件
 * 使用方法：
 * 1. 拷贝此文件到 components/AiChatWidget.vue
 * 2. 在 main.js 全局注册或在页面中直接引入 <AiChatWidget />
 */

// --- 配置配区 ---
const SERVER_URL = 'http://localhost:9900' 
const APP_KEY = 'ak_LDG0En1pIm8TLZQy1D3LRY5l' 
// --------------

const showChat = ref(false)
const isExpanded = ref(false)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const lastId = ref('')

const appName = ref('')
const themeColor = ref('#0f172a')
const welcomeMsg = ref('')

let sessionId = uni.getStorageSync('aics_session_id') || ('wx_' + Math.random().toString(36).slice(2))
uni.setStorageSync('aics_session_id', sessionId)

onMounted(() => {
  fetchConfig()
})

const fetchConfig = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/info`,
    success: (res) => {
      if (res.data.code === 200) {
        const d = res.data.data
        appName.value = d.appName
        themeColor.value = d.themeColor
        welcomeMsg.value = d.welcomeMsg
      }
      loadHistory()
    }
  })
}

const loadHistory = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/history?sessionId=${sessionId}&limit=20`,
    success: (res) => {
      if (res.data.data && res.data.data.length > 0) {
        messages.value = res.data.data.map(m => ({ role: m.role, content: m.content }))
      } else {
        messages.value = [{ role: 'assistant', content: welcomeMsg.value || '你好！' }]
      }
      goBottom()
    }
  })
}

const toggleChat = () => { showChat.value = !showChat.value; if(showChat.value) goBottom(); }
const toggleExpand = () => isExpanded.value = !isExpanded.value

const doSend = () => {
  if(!inputText.value.trim() || isTyping.value) return
  const txt = inputText.value
  messages.value.push({ role: 'user', content: txt })
  inputText.value = ''
  isTyping.value = true
  goBottom()

  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/message`,
    method: 'POST',
    data: { message: txt, sessionId, userId: sessionId },
    success: (res) => {
      messages.value.push({ role: 'assistant', content: res.data.data || '连接超时' })
    },
    complete: () => {
      isTyping.value = false
      goBottom()
    }
  })
}

const goBottom = () => {
  nextTick(() => { lastId.value = 'msg-' + (messages.value.length - 1) })
}

const getBubbleStyle = (role) => {
  return role === 'user' ? `background:${themeColor.value};color:#fff` : `background:#fff;color:#333;border:1px solid #eee`
}
</script>

<style scoped>
.ai-widget-wrapper { position: fixed; right: 30rpx; bottom: 100rpx; z-index: 999; }
.ai-bubble { 
  width: 100rpx; height: 100rpx; border-radius: 50%; display: flex; align-items: center; 
  justify-content: center; box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.2); 
}
.i-chat { font-size: 50rpx; }

.ai-chat-window {
  position: fixed; right: 30rpx; bottom: 160rpx; width: 600rpx; height: 800rpx;
  background: #f8fafc; border-radius: 20rpx; display: flex; flex-direction: column;
  box-shadow: 0 10rpx 50rpx rgba(0,0,0,0.15); overflow: hidden; transition: all 0.3s;
}
.ai-chat-window.expanded { width: 90vw; height: 85vh; right: 5vw; bottom: 5vh; }

.ai-header { padding: 20rpx 30rpx; display: flex; justify-content: space-between; align-items: center; color: #fff; }
.ai-header .title { font-size: 28rpx; font-weight: bold; }
.controls { display: flex; gap: 20rpx; }
.btn-icon { padding: 10rpx; font-size: 32rpx; cursor: pointer; }

.ai-messages { flex: 1; padding: 20rpx; box-sizing: border-box; }
.msg-list { display: flex; flex-direction: column; gap: 20rpx; }
.msg-item { display: flex; width: 100%; }
.msg-item.user { justify-content: flex-end; }
.bubble { 
  max-width: 80%; padding: 16rpx 24rpx; border-radius: 16rpx; font-size: 26rpx; 
  word-break: break-all; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02);
}

.ai-input-bar { 
  padding: 20rpx; background: #fff; border-top: 1px solid #eee; 
  display: flex; align-items: flex-end; gap: 15rpx;
}
.input { flex: 1; background: #f1f5f9; border-radius: 10rpx; padding: 15rpx; font-size: 26rpx; min-height: 40rpx; }
.send { font-size: 28rpx; font-weight: bold; padding: 10rpx; }
</style>
