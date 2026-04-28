import request from '@/utils/request'

export function getJournals(tripId) {
  return request({
    url: `/trips/${tripId}/journals`,
    method: 'get'
  })
}

export function getJournalById(tripId, entryId) {
  return request({
    url: `/trips/${tripId}/journals/${entryId}`,
    method: 'get'
  })
}

export function createJournal(tripId, data) {
  return request({
    url: `/trips/${tripId}/journals`,
    method: 'post',
    data
  })
}

export function updateJournal(tripId, entryId, data) {
  return request({
    url: `/trips/${tripId}/journals/${entryId}`,
    method: 'put',
    data
  })
}

export function deleteJournal(tripId, entryId) {
  return request({
    url: `/trips/${tripId}/journals/${entryId}`,
    method: 'delete'
  })
}
