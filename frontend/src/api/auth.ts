import apiClient from './client'
import type { LoginPayload, RegisterPayload } from '@/types'

export const authApi = {
  login(payload: LoginPayload) {
    return apiClient.post('/auth/login', payload)
  },
  register(payload: RegisterPayload) {
    return apiClient.post('/auth/register', payload)
  }
}
