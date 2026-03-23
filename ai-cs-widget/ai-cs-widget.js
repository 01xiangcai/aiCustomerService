/**
 * AI 客服中间件 · 嵌入式聊天 Widget
 * 
 * 使用方式：
 * <script src="https://cdn.ai-cs.com/widget.min.js"></script>
 * <script>AiCS.init({ appKey: 'your-app-key' })</script>
 */
; (function () {
  'use strict';

  // ==================== 默认配置 ====================
  const DEFAULT_CONFIG = {
    appKey: '',
    serverUrl: '',
    themeColor: '#0f172a', /* 替换为极简直男/性冷淡高级黑 */
    position: 'bottom-right',
    welcomeMsg: '你好！我是 AI 智能客服，有什么可以帮助你的？',
    placeholder: '输入你的问题...',
    title: 'AI 智能客服',
    width: 380,
    height: 560,
    bubbleSize: 56
  };

  let config = {};
  let isOpen = false;
  let sessionId = '';
  let messages = [];

  // ==================== 初始化 ====================
  function init(userConfig) {
    config = Object.assign({}, DEFAULT_CONFIG, userConfig);

    if (!config.appKey) {
      console.error('[AiCS] appKey 是必填项');
      return;
    }

    // 自动检测 serverUrl
    if (!config.serverUrl) {
      const scriptEl = document.querySelector('script[src*="widget"]');
      if (scriptEl) {
        const url = new URL(scriptEl.src);
        config.serverUrl = url.origin;
      } else {
        config.serverUrl = window.location.origin;
      }
    }

    // 从 localStorage 复用或新建会话 ID，以 appKey 区分不同机器人
    const storageKey = 'aics_session_' + config.appKey;
    sessionId = localStorage.getItem(storageKey);
    if (!sessionId) {
      sessionId = 'ws_' + Math.random().toString(36).substring(2, 15);
      localStorage.setItem(storageKey, sessionId);
    }

    // 注入样式和 DOM
    injectStyles();
    createWidget();

    // 从后端加载历史消息，没有历史则显示欢迎语
    loadHistory();

    console.log('[AiCS] Widget 初始化完成，sessionId=' + sessionId);
  }

  // ==================== 注入样式 ====================
  function injectStyles() {
    const style = document.createElement('style');
    style.id = 'aics-widget-styles';
    style.textContent = `
/* 气泡按钮 */
      #aics-bubble {
        position: fixed;
        ${config.position.includes('right') ? 'right: 24px' : 'left: 24px'};
        ${config.position.includes('bottom') ? 'bottom: 24px' : 'top: 24px'};
        width: ${config.bubbleSize}px;
        height: ${config.bubbleSize}px;
        border-radius: 50%;
        background: ${config.themeColor};
        color: white;
        border: none;
        cursor: pointer;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 999998;
        transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
      }
      #aics-bubble:hover {
        transform: scale(1.05) translateY(-2px);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
      }
#aics-bubble svg {
        width: 24px;
        height: 24px;
        transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
      }
      #aics-bubble.open svg {
        transform: rotate(90deg) scale(0);
        opacity: 0;
      }
      #aics-bubble.open::after {
        content: '';
        position: absolute;
        width: 16px;
        height: 2px;
        background: white;
        border-radius: 2px;
        transform: rotate(45deg);
        transition: all 0.3s;
      }
      #aics-bubble.open::before {
        content: '';
        position: absolute;
        width: 16px;
        height: 2px;
        background: white;
        border-radius: 2px;
        transform: rotate(-45deg);
        transition: all 0.3s;
      }

      /* 对话窗口 */
      #aics-chat {
        position: fixed;
        ${config.position.includes('right') ? 'right: 24px' : 'left: 24px'};
        ${config.position.includes('bottom') ? 'bottom: 96px' : 'top: 96px'};
        width: ${config.width}px;
        height: ${config.height}px;
        background: #ffffff;
        border-radius: 12px;
        box-shadow: 0 24px 80px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.05), 0 0 0 1px rgba(0, 0, 0, 0.03);
        display: flex;
        flex-direction: column;
        overflow: hidden;
        z-index: 999999;
        opacity: 0;
        transform: translateY(12px) scale(0.98);
        pointer-events: none;
        transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
      }
      #aics-chat.open {
        opacity: 1;
        transform: translateY(0) scale(1);
        pointer-events: auto;
      }

      /* 头部 */
      .aics-header {
        background: ${config.themeColor};
        color: white;
        padding: 16px 20px;
        display: flex;
        align-items: center;
        gap: 12px;
      }
      .aics-header-icon {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .aics-header-icon svg { width: 18px; height: 18px; }
      .aics-header-title {
        font-size: 15px;
        font-weight: 500;
        letter-spacing: -0.01em;
      }
      .aics-header-status {
        font-size: 12px;
        opacity: 0.7;
        font-weight: 400;
      }

      /* 消息区 */
      .aics-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        display: flex;
        flex-direction: column;
        gap: 12px;
        background: #f8fafc;
      }
      .aics-messages::-webkit-scrollbar {
        width: 4px;
      }
      .aics-messages::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 2px;
      }

      /* 消息气泡 */
      .aics-msg {
        max-width: 82%;
        animation: aics-fadeIn 0.3s ease;
      }
      .aics-msg.user {
        align-self: flex-end;
      }
      .aics-msg.assistant {
        align-self: flex-start;
      }
      .aics-msg-bubble {
        padding: 10px 14px;
        border-radius: 14px;
        font-size: 14px;
        line-height: 1.6;
        word-break: break-word;
        white-space: pre-wrap;
      }
      .aics-msg.user .aics-msg-bubble {
        background: ${config.themeColor};
        color: white;
        border-bottom-right-radius: 4px;
      }
      .aics-msg.assistant .aics-msg-bubble {
        background: white;
        color: #1e293b;
        border: 1px solid #e2e8f0;
        border-bottom-left-radius: 4px;
        box-shadow: 0 1px 3px rgba(0,0,0,0.04);
      }
      @keyframes aics-fadeIn {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
      }

      /* 输入打字中的动画 */
      .aics-typing {
        display: flex;
        gap: 4px;
        padding: 12px 16px;
      }
      .aics-typing span {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #94a3b8;
        animation: aics-bounce 1.4s ease-in-out infinite;
      }
      .aics-typing span:nth-child(2) { animation-delay: 0.2s; }
      .aics-typing span:nth-child(3) { animation-delay: 0.4s; }
      @keyframes aics-bounce {
        0%, 60%, 100% { transform: translateY(0); }
        30% { transform: translateY(-6px); }
      }

      /* 输入区 */
      .aics-input-area {
        padding: 12px 16px;
        display: flex;
        gap: 8px;
        border-top: 1px solid #e2e8f0;
        background: white;
      }
      .aics-input {
        flex: 1;
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        padding: 10px 14px;
        font-size: 14px;
        outline: none;
        resize: none;
        font-family: inherit;
        line-height: 1.4;
        transition: border-color 0.2s;
        max-height: 80px;
      }
      .aics-input:focus {
        border-color: ${config.themeColor};
        box-shadow: 0 0 0 3px ${config.themeColor}15;
      }
      .aics-input::placeholder {
        color: #94a3b8;
      }
      .aics-send-btn {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        background: ${config.themeColor};
        color: white;
        border: none;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;
        flex-shrink: 0;
        align-self: flex-end;
      }
      .aics-send-btn:hover {
        filter: brightness(1.1);
        transform: scale(1.05);
      }
      .aics-send-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        transform: none;
      }
      .aics-send-btn svg {
        width: 18px;
        height: 18px;
      }

      /* Powered by */
      .aics-powered {
        text-align: center;
        padding: 6px;
        font-size: 11px;
        color: #94a3b8;
        background: white;
        border-top: 1px solid #f1f5f9;
      }
    `;
    document.head.appendChild(style);
  }

  // ==================== 创建 DOM ====================
  function createWidget() {
    // 气泡按钮
    const bubble = document.createElement('button');
    bubble.id = 'aics-bubble';
    bubble.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>`;
    bubble.onclick = toggleChat;
    document.body.appendChild(bubble);

    // 对话窗口
    const chat = document.createElement('div');
    chat.id = 'aics-chat';
    chat.innerHTML = `
      <div class="aics-header">
        <div class="aics-header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
        </div>
        <div>
          <div class="aics-header-title">${config.title}</div>
          <div class="aics-header-status">● 在线</div>
        </div>
      </div>
      <div class="aics-messages" id="aics-messages"></div>
      <div class="aics-input-area">
        <textarea class="aics-input" id="aics-input" placeholder="${config.placeholder}" rows="1"></textarea>
        <button class="aics-send-btn" id="aics-send">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
        </button>
      </div>
      <div class="aics-powered">Powered by AI 一二智能客服</div>
    `;
    document.body.appendChild(chat);

    // 绑定事件
    document.getElementById('aics-send').onclick = sendMessage;
    document.getElementById('aics-input').addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });

    // 自动调整输入框高度
    document.getElementById('aics-input').addEventListener('input', function () {
      this.style.height = 'auto';
      this.style.height = Math.min(this.scrollHeight, 80) + 'px';
    });

    // 渲染初始消息
    renderMessages();
  }

  // ==================== 加载历史消息 ====================
  async function loadHistory() {
    try {
      const res = await fetch(
        `${config.serverUrl}/open/chat/${config.appKey}/history?sessionId=${encodeURIComponent(sessionId)}&limit=50`
      );
      const result = await res.json();
      if (result.data && result.data.length > 0) {
        // 有历史消息，直接展示历史
        messages = result.data.map(m => ({ role: m.role, content: m.content }));
      } else {
        // 无历史消息，展示欢迎语
        if (config.welcomeMsg) {
          messages = [{ role: 'assistant', content: config.welcomeMsg }];
        }
      }
    } catch (e) {
      // 网络失败时也显示欢迎语
      if (config.welcomeMsg) {
        messages = [{ role: 'assistant', content: config.welcomeMsg }];
      }
    }
    renderMessages();
  }

  // ==================== 切换窗口 ====================
  function toggleChat() {
    isOpen = !isOpen;
    document.getElementById('aics-chat').classList.toggle('open', isOpen);
    document.getElementById('aics-bubble').classList.toggle('open', isOpen);
    if (isOpen) {
      setTimeout(() => document.getElementById('aics-input')?.focus(), 350);
    }
  }

  // ==================== 发送消息 ====================
  async function sendMessage() {
    const input = document.getElementById('aics-input');
    const text = input.value.trim();
    if (!text) return;

    // 添加用户消息
    messages.push({ role: 'user', content: text });
    input.value = '';
    input.style.height = 'auto';
    renderMessages();

    // 显示 typing 指示器
    showTyping(true);

    // 禁用发送按钮
    const sendBtn = document.getElementById('aics-send');
    sendBtn.disabled = true;

    try {
      const response = await fetch(`${config.serverUrl}/open/chat/${config.appKey}/message`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: text,
          sessionId: sessionId,
          userId: 'widget_user'
        })
      });

      const result = await response.json();
      showTyping(false);

      if (result.data) {
        messages.push({ role: 'assistant', content: result.data });
      } else {
        messages.push({ role: 'assistant', content: '抱歉，我暂时无法回复。' });
      }
    } catch (err) {
      showTyping(false);
      messages.push({ role: 'assistant', content: '网络异常，请稍后再试。' });
      console.error('[AiCS]', err);
    } finally {
      sendBtn.disabled = false;
      renderMessages();
    }
  }

  // ==================== 渲染消息 ====================
  function renderMessages() {
    const container = document.getElementById('aics-messages');
    if (!container) return;

    container.innerHTML = messages.map(msg => `
      <div class="aics-msg ${msg.role}">
        <div class="aics-msg-bubble">${escapeHtml(msg.content)}</div>
      </div>
    `).join('');

    // 滚动到底部
    requestAnimationFrame(() => {
      container.scrollTop = container.scrollHeight;
    });
  }

  // ==================== Typing 动画 ====================
  function showTyping(show) {
    const container = document.getElementById('aics-messages');
    const existing = container.querySelector('.aics-typing-wrapper');
    if (existing) existing.remove();

    if (show) {
      const typingEl = document.createElement('div');
      typingEl.className = 'aics-msg assistant aics-typing-wrapper';
      typingEl.innerHTML = `
        <div class="aics-msg-bubble">
          <div class="aics-typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      `;
      container.appendChild(typingEl);
      container.scrollTop = container.scrollHeight;
    }
  }

  // ==================== 工具函数 ====================
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function adjustColor(hex, amount) {
    hex = hex.replace('#', '');
    const num = parseInt(hex, 16);
    let r = Math.min(255, (num >> 16) + amount);
    let g = Math.min(255, ((num >> 8) & 0x00FF) + amount);
    let b = Math.min(255, (num & 0x0000FF) + amount);
    return '#' + (1 << 24 | r << 16 | g << 8 | b).toString(16).slice(1);
  }

  // ==================== 暴露全局 API ====================
  window.AiCS = {
    init: init,
    open: function () { if (!isOpen) toggleChat(); },
    close: function () { if (isOpen) toggleChat(); },
    sendMessage: function (text) {
      document.getElementById('aics-input').value = text;
      sendMessage();
    }
  };

})();
