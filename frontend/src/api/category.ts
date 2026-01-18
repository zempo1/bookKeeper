import apiClient from './client'
import type { Category } from '@/types'

export const categoryApi = {
  getCategories(userId: number) {
    return apiClient.get(`/categories?userId=${userId}`)
  },
  createCategory(category: Partial<Category>) {
    return apiClient.post('/categories', category)
  },
  deleteCategory(id: number) {
    return apiClient.delete(`/categories/${id}`)
  }
}
