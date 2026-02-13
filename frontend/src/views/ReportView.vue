<template>
  <div>
    <el-tabs v-model="activeTab">
      <el-tab-pane 
        v-for="tab in reportTabs" 
        :key="tab.codeId" 
        :label="tab.codeName" 
        :name="tab.codeId" 
      />
    </el-tabs>
    
    <!-- 検索条件 -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="期間">
        <year-month-picker v-model:start="searchParams.startYm" 
                          v-model:end="searchParams.endYm" />
      </el-form-item>
      <el-form-item v-if="activeTab === 'RESOURCE_ALLOCATION'" label="稼働率範囲">
        <el-input-number v-model="searchParams.minRatio" :min="0" :max="2" :step="0.1" />
        <span> ~ </span>
        <el-input-number v-model="searchParams.maxRatio" :min="0" :max="2" :step="0.1" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchReport">検索</el-button>
        <el-button v-if="authStore.isAdmin" type="warning" @click="handleExport">CSVエクスポート</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 動的テーブル -->
    <el-table :data="reportData" v-loading="loading" border style="width: 100%">
      <el-table-column 
        v-for="col in dynamicColumns" 
        :key="col.prop"
        :prop="col.prop" 
        :label="col.label" 
      />
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getResourceAllocation, getBudgetMismatch, getProjectCost, exportResourceAllocation, exportBudgetMismatch, exportProjectCost } from '@/api/report'
import { getCodeMasters } from '@/api/code-master'
import YearMonthPicker from '@/components/YearMonthPicker.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('')
const reportTabs = ref([])
const reportData = ref([])
const loading = ref(false)
const searchParams = ref({
  startYm: '202501',
  endYm: '202512',
  minRatio: 0,
  maxRatio: 1.5
})

onMounted(async () => {
  try {
    const { data } = await getCodeMasters('REPORT_TYPE')
    reportTabs.value = data
    if (data.length > 0) {
      activeTab.value = data[0].codeId
      fetchReport()
    }
  } catch (e) {
    console.error('Failed to fetch report types', e)
  }
})

const dynamicColumns = computed(() => {
  if (reportData.value.length === 0) return []
  const firstRow = reportData.value[0]
  return Object.keys(firstRow).map(key => ({
    prop: key,
    label: key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
  }))
})

const fetchReport = async () => {
  if (!activeTab.value) return
  
  loading.value = true
  try {
    const apiMap = {
      RESOURCE_ALLOCATION: getResourceAllocation,
      BUDGET_MISMATCH: getBudgetMismatch,
      PROJECT_COST: getProjectCost
    }
    const apiFunc = apiMap[activeTab.value]
    if (apiFunc) {
        const { data } = await apiFunc(searchParams.value)
        reportData.value = data
    } else {
        console.warn(`No API function for report type: ${activeTab.value}`)
        reportData.value = []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  if (!activeTab.value) return
  
  try {
    const apiMap = {
      RESOURCE_ALLOCATION: exportResourceAllocation,
      BUDGET_MISMATCH: exportBudgetMismatch,
      PROJECT_COST: exportProjectCost
    }
    const apiFunc = apiMap[activeTab.value]
    if (apiFunc) {
        const response = await apiFunc(searchParams.value)
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `${activeTab.value.toLowerCase()}.csv`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    }
  } catch (e) {
    console.error(e)
  }
}

watch(activeTab, () => {
  reportData.value = []
  // Optionally auto-fetch when tab changes
  // fetchReport() 
})
</script>

<style scoped>
.search-form {
  margin-top: 20px;
  margin-bottom: 20px;
}
</style>
