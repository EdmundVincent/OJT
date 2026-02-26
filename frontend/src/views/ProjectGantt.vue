<template>
  <div class="gantt-container">
    <div class="toolbar">
      <el-date-picker
        v-model="rangeStart"
        type="month"
        value-format="YYYYMM"
        placeholder="表示開始"
      />
      <span class="range-separator">～</span>
      <el-date-picker
        v-model="rangeEnd"
        type="month"
        value-format="YYYYMM"
        placeholder="表示終了"
      />
      <span class="range-separator" style="margin-left: 20px">表示幅:</span>
      <el-slider v-model="cellWidth" :min="30" :max="200" style="width: 150px" />
    </div>
    <div class="gantt-wrapper">
      <div class="gantt-header">
        <div class="project-name-col">プロジェクト名</div>
        <div class="months-container">
          <div v-for="month in displayMonths" :key="month" class="month-col">
            {{ month }}
          </div>
        </div>
      </div>
      <div class="gantt-body">
        <template v-for="project in projects" :key="project.id">
          <div class="project-row" @click="toggleProject(project.id)">
            <div class="project-name-col">
              <el-icon v-if="getProjectAssignments(project.id).length > 0">
                <CaretBottom v-if="expandedProjects.has(project.id)" />
                <CaretRight v-else />
              </el-icon>
              {{ project.name }}
            </div>
            <div class="months-container">
              <div 
                v-if="getProjectBar(project)"
                class="project-bar"
                :style="getProjectBarStyle(project)"
              >
                <span class="bar-label">{{ project.status || '進行中' }}</span>
              </div>
              <div v-for="month in displayMonths" :key="month" class="month-cell"></div>
            </div>
          </div>
          
          <!-- Assignment Rows -->
          <div v-if="expandedProjects.has(project.id)">
            <div v-for="assign in getProjectAssignments(project.id)" :key="assign.id" class="assignment-row">
              <div class="project-name-col assignment-name">
                {{ getEmployeeName(assign.employeeId) }}
              </div>
              <div class="months-container">
                <div 
                  class="assignment-bar"
                  :style="getAssignmentBarStyle(assign)"
                >
                  <span class="bar-label">{{ assign.allocationRatio * 100 }}%</span>
                </div>
                <div v-for="month in displayMonths" :key="month" class="month-cell"></div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getProjects } from '@/api/project'
import { getAssignments } from '@/api/assignment'
import { getEmployees } from '@/api/employee'
import { CaretRight, CaretBottom } from '@element-plus/icons-vue'

const projects = ref([])
const assignments = ref([])
const employees = ref([])
const rangeStart = ref('202501')
const rangeEnd = ref('202512')
const cellWidth = ref(100)
const expandedProjects = ref(new Set())

const toggleProject = (id) => {
  if (expandedProjects.value.has(id)) {
    expandedProjects.value.delete(id)
  } else {
    expandedProjects.value.add(id)
  }
}

const getProjectAssignments = (projectId) => {
  return assignments.value.filter(a => a.projectId === projectId)
}

const getEmployeeName = (employeeId) => {
  const emp = employees.value.find(e => e.id === employeeId)
  return emp ? emp.name : `社員ID: ${employeeId}`
}

const getAssignmentBarStyle = (assign) => {
  const aStart = Number(assign.startYm)
  const aEnd = assign.endYm ? Number(assign.endYm) : Number(rangeEnd.value)
  const viewStart = Number(rangeStart.value)
  const viewEnd = Number(rangeEnd.value)

  if (aStart > viewEnd || aEnd < viewStart) return { display: 'none' }

  const actualStart = Math.max(aStart, viewStart)
  const actualEnd = Math.min(aEnd, viewEnd)

  const startIndex = displayMonths.value.indexOf(actualStart)
  const endIndex = displayMonths.value.indexOf(actualEnd)

  if (startIndex === -1 || endIndex === -1) return { display: 'none' }

  const width = (endIndex - startIndex + 1) * cellWidth.value
  const left = startIndex * cellWidth.value

  return {
    left: left + 'px',
    width: width + 'px',
    backgroundColor: '#67c23a',
    opacity: 0.8
  }
}

const fetchGanttData = async () => {
  const [pRes, aRes, eRes] = await Promise.all([
    getProjects(),
    getAssignments(),
    getEmployees()
  ])
  projects.value = pRes.data
  assignments.value = aRes.data
  employees.value = eRes.data
}

const displayMonths = computed(() => {
  const months = []
  let current = Number(rangeStart.value)
  const end = Number(rangeEnd.value)
  
  while (current <= end) {
    months.push(current)
    let year = Math.floor(current / 100)
    let month = current % 100
    month++
    if (month > 12) {
      month = 1
      year++
    }
    current = year * 100 + month
  }
  return months
})

const getProjectBar = (project) => {
  const pStart = Number(project.startYm)
  const pEnd = project.endYm ? Number(project.endYm) : Number(rangeEnd.value)
  const viewStart = Number(rangeStart.value)
  const viewEnd = Number(rangeEnd.value)
  
  if (pStart > viewEnd || pEnd < viewStart) return null
  
  const actualStart = Math.max(pStart, viewStart)
  const actualEnd = Math.min(pEnd, viewEnd)
  
  return { start: actualStart, end: actualEnd }
}

const getProjectBarStyle = (project) => {
  const bar = getProjectBar(project)
  if (!bar) return {}
  
  const startIndex = displayMonths.value.indexOf(bar.start)
  const endIndex = displayMonths.value.indexOf(bar.end)
  
  if (startIndex === -1 || endIndex === -1) return { display: 'none' }
  
  const width = (endIndex - startIndex + 1) * cellWidth.value
  const left = startIndex * cellWidth.value
  
  let color = '#409eff'
  if (project.status === '完了') color = '#909399'
  if (project.status === '中断') color = '#f56c6c'
  if (project.status === '提案中') color = '#e6a23c'

  return {
    left: left + 'px',
    width: width + 'px',
    backgroundColor: color
  }
}

onMounted(fetchGanttData)
</script>

<style scoped>
.gantt-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}
.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.range-separator {
  color: #606266;
}
.gantt-wrapper {
  overflow-x: auto;
  border: 1px solid #eee;
}
.gantt-header, .project-row {
  display: flex;
  width: fit-content;
}
.project-name-col {
  width: 200px;
  flex-shrink: 0;
  padding: 10px;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  font-weight: bold;
  background: #f9f9f9;
  position: sticky;
  left: 0;
  z-index: 2;
}
.months-container {
  display: flex;
  position: relative;
}
.month-col, .month-cell {
  width: v-bind('cellWidth + "px"');
  flex-shrink: 0;
  text-align: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  font-size: 12px;
}
.month-col {
  background: #f5f7fa;
  font-weight: bold;
}
.project-bar, .assignment-bar {
  position: absolute;
  top: 8px;
  height: 24px;
  border-radius: 4px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: all 0.3s;
}
.project-bar:hover, .assignment-bar:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}
.assignment-row {
  display: flex;
  width: fit-content;
  background: #fafafa;
}
.assignment-name {
  padding-left: 30px !important;
  font-weight: normal !important;
  color: #666;
  font-size: 13px;
}
.project-row {
  cursor: pointer;
}
.project-row:hover {
  background: #f0f7ff;
}
.project-name-col {
  display: flex;
  align-items: center;
  gap: 5px;
}
.bar-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 5px;
}
.gantt-body {
  border-left: 1px solid #eee;
}
</style>
