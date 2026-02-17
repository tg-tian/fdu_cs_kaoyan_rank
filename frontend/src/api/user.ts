import axios from 'axios'

export interface LoginParams {
  examNo: string;
  idCard: string;
}

export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

export const loginAndGetToken = async (data: LoginParams): Promise<string> => {
  const response = await axios.post<Result<string>>('/api/user/login', data)
  const result = response.data
  if (result.code === 200) {
    if (result.data) return result.data
  }
  throw new Error(result.message || '登录失败')
}
