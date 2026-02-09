import request from '@/api/axios'

export function getProjects() {
  return request.get('/projects')
}

export function createProject(data) {
  return request.post('/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/projects/${id}`, data)
}

export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}

export function importProjects(data) {
  return request.post('/projects/import', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportProjects() {
  return request.get('/projects/export', { responseType: 'blob' })
}

export function importProjectPlans(data) {
  return request.post('/projects/plans/import', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportProjectPlans() {
  return request.get('/projects/plans/export', { responseType: 'blob' })
}
