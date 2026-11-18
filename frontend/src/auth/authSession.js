import { ApiError } from '../api/apiError'
import { requestJson } from '../api/requestJson'

// Access token chỉ tồn tại trong bộ nhớ; refresh token luôn ở cookie HttpOnly.
let snapshot = { status: 'loading', user: null, accessToken: null, expiresAt: 0, notice: null }
const listeners = new Set()
let generation = 0
let refreshPromise = null
let logoutPending = false
let lastEventAt = 0
const channel = typeof BroadcastChannel === 'undefined' ? null : new BroadcastChannel('econnews-auth')

export function getSessionSnapshot() { return snapshot }
export function subscribeSession(listener) { listeners.add(listener); return () => listeners.delete(listener) }
function update(next) { snapshot = next; listeners.forEach((listener) => listener()) }
function stamp() { return performance.timeOrigin + performance.now() }
function anonymous(notice = null) {
  return { status: 'anonymous', user: null, accessToken: null, expiresAt: 0, notice }
}
function send(type) {
  lastEventAt = stamp()
  channel?.postMessage({ type, at: lastEventAt, session: type === 'session' ? snapshot : undefined })
}
function clear(notice = null, broadcast = true) {
  generation++
  update(anonymous(notice))
  if (broadcast) send('logout')
}
function applyLogin(data) {
  if (typeof data?.accessToken !== 'string' || data.tokenType !== 'Bearer'
      || !Number.isFinite(data.expiresIn) || data.expiresIn <= 0 || data.user?.role !== 'ADMIN') {
    throw new ApiError('Dữ liệu phiên đăng nhập không hợp lệ.', { code: 'INVALID_RESPONSE' })
  }
  update({ status: 'authenticated', user: data.user, accessToken: data.accessToken,
    expiresAt: Date.now() + data.expiresIn * 1000, notice: null })
  send('session')
  return snapshot.accessToken
}
function validToken() {
  return snapshot.accessToken && snapshot.expiresAt > Date.now() + 10000
}
async function withSessionLock(action) {
  if (!navigator.locks?.request) {
    throw new ApiError('Vui lòng dùng trình duyệt hiện đại trên localhost hoặc HTTPS để đăng nhập an toàn giữa nhiều tab.', { code: 'BROWSER_UNSUPPORTED' })
  }
  return navigator.locks.request('econnews-auth-cookie', { mode: 'exclusive' }, action)
}
async function cookiePost(path, body) {
  const csrf = (await requestJson('/auth/csrf')).data
  if (typeof csrf?.headerName !== 'string' || typeof csrf.token !== 'string') {
    throw new ApiError('Không lấy được thông tin bảo vệ yêu cầu.', { code: 'INVALID_CSRF_RESPONSE' })
  }
  return requestJson(path, { method: 'POST', body, headers: { [csrf.headerName]: csrf.token } })
}

export async function loginSession(username, password) {
  return withSessionLock(async () => {
    const started = generation
    const response = await cookiePost('/auth/login', { username, password })
    if (started !== generation || logoutPending) return null
    applyLogin(response.data)
    return response.data.user
  })
}

export function refreshSession(failedToken = null) {
  if (refreshPromise) return refreshPromise
  const started = generation
  refreshPromise = withSessionLock(async () => {
    if (started !== generation || logoutPending) return null
    if (validToken() && snapshot.accessToken !== failedToken) return snapshot.accessToken
    try {
      const response = await cookiePost('/auth/refresh')
      if (started !== generation || logoutPending) return null
      return applyLogin(response.data)
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        clear(snapshot.status === 'authenticated' ? 'Phiên đã kết thúc. Vui lòng đăng nhập lại.' : null)
        return null
      }
      throw error // Không tự retry nếu kết quả đổi token qua mạng chưa rõ.
    }
  }).finally(() => { refreshPromise = null })
  return refreshPromise
}

export async function restoreSession() {
  if (snapshot.status === 'error') update({ ...anonymous(), status: 'loading' })
  try { await refreshSession() } catch (error) {
    update({ ...anonymous(), status: 'error', notice: error.message })
  }
}

export async function logoutSession() {
  logoutPending = true
  generation++
  update(anonymous())
  try {
    await withSessionLock(async () => {
      await cookiePost('/auth/logout')
      clear('Bạn đã đăng xuất.')
    })
  } catch (error) {
    clear('Đã đóng giao diện quản trị nhưng chưa xác nhận được đăng xuất trên máy chủ. Vui lòng thử đăng xuất lại khi có kết nối.')
    throw error
  } finally {
    logoutPending = false
  }
}

export function invalidateSession() { clear('Phiên không còn hợp lệ. Vui lòng đăng nhập lại.') }

if (channel) {
  channel.onmessage = ({ data }) => {
    if (!data || !Number.isFinite(data.at) || data.at < lastEventAt) return
    if (data.type === 'logout') {
      lastEventAt = data.at
      clear('Phiên đã kết thúc ở tab khác.', false)
    } else if (data.type === 'session' && !logoutPending
      && typeof data.session?.accessToken === 'string'
      && data.session.user?.role === 'ADMIN' && data.session.expiresAt > Date.now()) {
      lastEventAt = data.at
      update({ ...data.session, status: 'authenticated', notice: null })
    }
  }
}
if (import.meta.hot) import.meta.hot.dispose(() => channel?.close())
