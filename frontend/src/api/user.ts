import type { LoginParams } from '../entities/login'

export interface LoginUpdate {
  type: string;
  data?: any;
}

export const loginAndGetToken = async (
  data: LoginParams,
  onUpdate: (update: LoginUpdate) => void
): Promise<string> => {
  const response = await fetch('/api/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })

  if (!response.body) throw new Error('浏览器不支持流式读取')

  const contentType = response.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    const data = await response.json()
    if (data.code !== 200) {
      throw new Error(data.message || '登录失败')
    }
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''

      for (const part of parts) {
        const lines = part.split('\n')
        let event = ''
        let eventData = ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            event = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            eventData = line.slice(5).trim()
          }
        }
        onUpdate({ type: event, data: eventData })
        if (event === 'success') {
          return eventData
        }
        if (event === 'error') {
          throw new Error(eventData)
        }
      }
    }
  } finally {
    reader.cancel()
  }

  throw new Error('连接断开，未收到登录令牌')
}
