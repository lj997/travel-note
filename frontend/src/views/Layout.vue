<template>
  <el-container class="app-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="app-aside">
      <div class="logo">
        <el-icon :size="32" class="logo-icon"><Document /></el-icon>
        <span v-show="!isCollapse" class="logo-text">旅行日记</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/trips">
          <el-icon><Location /></el-icon>
          <template #title>我的旅程</template>
        </el-menu-item>
        <el-menu-item index="/tags">
          <el-icon><Collection /></el-icon>
          <template #title>标签管理</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
      
      <div class="aside-footer">
        <el-button
          :icon="isCollapse ? 'Expand' : 'Fold'"
          text
          class="collapse-btn"
          @click="toggleCollapse"
        />
      </div>
    </el-aside>
    
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" />
                <span v-else>{{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) }}</span>
              </el-avatar>
              <span class="user-name">{{ userInfo?.nickname || userInfo?.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const userInfo = computed(() => userStore.userInfo)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-container {
  height: 100vh;
}

.app-aside {
  background-color: #1e293b;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  gap: 12px;
}

.logo-icon {
  color: $primary-color;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
  flex: 1;
}

.aside-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
}

.collapse-btn {
  color: #94a3b8;
  font-size: 20px;
}

.app-header {
  background: $bg-white;
  border-bottom: 1px solid $border-light;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: $radius-md;
  transition: background 0.2s;
  
  &:hover {
    background: $bg-gray;
  }
}

.user-avatar {
  background: $primary-color;
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: $font-size-sm;
  color: $text-primary;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: $text-muted;
}

.app-main {
  background: $bg-color;
  padding: 24px;
  overflow-y: auto;
}
</style>
