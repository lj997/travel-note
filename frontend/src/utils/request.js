import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken } from './auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.success) {
      return res
    } else {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 3 * 1000
      })
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('Response error:', error)
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessageBox.confirm(
            '登录状态已过期，您可以继续留在该页面，或者重新登录',
            '系统提示',
            {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            localStorage.removeItem('travel_note_token')
            router.push({ name: 'Login' })
          })
          break
        case 403:
          ElMessage({
            message: '没有权限访问此资源',
            type: 'error'
          })
          break
        case 404:
          ElMessage({
            message: '请求的资源不存在',
            type: 'error'
          })
          break
        case 500:
          ElMessage({
            message: '服务器内部错误',
            type: 'error'
          })
          break
        default:
          ElMessage({
            message: error.response.data?.message || '请求失败',
            type: 'error'
          })
      }
    } else {
      ElMessage({
        message: '网络连接失败，请检查网络',
        type: 'error'
      })
    }
    
    return Promise.reject(error)
  }
)

export default request
