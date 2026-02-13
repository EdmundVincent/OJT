import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import EmployeeList from '@/views/EmployeeList.vue'
import ProjectList from '@/views/ProjectList.vue'
import ProjectPlanList from '@/views/ProjectPlanList.vue'
import AssignmentList from '@/views/AssignmentList.vue'
import CodeMasterList from '@/views/CodeMasterList.vue'
import UserList from '@/views/UserList.vue'
import CustomerList from '@/views/CustomerList.vue'
import ReportView from '@/views/ReportView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Dashboard,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/employees' },
      { path: 'employees', name: 'Employees', component: EmployeeList },
      { path: 'projects', name: 'Projects', component: ProjectList },
      { path: 'projects/plans', name: 'ProjectPlans', component: ProjectPlanList },
      { path: 'assignments', name: 'Assignments', component: AssignmentList },
      { path: 'master/codes', name: 'CodeMaster', component: CodeMasterList },
      { path: 'master/users', name: 'UserMaster', component: UserList },
      { path: 'master/customers', name: 'CustomerMaster', component: CustomerList },
      { path: 'reports', name: 'Reports', component: ReportView }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
