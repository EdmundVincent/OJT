import request from '@/api/axios'

export function getEmployees() {
  return request.get('/employees')
}

export function createEmployee(data) {
  return request.post('/employees', data)
}

export function updateEmployee(id, data) {
  return request.put(`/employees/${id}`, data)
}

export function deleteEmployee(id) {
  return request.delete(`/employees/${id}`)
}

export function exportEmployees() {
  return request.get('/employees/export', { responseType: 'blob' })
}

export function importEmployees(formData) {
  return request.post('/employees/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getEmployeeHistory(id) {
  return request.get(`/employees/${id}/history`)
}
