<template>
  <div>
    <div class="toolbar">
      <el-select v-model="selectedProjectId" placeholder="プロジェクト選択" @change="fetchPlans">
        <el-option
          v-for="p in projects"
          :key="p.id"
          :label="p.name"
          :value="p.id"
        />
      </el-select>
      <el-button type="primary" :disabled="!selectedProjectId" @click="handleAdd" style="margin-left: 10px">
        計画登録
      </el-button>
    </div>

    <el-table :data="plansWithDiff" border style="width: 100%" v-if="selectedProjectId">
      <el-table-column prop="planVersion" label="版数" width="80" />
      <el-table-column prop="startYm" label="開始年月" width="100" />
      <el-table-column prop="endYm" label="終了年月" width="100" />
      <el-table-column label="要員数" width="120">
        <template #default="scope">
          {{ scope.row.resourceCount }}
          <span v-if="scope.row.diffResource !== 0" :class="scope.row.diffResource > 0 ? 'diff-up' : 'diff-down'">
            ({{ scope.row.diffResource > 0 ? '+' : '' }}{{ scope.row.diffResource.toFixed(1) }})
          </span>
        </template>
      </el-table-column>
      <el-table-column label="生産額（万）" width="150">
        <template #default="scope">
          {{ formatMoney(scope.row.productionAmount) }}
          <span v-if="scope.row.diffAmount !== 0" :class="scope.row.diffAmount > 0 ? 'diff-up' : 'diff-down'">
            ({{ scope.row.diffAmount > 0 ? '+' : '' }}{{ formatMoney(scope.row.diffAmount) }})
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">削除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <style scoped>
    .diff-up { color: #f56c6c; font-size: 12px; margin-left: 4px; }
    .diff-down { color: #67c23a; font-size: 12px; margin-left: 4px; }
    </style>

    <el-dialog v-model="dialogVisible" title="計画登録">
      <el-form :model="form" label-width="120px">
        <el-form-item label="版数">
          <el-input-number v-model="form.planVersion" :min="1" />
        </el-form-item>
        <el-form-item label="開始年月">
          <el-date-picker 
            v-model="form.startYm" 
            type="month" 
            value-format="YYYYMM" 
            placeholder="選択してください" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="終了年月">
          <el-date-picker 
            v-model="form.endYm" 
            type="month" 
            value-format="YYYYMM" 
            placeholder="選択してください" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="要員数">
          <el-input-number v-model="form.resourceCount" :min="0" :step="0.1" />
        </el-form-item>
        <el-form-item label="生産額">
          <el-input-number v-model="form.productionAmount" :min="0" :step="10000" />
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
import { ref, onMounted, computed } from 'vue'
import { getProjects } from '@/api/project'
import { getProjectPlans, createProjectPlan, deleteProjectPlan } from '@/api/project-plan'
import { ElMessage, ElMessageBox } from 'element-plus'

const projects = ref([])
const selectedProjectId = ref(null)
const plans = ref([])
const dialogVisible = ref(false)
const form = ref({})

const formatMoney = (value) => {
  if (value == null) return ''
  return Math.round(Number(value) / 10000)
}

const plansWithDiff = computed(() => {
  // Sort plans by version ascending to calculate diff
  const sorted = [...plans.value].sort((a, b) => a.planVersion - b.planVersion)
  return sorted.map((p, index) => {
    const prev = sorted[index - 1]
    return {
      ...p,
      diffResource: prev ? p.resourceCount - prev.resourceCount : 0,
      diffAmount: prev ? p.productionAmount - prev.productionAmount : 0
    }
  }).reverse() // Show latest first in table
})

const fetchProjects = async () => {
  const { data } = await getProjects()
  projects.value = data
}

const fetchPlans = async () => {
  if (!selectedProjectId.value) return
  const { data } = await getProjectPlans(selectedProjectId.value)
  plans.value = data
}

const handleAdd = () => {
  let nextVersion = 1
  let nextStartYm = 202501

  if (plans.value.length > 0) {
    // Find max version
    const maxVer = Math.max(...plans.value.map(p => p.planVersion))
    nextVersion = maxVer + 1

    // Find max endYm to suggest startYm
    const maxEndYm = Math.max(...plans.value.map(p => p.endYm))
    if (maxEndYm % 100 === 12) {
      nextStartYm = (Math.floor(maxEndYm / 100) + 1) * 100 + 1
    } else {
      nextStartYm = maxEndYm + 1
    }
  }

  form.value = { 
    planVersion: nextVersion, 
    startYm: String(nextStartYm),
    resourceCount: 1.0
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (form.value.endYm && form.value.endYm < form.value.startYm) {
    ElMessage.error('終了年月は開始年月以降にしてください')
    return
  }
  try {
    await createProjectPlan(selectedProjectId.value, form.value)
    dialogVisible.value = false
    fetchPlans()
    ElMessage.success('保存しました')
  } catch (e) {
    // handled by axios interceptor
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('削除しますか？', '確認', { type: 'warning' })
    .then(async () => {
      await deleteProjectPlan(selectedProjectId.value, row.id)
      fetchPlans()
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
