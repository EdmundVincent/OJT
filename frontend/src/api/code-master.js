import request from '@/api/axios'

export function getCodeMasters(categoryId) {
  return request.get('/code-master', { params: { categoryId } })
}

export function importCodeMasters(data) {
  return request.post('/code-master/import', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportCodeMasters() {
  return request.get('/code-master/export', { responseType: 'blob' })
}
