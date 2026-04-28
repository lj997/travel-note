import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/users/me',
    method: 'get'
  })
}

export function updateProfile(data) {
  return request({
    url: '/users/me',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/users/me/change-password',
    method: 'post',
    data
  })
}
