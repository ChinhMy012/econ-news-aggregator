import { ApiError } from './apiError'

export async function requestJson(path, { method = 'GET', body, headers = {}, signal } = {}) {
  let response
  try {
    response = await fetch(`/api/v1${path}`, {
      method,
      credentials: 'same-origin',
      cache: 'no-store',
      headers: { ...(body === undefined ? {} : { 'Content-Type': 'application/json' }), ...headers },
      body: body === undefined ? undefined : JSON.stringify(body),
      signal: signal ?? AbortSignal.timeout(15000),
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError('Không kết nối được máy chủ. Vui lòng kiểm tra kết nối rồi thử lại.')
  }
  if (response.status === 204) return null
  let payload
  try { payload = await response.json() } catch {
    throw new ApiError('Máy chủ trả về dữ liệu không hợp lệ.', { status: response.status, code: 'INVALID_RESPONSE' })
  }
  if (!response.ok || payload.success === false) {
    throw new ApiError(payload.message || 'Không thể thực hiện yêu cầu.', {
      status: response.status,
      code: payload.errorCode || 'HTTP_ERROR',
      errors: Array.isArray(payload.errors) ? payload.errors : [],
      requestId: payload.requestId,
      retryAfter: Number(response.headers.get('Retry-After')) || null,
    })
  }
  return payload
}
