import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/auth'

export function uploadFile(file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export function uploadMultipleFiles(files, onUploadProgress) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/files/upload-multiple',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

export function deletePhoto(photoId) {
  return request({
    url: `/files/photos/${photoId}`,
    method: 'delete'
  })
}

export function uploadFileWithAxios(file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  const token = getToken()
  
  return axios.post('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    onUploadProgress
  })
}
