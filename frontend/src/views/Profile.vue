<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="user-info">
        <div class="avatar-section">
          <el-avatar :size="80" class="user-avatar">
            <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" />
            <span v-else>{{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) }}</span>
          </el-avatar>
          <el-button type="primary" text size="small">更换头像</el-button>
        </div>
        
        <div class="info-section">
          <h2 class="user-name">{{ userInfo?.nickname || userInfo?.username }}</h2>
          <p class="user-username">@{{ userInfo?.username }}</p>
          <p v-if="userInfo?.bio" class="user-bio">{{ userInfo.bio }}</p>
          <p class="user-email">
            <el-icon><Message /></el-icon>
            {{ userInfo?.email }}
          </p>
        </div>
      </div>
    </div>
    
    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="基本信息" name="info">
        <el-card shadow="never" class="form-card">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
            </el-form-item>
            
            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" size="large" :loading="saving" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="修改密码" name="password">
        <el-card shadow="never" class="form-card">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                size="large"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                size="large"
                show-password
              />
              <p class="form-hint">密码长度至少6位，建议包含字母和数字</p>
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" size="large" :loading="changingPassword" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="账户设置" name="settings">
        <el-card shadow="never" class="form-card">
          <div class="settings-list">
            <div class="setting-item">
              <div class="setting-info">
                <h4>账户状态</h4>
                <p>当前账户处于正常状态</p>
              </div>
              <el-tag type="success">正常</el-tag>
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h4>注册时间</h4>
                <p>{{ formatDate(userInfo?.createdAt) }}</p>
              </div>
            </div>
            
            <div class="setting-item danger">
              <div class="setting-info">
                <h4>危险操作</h4>
                <p>注销账户将永久删除您的所有数据，此操作不可恢复</p>
              </div>
              <el-button type="danger" text>注销账户</el-button>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword } from '@/api/auth'
import dayjs from 'dayjs'

const userStore = useUserStore()

const activeTab = ref('info')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const saving = ref(false)
const changingPassword = ref(false)

const userInfo = computed(() => userStore.userInfo)

const profileForm = reactive({
  nickname: '',
  bio: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  nickname: [
    { max: 50, message: '昵称不能超过50个字符', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6-100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadFormData = () => {
  if (userInfo.value) {
    profileForm.nickname = userInfo.value.nickname || ''
    profileForm.bio = userInfo.value.bio || ''
  }
}

const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      
      try {
        const response = await updateProfile({
          nickname: profileForm.nickname || undefined,
          bio: profileForm.bio || undefined
        })
        
        userStore.updateUserInfo(response.data)
        ElMessage.success('保存成功')
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        
        ElMessage.success('密码修改成功')
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY年MM月DD日 HH:mm')
}

watch(userInfo, () => {
  loadFormData()
}, { immediate: true })

onMounted(() => {
  loadFormData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.profile-page {
  max-width: 800px;
}

.profile-header {
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%);
  border-radius: $radius-lg;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.user-info {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 32px;
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.info-section {
  flex: 1;
}

.user-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px;
}

.user-username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 8px;
}

.user-bio {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 8px;
  line-height: 1.5;
}

.user-email {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.profile-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
    background: $bg-white;
    border-radius: $radius-md;
    padding: 0 16px;
  }
}

.form-card {
  background: $bg-white;
  border-radius: $radius-lg;
  padding: 24px;
}

.profile-form,
.password-form {
  max-width: 500px;
}

.form-hint {
  font-size: 12px;
  color: $text-muted;
  margin-top: 4px;
}

.settings-list {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid $border-light;
  
  &:last-child {
    border-bottom: none;
  }
  
  &.danger {
    .setting-info {
      h4 {
        color: $danger-color;
      }
    }
  }
}

.setting-info {
  h4 {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 4px;
    color: $text-primary;
  }
  
  p {
    font-size: 13px;
    color: $text-secondary;
    margin: 0;
  }
}
</style>
