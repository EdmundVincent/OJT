<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新規登録</el-button>
      <el-upload
        action="#"
        :http-request="uploadCsv"
        :show-file-list="false"
        accept=".csv"
        style="display: inline-block; margin-left: 10px;"
      >
        <el-button type="success">CSVインポート</el-button>
      </el-upload>
      <el-button type="warning" @click="handleExport" style="margin-left: 10px">CSVエクスポート</el-button>
    </div>
    
    <el-table :data="employees" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="氏名" />
      <el-table-column prop="department" label="部門" />
      <el-table-column prop="rank" label="ランク" />
      <el-table-column prop="joinYear" label="入社年" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" label-width="100px">
        <el-form-item label="氏名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="入社年">
          <el-input-number v-model="form.joinYear" />
        </el-form-item>
        <el-form-item label="ランク">
          <el-select v-model="form.rank">
            <el-option label="ジュニア" value="JUNIOR" />
            <el-option label="ミドル" value="MIDDLE" />
            <el-option label="シニア" value="SENIOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="部門">
          <el-select v-model="form.department">
            <el-option label="営業部" value="SALES" />
            <el-option label="開発部" value="DEVELOPMENT" />
            <el-option label="コンサル部" value="CONSULTING" />
          </el-select>
        </el-form-item>
        <el-form-item label="備考">
          <el-input v-model="form.remarks" type="textarea" />
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
import { getEmployees, createEmployee, updateEmployee, deleteEmployee, exportEmployees, importEmployees } from '@/api/employee'
import { ElMessage, ElMessageBox } from 'element-plus'

const employees = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchEmployees = async () => {
  const { data } = await getEmployees()
  employees.value = data
}

const uploadCsv = async (param) => {
  const formData = new FormData()
  formData.append('file', param.file)
  try {
    await importEmployees(formData)
    ElMessage.success('インポートしました')
    fetchEmployees()
  } catch (e) {
    // handled
  }
}

const handleExport = async () => {
  try {
    const response = await exportEmployees()
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'employees.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    // handled
  }
}

const handleAdd = () => {
  form.value = { joinYear: 2024 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.value.id) {
      await updateEmployee(form.value.id, form.value)
    } else {
      await createEmployee(form.value)
    }
    dialogVisible.value = false
    fetchEmployees()
    ElMessage.success('保存しました')
  } catch (e) {
    // Error handled by interceptor
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('本当に削除しますか？', '警告', {
    confirmButtonText: '削除',
    cancelButtonText: 'キャンセル',
    type: 'warning',
  }).then(async () => {
    await deleteEmployee(row.id)
    fetchEmployees()
    ElMessage.success('削除しました')
  })
}

onMounted(fetchEmployees)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
