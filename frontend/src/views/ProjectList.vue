<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新規登録</el-button>
      <template v-if="authStore.isAdmin">
        <el-button type="success" @click="triggerImport">CSVインポート</el-button>
        <el-button type="warning" @click="handleExport">CSVエクスポート</el-button>
        <el-button type="success" @click="triggerPlanImport">計画CSVインポート</el-button>
        <el-button type="warning" @click="handlePlanExport">計画CSVエクスポート</el-button>
        <input type="file" ref="fileInput" style="display: none" @change="handleImport" accept=".csv" />
        <input type="file" ref="planFileInput" style="display: none" @change="handlePlanImport" accept=".csv" />
      </template>

      <el-input
        v-model="searchQuery"
        placeholder="プロジェクト名、顧客名で検索"
        style="width: 250px; margin-left: 20px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
        v-model="selectedCustomerId"
        placeholder="顧客で絞り込み"
        style="width: 200px; margin-left: 10px"
        clearable
      >
        <el-option
          v-for="c in customers"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="全て" name="全て" />
      <el-tab-pane label="提案中" name="提案中" />
      <el-tab-pane label="進行中" name="進行中" />
      <el-tab-pane label="完了" name="完了" />
      <el-tab-pane label="中断" name="中断" />
    </el-tabs>
    
    <el-table :data="filteredProjects" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="ステータス" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status || '進行中')">
            {{ scope.row.status || '進行中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="優先度" width="80">
        <template #default="scope">
          <el-tag :type="getPriorityType(scope.row.priority)" effect="dark" size="small">
            {{ scope.row.priority || '中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="プロジェクト名" min-width="150" />
      <el-table-column label="PM" width="100">
        <template #default="scope">
          {{ employees.find(e => e.id === scope.row.pmId)?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="顧客" width="120">
        <template #default="scope">
          {{ customers.find(c => String(c.id) === scope.row.customerId)?.name || scope.row.customerId }}
        </template>
      </el-table-column>
      <el-table-column prop="projectType" label="種別" width="100" />
      <el-table-column prop="startYm" label="開始" width="80" />
      <el-table-column prop="endYm" label="終了" width="80" />
      <el-table-column label="予算売上（万）">
        <template #default="scope">
          {{ formatRevenue(scope.row.revenue) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" label-width="120px">
        <el-form-item label="プロジェクト名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="優先度">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="高" value="高" />
                <el-option label="中" value="中" />
                <el-option label="低" value="低" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="種別">
              <el-select v-model="form.projectType" style="width: 100%">
                <el-option label="新規開発" value="新規開発" />
                <el-option label="保守・運用" value="保守・運用" />
                <el-option label="基盤構築" value="基盤構築" />
                <el-option label="その他" value="その他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="PM (責任者)">
              <el-select v-model="form.pmId" filterable placeholder="要員を選択" style="width: 100%">
                <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ステータス">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="提案中" value="提案中" />
                <el-option label="進行中" value="進行中" />
                <el-option label="完了" value="完了" />
                <el-option label="中断" value="中断" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.id && form.status !== originalStatus" label="ステータス変更理由">
          <el-input v-model="form.statusChangeReason" type="textarea" />
        </el-form-item>
        <el-form-item label="顧客">
          <el-select v-model="form.customerId" placeholder="顧客を選択" style="width: 100%">
            <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="開始年月">
          <el-date-picker v-model="form.startYm" type="month" value-format="YYYYMM" placeholder="選択してください" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="endYmLabel">
          <el-date-picker v-model="form.endYm" type="month" value-format="YYYYMM" placeholder="未定の場合は空欄のまま" style="width: 100%" />
        </el-form-item>
        <el-form-item label="予算売上（万）">
          <el-input-number v-model="form.revenue" :min="0" :step="1" />
        </el-form-item>
        <el-divider v-if="statusHistory.length > 0" />
        <el-table v-if="statusHistory.length > 0" :data="statusHistory" size="small" border>
          <el-table-column prop="changedAt" label="変更日時" width="180" />
          <el-table-column prop="changedBy" label="変更者" width="180" />
          <el-table-column prop="oldStatus" label="変更前ステータス" width="140" />
          <el-table-column prop="newStatus" label="変更後ステータス" width="140" />
          <el-table-column prop="reason" label="理由" />
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getProjects, createProject, updateProject, deleteProject, importProjects, exportProjects, importProjectPlans, exportProjectPlans, getProjectStatusHistory } from '@/api/project'
import { getCustomers } from '@/api/customer'
import { getEmployees } from '@/api/employee'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const projects = ref([])
const customers = ref([])
const employees = ref([])
const dialogVisible = ref(false)
const form = ref({})
const fileInput = ref(null)
const planFileInput = ref(null)
const activeTab = ref('全て')
const searchQuery = ref('')
const selectedCustomerId = ref(null)
const originalStatus = ref(null)
const statusHistory = ref([])

const endYmLabel = computed(() => {
  if (form.value.status === '完了') {
    return '終了年月'
  }
  return '予定終了年月'
})

const formatRevenue = (value) => {
  if (value == null) return ''
  const num = Number(value)
  if (Number.isNaN(num)) return ''
  return Math.round(num / 10000)
}

const filteredProjects = computed(() => {
  let list = projects.value
  
  // Tab filter
  if (activeTab.value !== '全て') {
    list = list.filter(p => (p.status || '進行中') === activeTab.value)
  }

  // Customer filter
  if (selectedCustomerId.value) {
    list = list.filter(p => String(p.customerId) === String(selectedCustomerId.value))
  }
  
  // Search query filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(p => {
      const customerName = customers.value.find(c => String(c.id) === p.customerId)?.name || ''
      const pmName = employees.value.find(e => e.id === p.pmId)?.name || ''
      return p.name?.toLowerCase().includes(query) || 
             customerName.toLowerCase().includes(query) ||
             pmName.toLowerCase().includes(query)
    })
  }
  
  return list
})

const getStatusType = (status) => {
  const map = {
    '提案中': 'info',
    '進行中': 'success',
    '完了': 'warning',
    '中断': 'danger'
  }
  return map[status] || 'success'
}

const getPriorityType = (priority) => {
  const map = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return map[priority] || 'warning'
}

const fetchProjects = async () => {
  const [pRes, cRes, eRes] = await Promise.all([getProjects(), getCustomers(), getEmployees()])
  projects.value = pRes.data
  customers.value = cRes.data
  employees.value = eRes.data
}


const triggerImport = () => fileInput.value.click()
const triggerPlanImport = () => planFileInput.value.click()

const handleImport = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    await importProjects(formData)
    ElMessage.success('インポートしました')
    fetchProjects()
  } catch (e) {
    console.error(e)
  } finally {
    event.target.value = ''
  }
}

const handleExport = async () => {
  try {
    const response = await exportProjects()
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'projects.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    console.error(e)
  }
}

const handlePlanImport = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    await importProjectPlans(formData)
    ElMessage.success('計画データをインポートしました')
  } catch (e) {
    console.error(e)
  } finally {
    event.target.value = ''
  }
}

