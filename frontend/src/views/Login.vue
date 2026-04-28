<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-left">
        <div class="logo-section">
          <el-icon :size="48" class="logo-icon"><Document /></el-icon>
          <h1 class="logo-title">旅行日记</h1>
          <p class="logo-desc">记录每一次美好旅程</p>
        </div>
        
        <div class="feature-list">
          <div class="feature-item">
            <el-icon :size="24"><Camera /></el-icon>
            <span>批量上传照片，自动生成时间线</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><MapLocation /></el-icon>
            <span>标记打卡地点，生成地图轨迹</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><Share /></el-icon>
            <span>一键分享旅程，导出PDF文档</span>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-form-wrapper">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">请登录您的账户</p>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名或邮箱"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            还没有账户？
            <router-link to="/register" class="link-text">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      const result = await userStore.handleLogin({
        username: loginForm.username,
        password: loginForm.password
      })
      
      loading.value = false
      
      if (result.success) {
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/trips'
        router.push(redirect)
      } else {
        ElMessage.error(result.message)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.login-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 960px;
  background: $bg-white;
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;
  overflow: hidden;
}

.login-left {
  width: 50%;
  padding: 48px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 48px;
}

.logo-icon {
  margin-bottom: 16px;
}

.logo-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.logo-desc {
  font-size: 16px;
  opacity: 0.9;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
  
  el-icon {
    opacity: 1;
  }
}

.login-right {
  width: 50%;
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form-wrapper {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 32px;
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.login-btn {
  width: 100%;
  font-weight: 500;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: $text-secondary;
  margin-top: 24px;
}

.link-text {
  color: $primary-color;
  font-weight: 500;
  margin-left: 4px;
  
  &:hover {
    color: $primary-dark;
  }
}

@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
  }
  
  .login-left,
  .login-right {
    width: 100%;
  }
  
  .login-left {
    padding: 32px;
  }
  
  .feature-list {
    display: none;
  }
  
  .login-right {
    padding: 32px;
  }
}
</style>
