import { ApiError } from './apiError'
import { requestJson } from './requestJson'
import { getSessionSnapshot, invalidateSession, refreshSession } from '../auth/authSession'

export async function authenticatedRequest(path, options = {}) {
  let token = getSessionSnapshot().accessToken
  if (!token) token = await refreshSession()
  if (!token) throw new ApiError('Bạn cần đăng nhập.', { status: 401, code: 'AUTHENTICATION_REQUIRED' })
  const send = (accessToken) => requestJson(path, { ...options,
    headers: { ...options.headers, Authorization: `Bearer ${accessToken}` } })
  try {
    return await send(token)
  } catch (error) {
    if (error.status !== 401) throw error
    if (error.code !== 'ACCESS_TOKEN_EXPIRED') { invalidateSession(); throw error }
    const replacement = await refreshSession(token)
    if (!replacement) throw error
    try { return await send(replacement) } catch (retryError) {
      if (retryError.status === 401) invalidateSession()
      throw retryError
    }
  }
}
