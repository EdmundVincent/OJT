import request from '@/api/axios'

export function getAssignments(params) {
  return request.get('/assignments', { params })
}

export function createAssignment(data) {
  return request.post('/assignments', data)
}

export function updateAssignment(id, data) {
  return request.put(`/assignments/${id}`, data)
}

export function deleteAssignment(id) {
  return request.delete(`/assignments/${id}`)
}

export function importAssignments(data) {
  return request.post('/assignments/import', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportAssignments() {
  return request.get('/assignments/export', { responseType: 'blob' })
}
