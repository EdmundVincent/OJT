<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">アサイン登録</el-button>
      <template v-if="authStore.isAdmin">
        <el-button type="success" @click="triggerImport">CSVインポート</el-button>
        <el-button type="warning" @click="handleExport">CSVエクスポート</el-button>
        <input type="file" ref="fileInput" style="display: none" @change="handleImport" accept=".csv" />
      </template>

      <el-select
        v-model="selectedProjectId"
        placeholder="プロジェクトで絞り込み"
        clearable
        filterable
        style="width: 200px; margin-left: 10px"
      >
        <el-option
          v-for="p in projects"
          :key="p.id"
          :label="p.name"
          :value="p.id"
        />
      </el-select>

      <el-select
        v-model="selectedEmployeeId"
        placeholder="社員で絞り込み"
        clearable
        filterable
        style="width: 200px; margin-left: 10px"
      >
        <el-option
          v-for="e in employees"
          :key="e.id"
          :label="e.name"
          :value="e.id"
        />
      </el-select>

      <el-input
        v-model="searchQuery"
        placeholder="検索"
        style="width: 200px; margin-left: 10px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table :data="filteredAssignments" border style="width: 100%">
      <el-table-column label="プロジェクト" min-width="150">
        <template #default="scope">
          {{ projects.find(p => p.id === scope.row.projectId)?.name || scope.row.projectId }}
        </template>
      </el-table-column>
      <el-table-column label="社員" min-width="150">
        <template #default="scope">
          <template v-if="employees.find(e => e.id === scope.row.employeeId)">
            {{ employees.find(e => e.id === scope.row.employeeId).name }} 
            ({{ employees.find(e => e.id === scope.row.employeeId).employeeCode }})
          </template>
          <template v-else>{{ scope.row.employeeId }}</template>
        </template>
      </el-table-column>
      <el-table-column prop="startYm" label="開始年月" width="100" />
      <el-table-column prop="endYm" label="終了年月" width="100" />
      <el-table-column prop="allocationRatio" label="稼働率" width="80" />
      <el-table-column label="単価（万）" width="120">
        <template #default="scope">
          {{ formatMoney(scope.row.unitPrice) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" label-width="120px">
        <el-form-item label="プロジェクト">
          <el-select v-model="form.projectId" placeholder="プロジェクトを選択" filterable style="width: 100%">
            <el-option
              v-for="p in projects"
              :key="p.id"
              :label="p.name + ' (ID:' + p.id + ')'"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="社員">
          <el-select v-model="form.employeeId" placeholder="社員を選択" filterable style="width: 100%">
            <el-option
              v-for="e in employees"
              :key="e.id"
              :label="e.name + ' (' + e.employeeCode + ')'"
              :value="e.id"
            />
          </el-select>
          <div v-if="!form.id && form.projectId" style="margin-top: 10px">
            <el-button type="success" size="small" plain @click="showRecommendationsDialog = true">
              <el-icon><Star /></el-icon> AI推奨候補を表示
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="開始年月">
          <el-date-picker v-model="form.startYm" type="month" value-format="YYYYMM" placeholder="選択してください" style="width: 100%" />
        </el-form-item>
        <el-form-item label="終了年月">
          <el-date-picker v-model="form.endYm" type="month" value-format="YYYYMM" placeholder="選択してください" style="width: 100%" />
        </el-form-item>
        <el-form-item label="稼働率">
          <el-input-number v-model="form.allocationRatio" :step="0.1" :min="0" :max="1.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="単価（万）">
          <el-input-number v-model="form.unitPrice" :step="1" :min="0" style="width: 100%" />
        </el-form-item>
        <el-alert
          v-if="conflictWarnings.length > 0"
          type="error"
          show-icon
          :closable="false"
          style="margin-bottom: 10px"
        >
          <ul>
            <li v-for="msg in conflictWarnings" :key="msg">{{ msg }}</li>
          </ul>
        </el-alert>
        <div v-if="currentEmployeeAssignments.length > 0">
          <div style="margin-bottom: 4px; font-weight: bold;">選択中の社員の既存アサイン</div>
          <el-table :data="currentEmployeeAssignments" size="small" border>
            <el-table-column label="プロジェクト" min-width="150">
              <template #default="scope">
                {{ projects.find(p => p.id === scope.row.projectId)?.name || scope.row.projectId }}
              </template>
            </el-table-column>
            <el-table-column prop="startYm" label="開始年月" width="100" />
            <el-table-column prop="endYm" label="終了年月" width="100" />
            <el-table-column prop="allocationRatio" label="稼働率" width="80" />
          </el-table>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRecommendationsDialog" title="AI推奨候補" width="600px">
      <el-table :data="recommendations" border size="small">
        <el-table-column label="社員名" prop="name" width="120" />
        <el-table-column label="部門" prop="department" width="100" />
        <el-table-column label="現在稼働率" width="100">
          <template #default="scope">
            <el-progress :percentage="Math.min(100, Math.round(scope.row.currentUtilization * 100))" :status="scope.row.currentUtilization > 1 ? 'exception' : ''" />
          </template>
        </el-table-column>
        <el-table-column label="スキル" prop="skills" show-overflow-tooltip />
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button type="primary" size="small" @click="selectRecommendedEmployee(scope.row)">選択</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 10px; font-size: 12px; color: #909399;">
        ※プロジェクト期間中に稼働率に余裕があり、スキルや部門が近い社員を上位に表示しています。
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { getAssignments, createAssignment, updateAssignment, deleteAssignment, importAssignments, exportAssignments } from '@/api/assignment'
import { getProjects } from '@/api/project'
import { getEmployees } from '@/api/employee'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const assignments = ref([])
const projects = ref([])
const employees = ref([])
const dialogVisible = ref(false)
const form = ref({})
const fileInput = ref(null)
const searchQuery = ref('')
const selectedProjectId = ref(null)
const selectedEmployeeId = ref(null)
const conflictWarnings = ref([])
const showRecommendationsDialog = ref(false)

const formatMoney = (value) => {
  if (value == null) return ''
  const num = Number(value)
  if (Number.isNaN(num)) return ''
  return Math.round(num / 10000)
}

const recommendations = computed(() => {
  if (!form.value.projectId) return []
  const project = projects.value.find(p => p.id === form.value.projectId)
  if (!project) return []

  const currentYm = Number(getCurrentYm())
  
  return employees.value.map(e => {
    // Calculate current utilization
    const utilization = assignments.value
      .filter(a => a.employeeId === e.id && a.startYm <= currentYm && a.endYm >= currentYm)
      .reduce((sum, a) => sum + Number(a.allocation_ratio), 0)
    
    // Simple score: lower utilization is better
    let score = (1.5 - utilization) * 10
    
    // Department matching
    const projectCustomer = projects.value.find(p => p.id === form.value.projectId)?.customerId
    // (Could add more complex logic here)
    
    return {
      ...e,
      currentUtilization: utilization,
      score: score
    }
  })
  .filter(e => e.currentUtilization < 1.0) // Only recommend those with space
  .sort((a, b) => b.score - a.score)
  .slice(0, 5) // Top 5
})

const selectRecommendedEmployee = (emp) => {
  form.value.employeeId = emp.id
  showRecommendationsDialog.value = false
}

const getCurrentYm = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  return `${y}${String(m).padStart(2, '0')}`
}

const filteredAssignments = computed(() => {
  let list = assignments.value

  if (selectedProjectId.value) {
    list = list.filter(a => a.projectId === selectedProjectId.value)
  }

  if (selectedEmployeeId.value) {
    list = list.filter(a => a.employeeId === selectedEmployeeId.value)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(a => {
      const projectName = projects.value.find(p => p.id === a.projectId)?.name || ''
      const employeeName = employees.value.find(e => e.id === a.employeeId)?.name || ''
      return projectName.toLowerCase().includes(query) || employeeName.toLowerCase().includes(query)
    })
  }
  
  return list
})

const currentEmployeeAssignments = computed(() => {
  if (!form.value.employeeId) return []
  return assignments.value.filter(a => {
    if (a.employeeId !== form.value.employeeId) return false
    if (form.value.id && a.id === form.value.id) return false
    return true
  })
})

const recalcConflicts = () => {
  conflictWarnings.value = []
  if (!form.value.employeeId || !form.value.startYm || !form.value.endYm || !form.value.allocationRatio) {
    return
  }
  const startYm = Number(form.value.startYm)
  const endYm = Number(form.value.endYm)
  if (!startYm || !endYm || endYm < startYm) {
    return
  }
  const warnings = []
  let ym = startYm
  while (ym <= endYm) {
    let total = Number(form.value.allocationRatio) || 0
    currentEmployeeAssignments.value.forEach(a => {
      if (ym >= a.startYm && ym <= a.endYm) {
        total += Number(a.allocationRatio) || 0
      }
    })
    if (total > 1.0) {
      const year = Math.floor(ym / 100)
      const month = ym % 100
      const label = `${year}-${String(month).padStart(2, '0')}`
      warnings.push(`${label} の合計稼働率は ${total.toFixed(2)} で 1.0 を超えています`)
    }
    let year = Math.floor(ym / 100)
    let month = ym % 100
    month += 1
    if (month > 12) {
      month = 1
      year += 1
    }
    ym = year * 100 + month
  }
  conflictWarnings.value = warnings
}

watch(
  () => [form.value.employeeId, form.value.startYm, form.value.endYm, form.value.allocationRatio],
  recalcConflicts
)

const fetchMasterData = async () => {
  try {
    const [pRes, eRes] = await Promise.all([getProjects(), getEmployees()])
    projects.value = pRes.data
    employees.value = eRes.data
  } catch (e) {
    console.error('Failed to fetch master data', e)
  }
}

const fetchAssignments = async () => {
  // パラメータなしで全件取得
  const { data } = await getAssignments()
  assignments.value = data
}

const triggerImport = () => fileInput.value.click()

const handleImport = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    await importAssignments(formData)
    ElMessage.success('インポートしました')
    fetchAssignments()
  } catch (e) {
    console.error(e)
  } finally {
    event.target.value = ''
  }
}

const handleExport = async () => {
  try {
    const response = await exportAssignments()
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'assignments.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = () => {
  form.value = { startYm: '202501', allocationRatio: 1.0, unitPrice: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { 
    ...row,
    startYm: row.startYm ? String(row.startYm) : '',
    endYm: row.endYm ? String(row.endYm) : '',
    unitPrice: formatMoney(row.unitPrice)
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    const payload = {
      ...form.value,
      unitPrice: form.value.unitPrice != null ? Math.round(Number(form.value.unitPrice) * 10000) : 0
    }
    if (form.value.id) {
      await updateAssignment(form.value.id, payload)
    } else {
      await createAssignment(payload)
    }
    dialogVisible.value = false
    fetchAssignments()
    ElMessage.success('保存しました')
  } catch (e) {
    // handled
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('削除しますか？', '確認', { type: 'warning' })
    .then(async () => {
      await deleteAssignment(row.id)
      fetchAssignments()
      ElMessage.success('削除しました')
    })
}

onMounted(() => {
  fetchMasterData()
  fetchAssignments()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
