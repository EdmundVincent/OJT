<template>
  <div class="user-list">
    <div class="header">
      <h2>利用者マスタ</h2>
      <el-button type="primary" @click="handleAdd">新規登録</el-button>
    </div>

    <el-table :data="users" border stripe v-loading="loading">
      <el-table-column prop="email" label="Email" />
      <el-table-column prop="role" label="Role">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="validFrom" label="Valid From" />
      <el-table-column prop="validTo" label="Valid To" />
      <el-table-column label="Actions" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '利用者編集' : '利用者登録'"
      width="500px"
    >
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="Role" prop="role">
          <el-select v-model="form.role">
            <el-option label="ADMIN" value="ADMIN" />
            <el-option label="USER" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="変更する場合のみ入力" v-if="isEdit" />
          <el-input v-model="form.password" type="password" placeholder="必須" v-else />
        </el-form-item>
        <el-form-item label="Valid From" prop="validFrom">
          <el-date-picker v-model="form.validFrom" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="Valid To" prop="validTo">
          <el-date-picker v-model="form.validTo" type="date" value-format="YYYY-MM-DD" placeholder="無期限" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">キャンセル</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getUsers, createUser, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  email: '',
  role: 'USER',
  password: '',
  validFrom: '',
  validTo: null
})

const rules = {
  email: [{ required: true, message: 'Email is required', trigger: 'blur' }],
  role: [{ required: true, message: 'Role is required', trigger: 'change' }],
  password: [{ required: false, message: 'Password is required', trigger: 'blur' }], // Dynamic check logic needed
  validFrom: [{ required: true, message: 'Valid From is required', trigger: 'change' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const { data } = await getUsers()
    users.value = data
  } catch (error) {
    ElMessage.error('Failed to fetch users')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.email = ''
  form.role = 'USER'
  form.password = ''
  form.validFrom = new Date().toISOString().split('T')[0]
  form.validTo = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  form.password = '' // Don't show hash
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('本当に削除しますか？', 'Warning', {
    confirmButtonText: '削除',
    cancelButtonText: 'キャンセル',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.email)
      ElMessage.success('削除しました')
      fetchUsers()
    } catch (error) {
      ElMessage.error('削除に失敗しました')
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  // Custom validation for password on create
  if (!isEdit.value && !form.password) {
    ElMessage.warning('パスワードは必須です')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateUser(form.email, form)
        } else {
          await createUser(form)
        }
        ElMessage.success('保存しました')
        dialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error('保存に失敗しました: ' + (error.response?.data?.message || error.message))
      }
    }
  })
}

onMounted(fetchUsers)
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
