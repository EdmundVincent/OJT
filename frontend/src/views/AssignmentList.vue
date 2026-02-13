<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">アサイン登録</el-button>
      <template v-if="authStore.isAdmin">
        <el-button type="success" @click="triggerImport">CSVインポート</el-button>
        <el-button type="warning" @click="handleExport">CSVエクスポート</el-button>
        <input type="file" ref="fileInput" style="display: none" @change="handleImport" accept=".csv" />
      </template>
    </div>
    
    <el-table :data="assignments" border style="width: 100%">
      <el-table-column prop="projectId" label="PJ ID" width="80" />
      <el-table-column prop="employeeId" label="社員ID" width="80" />
      <el-table-column prop="startYm" label="開始年月" />
      <el-table-column prop="endYm" label="終了年月" />
      <el-table-column prop="allocationRatio" label="稼働率" />
      <el-table-column prop="unitPrice" label="単価" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" label-width="120px">
        <el-form-item label="プロジェクト">
          <el-select v-model="form.projectId" placeholder="プロジェクトを選択" filterable>
            <el-option
              v-for="p in projects"
              :key="p.id"
              :label="p.name + ' (ID:' + p.id + ')'"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="社員">
          <el-select v-model="form.employeeId" placeholder="社員を選択" filterable>
            <el-option
              v-for="e in employees"
              :key="e.id"
              :label="e.name + ' (ID:' + e.id + ')'"
              :value="e.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="開始年月">
          <el-date-picker v-model="form.startYm" type="month" value-format="YYYYMM" placeholder="選択してください" style="width: 100%" />
        </el-form-item>
        <el-form-item label="終了年月">
          <el-date-picker v-model="form.endYm" type="month" value-format="YYYYMM" placeholder="選択してください" style="width: 100%" />
        </el-form-item>
        <el-form-item label="稼働率">
          <el-input-number v-model="form.allocationRatio" :step="0.1" :min="0" :max="1.5" />
        </el-form-item>
        <el-form-item label="単価">
          <el-input-number v-model="form.unitPrice" :step="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAssignments, createAssignment, updateAssignment, deleteAssignment, importAssignments, exportAssignments } from '@/api/assignment'
import { getProjects } from '@/api/project'
import { getEmployees } from '@/api/employee'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const assignments = ref([])
const projects = ref([])
const employees = ref([])
const dialogVisible = ref(false)
const form = ref({})
const fileInput = ref(null)

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
  form.value = { startYm: '202501', allocationRatio: 1.0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { 
    ...row,
    startYm: row.startYm ? String(row.startYm) : '',
    endYm: row.endYm ? String(row.endYm) : ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.value.id) {
      await updateAssignment(form.value.id, form.value)
    } else {
      await createAssignment(form.value)
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
  // fetchAssignments() // 初期ロードはしない、検索ボタンをつけるべきだが、今回は省略
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
