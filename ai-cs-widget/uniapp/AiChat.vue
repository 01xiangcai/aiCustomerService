<template>
  <view class="ai-chat-container">
    <!-- 头部预留（由于微信小程序自带顶部 NavigationBar，故无需自己写头部，在 onLoad 动态改变 NavigationBar 即可） -->

    <!-- 聊天记录区 -->
    <scroll-view class="chat-messages" scroll-y :scroll-into-view="scrollTopId" scroll-with-animation>
      <view class="message-list">
        <block v-for="(msg, index) in messages" :key="index">
          <view :class="['message-item', msg.role]" :id="'msg-' + index">
            <!-- 头像区 -->
            <image v-if="msg.role === 'assistant'" class="avatar" src="/static/ai-avatar.png" />
            
            <!-- 气泡内容区 -->
            <view class="message-bubble" :style="getBubbleStyle(msg.role)">
              <text v-if="msg.role === 'user'">{{ msg.content }}</text>
              <!-- 推荐在小程序中安装 mp-html 后，把下面这行解开以代替 text，将获得极其优美的 Markdown 渲染！ -->
              <!-- <mp-html v-if="msg.role === 'assistant'" :content="msg.content" :markdown="true"></mp-html> -->
              <text v-else>{{ msg.content }}</text>
            </view>
            
            <image v-if="msg.role === 'user'" class="avatar avatar-right" src="/static/user-avatar.png" />
          </view>
        </block>

        <!-- 输入状态提示 -->
        <view class="message-item assistant typing" v-if="isTyping" :id="'msg-typing'">
          <image class="avatar" src="/static/ai-avatar.png" />
          <view class="message-bubble">
            <text>正在思考中...</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部输入区 -->
    <view class="chat-input-area">
      <view class="input-wrapper">
        <textarea 
          class="chat-textarea" 
          v-model="inputText" 
          placeholder="请输入你的问题..." 
          :auto-height="true"
          :maxlength="500"
          :cursor-spacing="20"
          :show-confirm-bar="false"
        />
        <button class="send-btn" :style="{ backgroundColor: themeColor }" @tap="sendMessage" :disabled="!inputText.trim() || isTyping">
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

// ======================= 配置区域 =======================
const SERVER_URL = 'http://localhost:9900' // 【重点】你的后端地址
const APP_KEY = 'ak_LDG0En1pIm8TLZQy1D3LRY5l' // 【重点】后台复制的机器人凭证
// ========================================================

const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const scrollTopId = ref('')

// 动态主题和初始化参数
const appName = ref('AI 客服助手')
const themeColor = ref('#4f46e5')
const welcomeMsg = ref('你好！我是 AI 客服助手，有什么可以帮你的？')

// 初始化用户标识（利用小程序的 Storage 实现长期记忆）
let sessionId = uni.getStorageSync('aics_session_id')
if (!sessionId) {
  // 正式项目建议直接用 \`wx_user_\${userInfo.id}\`，这样客服可以跨设备认出用户
  sessionId = 'wx_' + Math.random().toString(36).substring(2, 10)
  uni.setStorageSync('aics_session_id', sessionId)
}

onMounted(() => {
  initAppConfig()
})

// 加载机器人专属配置
const initAppConfig = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/info`,
    method: 'GET',
    success: (res) => {
      if (res.data && res.data.code === 200) {
        const info = res.data.data
        appName.value = info.appName || appName.value
        themeColor.value = info.themeColor || themeColor.value
        welcomeMsg.value = info.welcomeMsg || welcomeMsg.value

        // 动态改写微信原生的标题栏和顶栏底色，极其高端
        uni.setNavigationBarTitle({ title: appName.value })
        uni.setNavigationBarColor({ 
          backgroundColor: themeColor.value, 
          frontColor: '#ffffff' 
        })
      }
      loadHistory()
    },
    fail: () => {
      // 网络失败，使用保底欢迎语
      loadHistory()
    }
  })
}

// 拉取历史三十条聊天漫游
const loadHistory = () => {
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/history?sessionId=${sessionId}&limit=30`,
    method: 'GET',
    success: (res) => {
      if (res.data && res.data.code === 200 && res.data.data.length > 0) {
        messages.value = res.data.data.map(m => ({
          role: m.role,
          content: m.content
        }))
      } else {
        // 第一天使用的新客，直接抛出欢迎语
        if(welcomeMsg.value) {
           messages.value.push({ role: 'assistant', content: welcomeMsg.value })
        }
      }
      scrollToBottom()
    }
  })
}

// 用户点击发送
const sendMessage = () => {
  if (!inputText.value.trim() || isTyping.value) return

  const userContent = inputText.value
  messages.value.push({ role: 'user', content: userContent })
  inputText.value = ''
  isTyping.value = true
  scrollToBottom()

  // 发送给后端非流式直接获取答案 (解决小程序对于流式连接不标准的问题)
  uni.request({
    url: `${SERVER_URL}/open/chat/${APP_KEY}/message`,
    method: 'POST',
    data: {
      message: userContent,
      sessionId: sessionId,
      userId: sessionId // 在真实业务中填入你的数据库 UserID
    },
    success: (res) => {
      if (res.data && res.data.code === 200) {
        messages.value.push({ role: 'assistant', content: res.data.data })
      } else {
        messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
      }
    },
    fail: () => {
      messages.value.push({ role: 'assistant', content: '网络错误，请检查您的网络连接。' })
    },
    complete: () => {
      isTyping.value = false
      scrollToBottom()
    }
  })
}

// 自动滚动到底部动画
const scrollToBottom = () => {
  nextTick(() => {
    scrollTopId.value = 'msg-' + (messages.value.length - 1)
    if(isTyping.value) {
       scrollTopId.value = 'msg-typing'
    }
  })
}

// 给不同角色的气泡赋予底色
const getBubbleStyle = (role) => {
  if (role === 'user') {
    return `background-color: ${themeColor.value}; color: white;`
  }
  return 'background-color: #ffffff; color: #333333; border: 1px solid #eeeeee;'
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f7f8fa;
}

.chat-messages {
  flex: 1;
  padding: 20rpx;
  /* 解决微信底部安全距离遮挡问题 */
  height: calc(100vh - 180rpx - env(safe-area-inset-bottom));
}

.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 30rpx;
}

/* AI 的布局 */
.message-item.assistant {
  flex-direction: row;
}

/* 用户的布局 */
.message-item.user {
  flex-direction: row;
  justify-content: flex-end;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #e5e7eb;
}

.avatar-right {
  margin-right: 0;
  margin-left: 20rpx;
}

.message-bubble {
  max-width: 65%;
  padding: 20rpx 28rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-break: break-all;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.02);
}

.message-item.assistant .message-bubble {
  border-top-left-radius: 4rpx;
}

.message-item.user .message-bubble {
  border-top-right-radius: 4rpx;
}

/* 底部输入框 */
.chat-input-area {
  padding: 20rpx 30rpx 40rpx 30rpx; /* 预留安全底部 */
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #ffffff;
  border-top: 1px solid #f1f5f9;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
}

.chat-textarea {
  flex: 1;
  background-color: #f8fafc;
  border-radius: 16rpx;
  padding: 20rpx;
  font-size: 28rpx;
  min-height: 48rpx;
  max-height: 180rpx;
}

.send-btn {
  width: 130rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 16rpx;
  color: white;
  font-size: 28rpx;
  margin: 0;
  padding: 0;
  border: none;
  font-weight: 500;
  padding-bottom: 0 !important;
}

.send-btn[disabled] {
  opacity: 0.5;
  background-color: #cbd5e1 !important;
  color: #ffffff !important;
}
</style>
