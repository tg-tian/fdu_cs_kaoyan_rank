import { apiClient, getToken, resolveResult } from './index'
import type { Result } from './index'
import type { ScoreItem } from '../entities/score'

export const getMyScore = async (): Promise<ScoreItem> => {
  const token = getToken()
  const response = await apiClient.get<Result<ScoreItem>>('/api/score/me', {
    headers: { token }
  })
  return resolveResult<ScoreItem>(response.data)
}

export const getAllScores = async (): Promise<ScoreItem[]> => {
  const token = getToken()
  const response = await apiClient.get<Result<ScoreItem[]>>('/api/score/all', {
    headers: { token }
  })
  return resolveResult<ScoreItem[]>(response.data)
}
