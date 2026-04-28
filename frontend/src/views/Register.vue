<template>
  <div class="register-container">
    <div class="register-wrapper">
      <div class="register-left">
        <div class="logo-section">
          <el-icon :size="48" class="logo-icon"><Document /></el-icon>
          <h1 class="logo-title">旅行日记</h1>
          <p class="logo-desc">记录每一次美好旅程</p>
        </div>
        
        <div class="quote-section">
          <el-icon :size="32" class="quote-icon"><ChatDotRound /></el-icon>
          <p class="quote-text">旅行的意义不在于到达，而在于沿途的风景和看风景的心情。</p>
        </div>
      </div>
      
      <div class="register-right">
        <div class="register-form-wrapper">
          <h2 class="form-title">创建账户</h2>
          <p class="form-subtitle">加入我们，开始记录您的旅程</p>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱"
                size="large"
                prefix-icon="Message"
              />
            </el-form-item>
            
            <el-form-item prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="昵称（可选）"
                size="large"
                prefix-icon="UserFilled"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                size="large"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="register-btn"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            已有账户？
            <router-link to="/login" class="link-text">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      
      const result = await userStore.handleRegister({
        username: registerForm.username,
        email: registerForm.email,
        nickname: registerForm.nickname || registerForm.username,
        password: registerForm.password
      })
      
      loading.value = false
      
      if (result.success) {
        ElMessage.success('注册成功')
        router.push('/trips')
      } else {
        ElMessage.error(result.message)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.register-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.register-wrapper {
  display: flex;
  width: 100%;
  max-width: 1000px;
  background: $bg-white;
  border-radius: $radius-xl;
  box-shadow: $shadow-lg;
  overflow: hidden;
}

.register-left {
  width: 45%;
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
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.logo-desc {
  font-size: 14px;
  opacity: 0.9;
}

.quote-section {
  text-align: center;
}

.quote-icon {
  margin-bottom: 16px;
  opacity: 0.7;
}

.quote-text {
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.9;
  font-style: italic;
}

.register-right {
  width: 55%;
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-form-wrapper {
  width: 100%;
  max-width: 360px;
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

.register-form {
  .el-form-item {
    margin-bottom: 16px;
  }
}

.register-btn {
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
  .register-wrapper {
    flex-direction: column;
  }
  
  .register-left,
  .register-right {
    width: 100%;
  }
  
  .register-left {
    padding: 24px;
  }
  
  .quote-section {
    display: none;
  }
  
  .register-right {
    padding: 32px;
  }
}
</style>
