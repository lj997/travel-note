import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const isAuthenticated = computed(() => !!token.value && !!userInfo.value)

  const handleLogin = async (loginData) => {
    try {
      const response = await login(loginData)
      const { token: jwtToken, userId, username, email, nickname, avatar } = response.data
      
      token.value = jwtToken
      setToken(jwtToken)
      
      userInfo.value = {
        id: userId,
        username,
        email,
        nickname,
        avatar
      }
      
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '登录失败' }
    }
  }

  const handleRegister = async (registerData) => {
    try {
      const response = await register(registerData)
      const { token: jwtToken, userId, username, email, nickname, avatar } = response.data
      
      token.value = jwtToken
      setToken(jwtToken)
      
      userInfo.value = {
        id: userId,
        username,
        email,
        nickname,
        avatar
      }
      
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '注册失败' }
    }
  }

  const fetchUserInfo = async () => {
    if (!token.value) {
      return { success: false }
    }
    
    try {
      const response = await getCurrentUser()
      userInfo.value = response.data
      return { success: true }
    } catch (error) {
      token.value = ''
      userInfo.value = null
      removeToken()
      return { success: false }
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  const updateUserInfo = (info) => {
    userInfo.value = { ...userInfo.value, ...info }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    handleLogin,
    handleRegister,
    fetchUserInfo,
    logout,
    updateUserInfo
  }
}, {
  persist: true
})
