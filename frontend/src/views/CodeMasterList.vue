<template>
  <div>
    <div class="toolbar" v-if="authStore.isAdmin">
      <el-button type="success" @click="triggerImport">CSVインポート</el-button>
      <el-button type="warning" @click="handleExport">CSVエクスポート</el-button>
      <input type="file" ref="fileInput" style="display: none" @change="handleImport" accept=".csv" />
    </div>
    <el-table :data="codes" border style="width: 100%">
      <el-table-column prop="categoryId" label="カテゴリID" />
      <el-table-column prop="categoryName" label="カテゴリ名" />
      <el-table-column prop="codeId" label="コードID" />
      <el-table-column prop="codeName" label="コード名" />
      <el-table-column prop="sortOrder" label="表示順" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCodeMasters, importCodeMasters, exportCodeMasters } from '@/api/code-master'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const codes = ref([])
const fileInput = ref(null)

const fetchCodes = async () => {
  const { data } = await getCodeMasters()
  codes.value = data
}

const triggerImport = () => fileInput.value.click()

const handleImport = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    await importCodeMasters(formData)
    ElMessage.success('インポートしました')
    fetchCodes()
  } catch (e) {
    console.error(e)
  } finally {
    event.target.value = ''
  }
}

const handleExport = async () => {
  try {
    const response = await exportCodeMasters()
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'codemaster.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (e) {
    console.error(e)
  }
}

onMounted(fetchCodes)
</script>
<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
