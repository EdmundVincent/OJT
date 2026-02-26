<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>要員数</span>
            </div>
          </template>
          <div class="stat-value">{{ employees.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>プロジェクト数</span>
            </div>
          </template>
          <div class="stat-value">{{ projects.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>稼働中PJ</span>
            </div>
          </template>
          <div class="stat-value">{{ activeProjectsCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>平均稼働率</span>
            </div>
          </template>
          <div class="stat-value">{{ avgUtilization }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>プロジェクト状況分布</span>
            </div>
          </template>
          <div ref="statusChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>部門別要員分布</span>
            </div>
          </template>
          <div ref="deptChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>月別売上・コスト推移（万）</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="height: 410px; overflow-y: auto;">
          <template #header>
            <div class="card-header">
              <span style="color: #f56c6c; font-weight: bold;">リスク・アラート</span>
            </div>
          </template>
          <div v-if="risks.length === 0" style="text-align: center; color: #909399; margin-top: 100px;">
            現在リスクは検出されていません
          </div>
          <div v-else>
            <div v-for="(risk, index) in risks" :key="index" class="risk-item">
              <el-alert
                :title="risk.title"
                :type="risk.type"
                :description="risk.description"
                show-icon
                :closable="false"
                style="margin-bottom: 10px"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getEmployees } from '@/api/employee'
import { getProjects } from '@/api/project'
import { getAssignments } from '@/api/assignment'

const employees = ref([])
const projects = ref([])
const assignments = ref([])
const statusChartRef = ref(null)
const deptChartRef = ref(null)
const trendChartRef = ref(null)

const activeProjectsCount = computed(() => {
  return projects.value.filter(p => (p.status || '進行中') === '進行中').length
})

const statusDistribution = computed(() => {
  const dist = {}
  projects.value.forEach(p => {
    const status = p.status || '進行中'
    dist[status] = (dist[status] || 0) + 1
  })
  return dist
})

const avgUtilization = computed(() => {
  if (employees.value.length === 0) return 0
  const currentYm = Number(getCurrentYm())
  const activeAssignments = assignments.value.filter(a => a.startYm <= currentYm && a.endYm >= currentYm)
  const totalRatio = activeAssignments.reduce((sum, a) => sum + Number(a.allocation_ratio), 0)
  return Math.round((totalRatio / employees.value.length) * 100)
})

const risks = computed(() => {
  const list = []
  const currentYm = Number(getCurrentYm())
  
  // 1. Overloaded staff
  const empUtilization = {}
  assignments.value.filter(a => a.startYm <= currentYm && a.endYm >= currentYm).forEach(a => {
    empUtilization[a.employeeId] = (empUtilization[a.employeeId] || 0) + Number(a.allocation_ratio)
  })
  Object.entries(empUtilization).forEach(([id, ratio]) => {
    if (ratio > 1.0) {
      const emp = employees.value.find(e => String(e.id) === id)
      list.push({
        title: `過剰稼働: ${emp?.name || id}`,
        type: 'error',
        description: `現在の稼働率が ${(ratio * 100).toFixed(0)}% に達しています。`
      })
    }
  })

  // 2. Projects ending soon (within 2 months)
  const nextMonth = (currentYm % 100 === 12) ? (currentYm + 89) : (currentYm + 1)
  const next2Month = (nextMonth % 100 === 12) ? (nextMonth + 89) : (nextMonth + 1)
  
  projects.value.filter(p => (p.status || '進行中') === '進行中' && p.endYm && p.endYm <= next2Month).forEach(p => {
    list.push({
      title: `間もなく終了: ${p.name}`,
      type: 'warning',
      description: `終了予定年月: ${p.endYm}`
    })
  })

  // 3. High priority projects without PM
  projects.value.filter(p => p.priority === '高' && !p.pmId).forEach(p => {
    list.push({
      title: `PM未設定: ${p.name}`,
      type: 'error',
      description: '高優先度プロジェクトですが、責任者が設定されていません。'
    })
  })

  return list
})

const getCurrentYm = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  return `${y}${String(m).padStart(2, '0')}`
}

const fetchStats = async () => {
  const [eRes, pRes, aRes] = await Promise.all([
    getEmployees(),
    getProjects(),
    getAssignments()
  ])
  employees.value = eRes.data
  projects.value = pRes.data
  assignments.value = aRes.data
  initCharts()
}

const initCharts = () => {
  if (!window.echarts) return

  // Status Chart
  const statusChart = window.echarts.init(statusChartRef.value)
  const statusData = Object.entries(statusDistribution.value).map(([name, value]) => ({ name, value }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: statusData,
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      }
    }]
  })

  // Dept Chart
  const deptChart = window.echarts.init(deptChartRef.value)
  const deptDist = {}
  employees.value.forEach(e => {
    deptDist[e.department] = (deptDist[e.department] || 0) + 1
  })
  deptChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(deptDist) },
    yAxis: { type: 'value' },
    series: [{
      data: Object.values(deptDist),
      type: 'bar',
      itemStyle: { color: '#409EFF' }
    }]
  })

  // Trend Chart
  const trendChart = window.echarts.init(trendChartRef.value)
  const months = []
  const currentYm = Number(getCurrentYm())
  let tempYm = currentYm
  for (let i = 0; i < 6; i++) {
    months.unshift(tempYm)
    tempYm = (tempYm % 100 === 1) ? (tempYm - 89) : (tempYm - 1)
  }

  const revenueData = months.map(m => {
    return projects.value
      .filter(p => p.startYm <= m && (p.endYm >= m || !p.endYm))
      .reduce((sum, p) => sum + (p.revenue ? Number(p.revenue) / 10000 : 0) / 6, 0) // Simplified: revenue spread over 6 months
      .toFixed(0)
  })

  const costData = months.map(m => {
    return assignments.value
      .filter(a => a.startYm <= m && a.endYm >= m)
      .reduce((sum, a) => sum + (Number(a.unit_price) * Number(a.allocation_ratio) / 10000), 0)
      .toFixed(0)
  })

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['売上（概算）', 'コスト'] },
    xAxis: { type: 'category', data: months.map(m => String(m).substring(4) + '月') },
    yAxis: { type: 'value' },
    series: [
      { name: '売上（概算）', type: 'line', data: revenueData, smooth: true, itemStyle: { color: '#67C23A' } },
      { name: 'コスト', type: 'line', data: costData, smooth: true, itemStyle: { color: '#F56C6C' } }
    ]
  })
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.home-container {
  padding: 10px;
}
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-bar {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.status-label {
  width: 80px;
  font-size: 14px;
}
.el-progress {
  flex: 1;
  margin: 0 10px;
}
.status-count {
  width: 30px;
  text-align: right;
  font-size: 14px;
}
</style>
