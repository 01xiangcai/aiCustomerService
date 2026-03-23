<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h2>控制台</h2>
        <p class="subtitle">数据概览与快捷操作</p>
      </div>
      <el-select v-model="selectedAppId" placeholder="选择机器人" size="large" style="width: 200px" @change="fetchData">
        <el-option v-for="app in apps" :key="app.id" :label="app.appName" :value="app.id" />
      </el-select>
    </div>

    <!-- 数据卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="item in statCards" :key="item.label">
        <div class="stat-icon" :style="{ background: item.gradient }">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts-row" v-if="selectedAppId">
      <div class="card chart-card">
        <h3>消息量趋势（近 7 天）</h3>
        <div class="chart-container" ref="chartRef"></div>
      </div>

      <div class="card quick-actions">
        <h3>快捷操作</h3>
        <div class="action-list">
          <div class="action-item" @click="$router.push('/apps')">
            <el-icon :size="20"><Monitor /></el-icon>
            <span>管理机器人</span>
          </div>
          <div class="action-item" v-if="selectedAppId" @click="$router.push(`/apps/${selectedAppId}/knowledge`)">
            <el-icon :size="20"><Document /></el-icon>
            <span>知识库管理</span>
          </div>
          <div class="action-item" v-if="selectedAppId" @click="$router.push(`/apps/${selectedAppId}/records`)">
            <el-icon :size="20"><ChatDotRound /></el-icon>
            <span>对话记录</span>
          </div>
          <div class="action-item" @click="$router.push('/settings')">
            <el-icon :size="20"><Setting /></el-icon>
            <span>账号设置</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!apps.length" class="empty-state card">
      <el-icon :size="48" color="#94a3b8"><Monitor /></el-icon>
      <h3>还没有创建机器人</h3>
      <p>创建你的第一个 AI 客服机器人，开始使用智能客服能力</p>
      <el-button type="primary" @click="$router.push('/apps')">
        <el-icon><Plus /></el-icon>
        创建机器人
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { appApi, statsApi } from '@/api'
import * as echarts from 'echarts'

const apps = ref([])
const selectedAppId = ref(null)
const overview = ref({})
const trend = ref([])
const chartRef = ref(null)
let chartInstance = null

const statCards = computed(() => [
  { label: '今日对话', value: overview.value.todaySessions || 0, icon: 'ChatDotRound', gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' },
  { label: '今日消息', value: overview.value.todayMessages || 0, icon: 'ChatLineSquare', gradient: 'linear-gradient(135deg, #3b82f6, #60a5fa)' },
  { label: '总对话数', value: overview.value.totalSessions || 0, icon: 'DataAnalysis', gradient: 'linear-gradient(135deg, #10b981, #34d399)' },
  { label: 'AI 解答率', value: (overview.value.resolveRate || 0) + '%', icon: 'TrendCharts', gradient: 'linear-gradient(135deg, #f59e0b, #fbbf24)' }
])

onMounted(async () => {
  try {
    const res = await appApi.list({ page: 1, size: 100 })
    apps.value = res.data.records || []
    if (apps.value.length > 0) {
      selectedAppId.value = apps.value[0].id
      await fetchData()
    }
  } catch (e) { /* 拦截器已处理 */ }
})

const fetchData = async () => {
  if (!selectedAppId.value) return
  try {
    const [overviewRes, trendRes] = await Promise.all([
      statsApi.overview(selectedAppId.value),
      statsApi.trend(selectedAppId.value, 7)
    ])
    overview.value = overviewRes.data || {}
    trend.value = trendRes.data || []
    await nextTick()
    renderChart()
  } catch (e) { /* 拦截器已处理 */ }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.value.map(i => i.date?.substring(5) || ''),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#94a3b8' }
    },
    series: [{
      data: trend.value.map(i => i.count || 0),
      type: 'line',
      smooth: true,
      symbolSize: 6,
      lineStyle: { color: '#6366f1', width: 3 },
      itemStyle: { color: '#6366f1' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(99, 102, 241, 0.15)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0)' }
        ])
      }
    }]
  })
}

// 响应窗口大小变化
window.addEventListener('resize', () => chartInstance?.resize())
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.chart-card h3,
.quick-actions h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.chart-container {
  height: 280px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
  font-size: 14px;
}

.action-item:hover {
  background: var(--primary-bg);
  color: var(--primary);
}

.empty-state {
  text-align: center;
  padding: 60px 24px;
}

.empty-state h3 {
  margin: 16px 0 8px;
  color: var(--text-primary);
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 20px;
  font-size: 14px;
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}
</style>
