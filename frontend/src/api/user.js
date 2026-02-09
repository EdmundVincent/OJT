import axios from './axios'

export const getUsers = () => {
  return axios.get('/users')
}

export const createUser = (data) => {
  return axios.post('/users', data)
}

export const updateUser = (email, data) => {
  return axios.put(`/users/${email}`, data)
}

export const deleteUser = (email) => {
  return axios.delete(`/users/${email}`)
}
