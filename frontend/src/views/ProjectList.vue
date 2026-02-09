<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新規登録</el-button>
      <template v-if="authStore.user?.role === 'ADMIN'">
        <el-button type="success" @click="triggerImport">CSVインポート</el-button>
        <el-button type="warning" @click="handleExport">CSVエクスポート</el-button>
        <el-button type="success" @click="triggerPlanImport">計画CSVインポート</el-button>
        <el-button type="warning" @click="handlePlanExport">計画CSVエクスポート</el-button>
        <input type="file" ref="fileInput" style="display: none" @change="handleImport" accept=".csv" />
        <input type="file" ref="planFileInput" style="display: none" @change="handlePlanImport" accept=".csv" />
      </template>
    </div>
    
    <el-table :data="projects" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="プロジェクト名" />
      <el-table-column prop="customerId" label="顧客ID" />
      <el-table-column prop="startYm" label="開始年月" />
      <el-table-column prop="endYm" label="終了年月" />
      <el-table-column prop="revenue" label="売上" />
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
        <el-form-item label="顧客ID">
          <el-input v-model="form.customerId" />
        </el-form-item>
        <el-form-item label="開始年月">
          <el-input-number v-model="form.startYm" :min="200000" :max="209912" />
        </el-form-item>
        <el-form-item label="終了年月">
          <el-input-number v-model="form.endYm" :min="200000" :max="209912" />
        </el-form-item>
        <el-form-item label="売上">
          <el-input-number v-model="form.revenue" :min="0" />
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
import { getProjects, createProject, updateProject, deleteProject, importProjects, exportProjects, importProjectPlans, exportProjectPlans } from '@/api/project'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const projects = ref([])
const dialogVisible = ref(false)
const form = ref({})
const fileInput = ref(null)
const planFileInput = ref(null)

const fetchProjects = async () => {
  const { data } = await getProjects()
  projects.value = data
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

const handleAdd = () => {
  form.value = { startYm: 202501 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.value.id) {
      await updateProject(form.value.id, form.value)
    } else {
      await createProject(form.value)
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
