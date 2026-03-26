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
          <el-button size="small" type="primary" plain @click="showIntegration(app)">
            <el-icon><Link /></el-icon> 接入
          </el-button>
          <el-button size="small" @click="editApp(app)">
            <el-icon><Edit /></el-icon> 配置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 接入指南对话框 -->
    <el-dialog v-model="showGuide" title="集成指南" width="720px" custom-class="guide-dialog">
      <el-tabs v-model="activeGuideTab">
        <el-tab-pane label="网页端接入 (Widget)" name="web">
          <div class="guide-content">
            <h4 class="step-title">1. 引入脚本</h4>
            <p class="guide-desc">在您的 HTML 页面 <code>&lt;/body&gt;</code> 标签结束前，添加以下代码即可：</p>
            <div class="code-block">
              <pre><code>{{ webCode }}</code></pre>
              <el-button class="copy-btn" type="primary" size="small" @click="copyText(webCode)">复制代码</el-button>
            </div>
            <h4 class="step-title">2. 自动挂载</h4>
            <p class="guide-desc">保存后刷新您的网页，右下角将自动出现智能客服气泡。无需额外编写 UI。</p>
            <div class="guide-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>提示：请确保后端地址 <code>{{ currentApp?.serverUrl || 'http://localhost:9900' }}</code> 可被公网访问。</span>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="小程序 / UniApp (双方案)" name="uniapp">
          <div class="guide-content">
            <el-radio-group v-model="uniappType" style="margin-bottom: 20px;">
              <el-radio-button label="webview">WebView 模式 (推荐)</el-radio-button>
              <el-radio-button label="widget">悬浮球模式 (原生感)</el-radio-button>
            </el-radio-group>

            <div v-if="uniappType === 'webview'">
              <h4 class="step-title">1. 新建 WebView 页面</h4>
              <p class="guide-desc">在小程序中新建一个页面（例如 <code>pages/chat/index</code>），在模板中只需加入一行代码：</p>
              <div class="code-block">
                <pre><code>{{ uniappWebViewCode }}</code></pre>
                <el-button class="copy-btn" type="primary" size="small" @click="copyText(uniappWebViewCode)">复制代码</el-button>
              </div>
              
              <h4 class="step-title">方案优势</h4>
              <p class="guide-desc">
                ● <b>零文件拷贝</b>：逻辑全在云端，项目不冗余。<br/>
                ● <b>云端同步</b>：修改后台配置后，所有小程序立即生效。<br/>
                ● <b>快速合规</b>：适合作为独立的“在线客服”功能页。
              </p>
            </div>

            <div v-else>
              <h4 class="step-title">1. 获取核心逻辑 (Composable)</h4>
              <p class="guide-desc">下载最新版的 <code>useAiChat.js</code> 文件并放入您的 <code>composables</code> 目录中。</p>
              
              <h4 class="step-title">2. 在页面的 &lt;script setup&gt; 中引入</h4>
              <div class="code-block" style="background: #282c34">
                <pre><code>import { useAiChat } from '@/composables/useAiChat';
const {
  csOpen, csAppName, csTheme, csMessages, csInput, csTyping, csScrollId,
  openCs, closeCs, resetCs, csSend
} = useAiChat('{{ currentApp?.serverUrl || "http://127.0.0.1:9900" }}', '{{ currentApp?.appKey }}');</code></pre>
                <el-button class="copy-btn" type="primary" size="small" @click="copyText('import { useAiChat } from \'@/composables/useAiChat\';\nconst {\n  csOpen, csAppName, csTheme, csMessages, csInput, csTyping, csScrollId,\n  openCs, closeCs, resetCs, csSend\n} = useAiChat(\'' + (currentApp?.serverUrl || 'http://127.0.0.1:9900') + '\', \'' + (currentApp?.appKey || '') + '\');')">复制代码</el-button>
              </div>

              <h4 class="step-title">3. 在页面的 &lt;template&gt; 内（.page 最末尾）粘贴 UI</h4>
              <p class="guide-desc">直接在页面中内联 UI 以避免小程序的 fixed 穿透问题，具体模板代码请参考官网文档。</p>

              <h4 class="step-title">方案优势</h4>
              <p class="guide-desc">
                ● <b>原生交互</b>：弹窗丝滑律动，打字机动画流畅。<br/>
                ● <b>高度解耦</b>：仅提供纯粹 JS 控制，界面您甚至可以随心所欲自由魔改！
              </p>
            </div>

            <div class="guide-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>注意：微信小程序需在后台配置<b>业务域名</b>为 <code>{{ serverHost }}</code></span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

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
const showGuide = ref(false)
const activeGuideTab = ref('web')
const uniappType = ref('webview')
const currentApp = ref(null)
const creating = ref(false)
const updating = ref(false)

const createForm = reactive({ appName: '' })
const editForm = reactive({ id: null, appName: '', welcomeMsg: '', fallbackMsg: '', modelType: '', themeColor: '' })

// 接入代码模板
const webCode = ref('')
const uniappWebViewCode = ref('')
const serverHost = ref('')

const showIntegration = (app) => {
  currentApp.value = app
  const serverUrl = window.location.origin.replace('5173', '9900') // 兼容开发环境
  serverHost.value = serverUrl.replace(/^https?:\/\//, '')
  
  webCode.value = `<!-- AI 客服组件开始 -->
<script src="${serverUrl}/widget/ai-cs-widget.js"><\/script>
<script>
  AiCS.init({
    appKey: '${app.appKey}',
    serverUrl: '${serverUrl}',
    title: '${app.appName}'
  });
<\/script>
<!-- AI 客服组件结束 -->`

  uniappWebViewCode.value = `<template>
  <web-view src="${serverUrl}/widget/h5-mobile.html?appKey=${app.appKey}"></web-view>
</template>`

  showGuide.value = true
}

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
  flex-wrap: wrap;
}

.guide-content {
  padding: 10px 0;
}

.step-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 16px 0 8px 0;
  display: flex;
  align-items: center;
}

.step-title::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 16px;
  background: var(--primary);
  margin-right: 8px;
  border-radius: 2px;
}

.guide-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.code-block {
  position: relative;
  background: #1e293b;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.code-block pre {
  margin: 0;
  color: #e2e8f0;
  font-family: 'Fira Code', 'Cascadia Code', monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
}

.copy-btn {
  position: absolute;
  top: 10px;
  right: 10px;
}

.guide-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  color: #0369a1;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
}

.guide-tip.success {
  background: #f0fdf4;
  border-color: #bbf7d0;
  color: #15803d;
}

.guide-tip code {
  background: #e0f2fe;
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
