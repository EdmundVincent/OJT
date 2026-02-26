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
      
      <el-input
        v-model="searchQuery"
        placeholder="名前、社員ID、部門、スキルで検索"
        style="width: 250px; margin-left: 20px"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select
        v-model="selectedSkills"
        multiple
        collapse-tags
        placeholder="スキルで絞り込み"
        style="width: 250px; margin-left: 10px"
        clearable
      >
        <el-option
          v-for="skill in allSkills"
          :key="skill"
          :label="skill"
          :value="skill"
        />
      </el-select>
    </div>
    
    <el-table :data="filteredEmployees" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="employeeCode" label="社員ID" width="100" />
      <el-table-column prop="name" label="氏名" />
      <el-table-column prop="department" label="部門" />
      <el-table-column prop="rank" label="ランク" />
      <el-table-column prop="joinYear" label="入社年" />
      <el-table-column label="スキル">
        <template #default="scope">
          <div v-if="scope.row.skills" class="skill-tags">
            <el-tag 
              v-for="skill in scope.row.skills.split(',')" 
              :key="skill"
              size="small"
              class="skill-tag"
            >
              {{ skill.trim() }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" @click="openHistory(scope.row)">履歴</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" :rules="rules" ref="employeeForm" label-width="100px">
        <el-form-item label="社員ID" prop="employeeCode">
          <el-input v-model="form.employeeCode" placeholder="4桁の数字 (例: 1001)" maxlength="4" />
        </el-form-item>
        <el-form-item label="氏名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="入社年">
          <el-date-picker 
            v-model="form.joinYear" 
            type="year" 
            value-format="YYYY" 
            placeholder="年を選択" 
            style="width: 100%" 
          />
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
        <el-form-item label="スキル">
          <el-input v-model="form.skills" type="textarea" placeholder="Java, Python, Vue.jsなど" />
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

    <el-dialog v-model="historyDialogVisible" title="プロジェクト履歴" width="700px">
      <div v-if="historyEmployee" style="margin-bottom: 10px;">
        {{ historyEmployee.name }} ({{ historyEmployee.employeeCode }})
      </div>
      <el-table :data="historyItems" border size="small">
        <el-table-column prop="projectName" label="プロジェクト名" min-width="180" />
        <el-table-column prop="projectStatus" label="ステータス" width="120" />
        <el-table-column prop="startYm" label="開始年月" width="100" />
        <el-table-column prop="endYm" label="終了年月" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="historyDialogVisible = false">閉じる</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getEmployees, createEmployee, updateEmployee, deleteEmployee, exportEmployees, importEmployees, getEmployeeHistory } from '@/api/employee'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const employees = ref([])
const dialogVisible = ref(false)
const employeeForm = ref(null)
const form = ref({})
const searchQuery = ref('')
const selectedSkills = ref([])
const historyDialogVisible = ref(false)
const historyItems = ref([])
const historyEmployee = ref(null)

const rules = {
  employeeCode: [
    { required: true, message: '社員IDを入力してください', trigger: 'blur' },
    { pattern: /^\d{4}$/, message: '4桁の数字で入力してください', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '氏名を入力してください', trigger: 'blur' }
  ]
}

const allSkills = computed(() => {
  const skillsSet = new Set()
  employees.value.forEach(e => {
    if (e.skills) {
      e.skills.split(',').forEach(s => {
        const trimmed = s.trim()
        if (trimmed) skillsSet.add(trimmed)
      })
    }
  })
  return Array.from(skillsSet).sort()
})

const filteredEmployees = computed(() => {
  let list = employees.value

  // Search query filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    list = list.filter(e => 
      e.name?.toLowerCase().includes(query) ||
      e.employeeCode?.toLowerCase().includes(query) ||
      e.department?.toLowerCase().includes(query) ||
      e.skills?.toLowerCase().includes(query)
    )
  }

  // Skills filter
  if (selectedSkills.value.length > 0) {
    list = list.filter(e => {
      if (!e.skills) return false
      const empSkills = e.skills.split(',').map(s => s.trim())
      return selectedSkills.value.every(s => empSkills.includes(s))
    })
  }

  return list
})

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
  form.value = { joinYear: '2024' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { 
    ...row,
    joinYear: row.joinYear ? String(row.joinYear) : ''
  }
  dialogVisible.value = true
}

const openHistory = async (row) => {
  historyEmployee.value = row
  historyItems.value = []
  historyDialogVisible.value = true
  try {
    const res = await getEmployeeHistory(row.id)
    historyItems.value = res.data || []
  } catch (e) {
    historyItems.value = []
  }
}

const handleSave = async () => {
  if (!employeeForm.value) return
  
  try {
    await employeeForm.value.validate()
    if (form.value.id) {
      await updateEmployee(form.value.id, form.value)
    } else {
      await createEmployee(form.value)
    }
    dialogVisible.value = false
    fetchEmployees()
    ElMessage.success('保存しました')
  } catch (e) {
    // validation failed or api error
    if (e.message) {
      console.error(e)
    }
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
.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.skill-tag {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
