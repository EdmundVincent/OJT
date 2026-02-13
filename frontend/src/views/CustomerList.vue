<template>
  <div>
    <h2>顧客マスタ</h2>
    <div class="toolbar" v-if="authStore.isAdmin">
      <el-button type="primary" @click="handleAdd">新規登録</el-button>
    </div>

    <el-table :data="customers" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="顧客名" />
      <el-table-column prop="contactPerson" label="担当者名" />
      <el-table-column prop="email" label="メールアドレス" />
      <el-table-column prop="phone" label="電話番号" />
      <el-table-column label="操作" width="180" v-if="authStore.isAdmin">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">編集</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '編集' : '新規登録'">
      <el-form :model="form" label-width="120px">
        <el-form-item label="顧客名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="担当者名">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="メールアドレス">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="電話番号">
          <el-input v-model="form.phone" />
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
import { getCustomers, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const customers = ref([])
const dialogVisible = ref(false)
const form = ref({})

const fetchCustomers = async () => {
  const { data } = await getCustomers()
  customers.value = data
}

const handleAdd = () => {
  form.value = {}
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.value.id) {
      await updateCustomer(form.value.id, form.value)
    } else {
      await createCustomer(form.value)
    }
    dialogVisible.value = false
    fetchCustomers()
    ElMessage.success('保存しました')
  } catch (e) {
    // handled
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('削除しますか？', '確認', { type: 'warning' })
    .then(async () => {
      await deleteCustomer(row.id)
      fetchCustomers()
      ElMessage.success('削除しました')
    })
}

onMounted(fetchCustomers)
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
