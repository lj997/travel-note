import request from '@/utils/request'

export function getTrips() {
  return request({
    url: '/trips',
    method: 'get'
  })
}

export function getTripById(id) {
  return request({
    url: `/trips/${id}`,
    method: 'get'
  })
}

export function getSharedTrip(token) {
  return request({
    url: `/trips/shared/${token}`,
    method: 'get'
  })
}

export function createTrip(data) {
  return request({
    url: '/trips',
    method: 'post',
    data
  })
}

export function updateTrip(id, data) {
  return request({
    url: `/trips/${id}`,
    method: 'put',
    data
  })
}

export function deleteTrip(id) {
  return request({
    url: `/trips/${id}`,
    method: 'delete'
  })
}

export function generateShareToken(tripId) {
  return request({
    url: `/trips/${tripId}/share`,
    method: 'post'
  })
}

export function revokeShareToken(tripId) {
  return request({
    url: `/trips/${tripId}/unshare`,
    method: 'post'
  })
}
