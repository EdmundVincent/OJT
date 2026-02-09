import request from '@/api/axios'

export function getResourceAllocation(params) {
  return request.post('/reports/resource-allocation', params)
}

export function getBudgetMismatch(params) {
  return request.post('/reports/budget-mismatch', params)
}

export function getProjectCost(params) {
  return request.post('/reports/project-cost', params)
}

export function exportResourceAllocation(params) {
  return request.post('/reports/resource-allocation/export', params, { responseType: 'blob' })
}

export function exportBudgetMismatch(params) {
  return request.post('/reports/budget-mismatch/export', params, { responseType: 'blob' })
}

export function exportProjectCost(params) {
  return request.post('/reports/project-cost/export', params, { responseType: 'blob' })
}
