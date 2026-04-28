import request from '@/utils/request'

export function getTags() {
  return request({
    url: '/tags',
    method: 'get'
  })
}

export function searchTags(keyword) {
  return request({
    url: '/tags/search',
    method: 'get',
    params: { keyword }
  })
}

export function getTagById(id) {
  return request({
    url: `/tags/${id}`,
    method: 'get'
  })
}

export function createTag(data) {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}

export function updateTag(id, data) {
  return request({
    url: `/tags/${id}`,
    method: 'put',
    data
  })
}

export function deleteTag(id) {
  return request({
    url: `/tags/${id}`,
    method: 'delete'
  })
}
