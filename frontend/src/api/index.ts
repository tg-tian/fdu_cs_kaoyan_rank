import axios from 'axios'
import type { AxiosResponse } from 'axios'

export const apiClient = axios.create()

export interface Result<T> {
  code: number
  message: string
  data: T
}

export const getToken = () => {
  const token = localStorage.getItem('token')
  if (!token) throw new Error('未检测到登录信息')
  return token
}

export const resolveResult = <T>(response: AxiosResponse<Result<T>>): T => {
  if (response.status === 401) {
    throw new Error('登录过期')
  }
  const result = response.data
  if (result.code === 200) {
    if (result.data !== undefined && result.data !== null) 
      return result.data
  }
  throw new Error(result.message || '请求失败')
}
