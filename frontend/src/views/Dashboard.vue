<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        router
        :default-active="$route.path"
        class="el-menu-vertical"
      >
        <el-menu-item index="/employees">
          <el-icon><User /></el-icon>
          <span>要員一覧</span>
        </el-menu-item>
        <el-sub-menu index="/projects">
          <template #title>
            <el-icon><Briefcase /></el-icon>
            <span>プロジェクト</span>
          </template>
          <el-menu-item index="/projects">プロジェクト一覧</el-menu-item>
          <el-menu-item index="/projects/plans">計画管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/assignments">
          <el-icon><Connection /></el-icon>
          <span>アサイン管理</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><DataAnalysis /></el-icon>
          <span>レポート</span>
        </el-menu-item>
        <el-sub-menu index="/master">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>マスタ管理</span>
          </template>
          <el-menu-item index="/master/users">利用者マスタ</el-menu-item>
          <el-menu-item index="/master/codes">コードマスタ</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <h3>要員アサイン管理システム</h3>
          <div class="user-info">
            <span>{{ user?.email }}</span>
            <el-button type="text" @click="handleLogout">Logout</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
}
.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-menu-vertical {
  height: 100%;
}
</style>