const handlePlanExport = async () => {
  try {
    const response = await exportProjectPlans()
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'project_plans.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    console.error(e)
  }
}

const getCurrentYm = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  return `${y}${String(m).padStart(2, '0')}`
}

const handleAdd = () => {
  form.value = { 
    startYm: getCurrentYm(), 
    revenue: 0,
    priority: '中',
    projectType: '新規開発',
    status: '提案中'
  }
  originalStatus.value = null
  statusHistory.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  form.value = { 
    ...row,
    startYm: row.startYm ? String(row.startYm) : '',
    endYm: row.endYm ? String(row.endYm) : '',
    revenue: formatRevenue(row.revenue),
    priority: row.priority || '中',
    projectType: row.projectType || '新規開発',
    status: row.status || '進行中',
    statusChangeReason: ''
  }
  originalStatus.value = row.status || '進行中'
  dialogVisible.value = true
  statusHistory.value = []
  try {
    const res = await getProjectStatusHistory(row.id)
    statusHistory.value = res.data || []
  } catch (e) {
    statusHistory.value = []
  }
}

const handleSave = async () => {
  try {
    if (form.value.id && form.value.status !== originalStatus.value && !form.value.statusChangeReason) {
      ElMessage.error('ステータス変更理由を入力してください')
      return
    }
    const payload = {
      ...form.value,
      revenue: form.value.revenue != null ? Math.round(Number(form.value.revenue) * 10000) : 0
    }
    if (form.value.id) {
      await updateProject(form.value.id, payload)
    } else {
      await createProject(payload)
    }
    dialogVisible.value = false
    fetchProjects()
    ElMessage.success('保存しました')
  } catch (e) {
    // handled by interceptor
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('削除しますか？', '確認', { type: 'warning' })
    .then(async () => {
      await deleteProject(row.id)
      fetchProjects()
      ElMessage.success('削除しました')
    })
}

onMounted(fetchProjects)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
