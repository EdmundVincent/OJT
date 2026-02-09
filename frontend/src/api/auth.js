import request from '@/api/axios'

export function login(email, password) {
  return request.post('/auth/login', { email, password })
}
