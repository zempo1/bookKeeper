import apiClient from './client'
import type { Record } from '@/types'

export const recordApi = {
  getRecords(userId: number, startDate: string, endDate: string) {
    return apiClient.get(`/records`, {
      params: { userId, startDate, endDate }
    })
  },
  createRecord(record: Partial<Record>) {
    return apiClient.post('/records', record)
  },
  updateRecord(id: number, record: Partial<Record>) {
    return apiClient.put(`/records/${id}`, record)
  },
  deleteRecord(id: number) {
    return apiClient.delete(`/records/${id}`)
  }
}
