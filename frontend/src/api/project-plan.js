import request from '@/api/axios'

export function getProjectPlans(projectId) {
  return request.get(`/projects/${projectId}/plans`)
}

export function createProjectPlan(projectId, data) {
  return request.post(`/projects/${projectId}/plans`, data)
}

export function deleteProjectPlan(projectId, planId) {
  return request.delete(`/projects/plans/${planId}`)
}
