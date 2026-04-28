import request from '@/utils/request'

export function getLocations(tripId) {
  return request({
    url: `/trips/${tripId}/locations`,
    method: 'get'
  })
}

export function getLocationById(tripId, locationId) {
  return request({
    url: `/trips/${tripId}/locations/${locationId}`,
    method: 'get'
  })
}

export function createLocation(tripId, data) {
  return request({
    url: `/trips/${tripId}/locations`,
    method: 'post',
    data
  })
}

export function updateLocation(tripId, locationId, data) {
  return request({
    url: `/trips/${tripId}/locations/${locationId}`,
    method: 'put',
    data
  })
}

export function deleteLocation(tripId, locationId) {
  return request({
    url: `/trips/${tripId}/locations/${locationId}`,
    method: 'delete'
  })
}

export function reorderLocations(tripId, locationIds) {
  return request({
    url: `/trips/${tripId}/locations/reorder`,
    method: 'post',
    data: locationIds
  })
}
