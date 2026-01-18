export interface User {
  id: number
  username: string
  createdAt: string
}

export interface LoginPayload {
  username: string
  password?: string
}

export interface RegisterPayload {
  username: string
  password?: string
}

export interface Category {
  id: number
  name: string
  type: 'INCOME' | 'EXPENSE'
  icon: string
  userId: number
}

export interface Record {
  id: number
  userId: number
  categoryId?: number
  category?: Category
  amount: number
  type: 'INCOME' | 'EXPENSE'
  recordDate: string
  description: string
  createdAt: string
}
