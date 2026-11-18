import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../../auth/useAuth'

export function LoginPage() {
  const auth = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [visible, setVisible] = useState(false)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const [logoutPending, setLogoutPending] = useState(false)
  if (auth.status === 'authenticated') return <Navigate to="/admin" replace />

  async function submit(event) {
    event.preventDefault()
    if (pending) return
    setError(null)
    setPending(true)
    try {
      const user = await auth.login(username, password)
      setPassword('')
      const from = location.state?.from
      if (user) navigate(typeof from === 'string' && from.startsWith('/admin') ? from : '/admin', { replace: true })
    } catch (failure) { setError(failure) } finally { setPending(false) }
  }
  const fieldError = (name) => error?.errors?.find((item) => item.field === name)?.message
  async function finishLogout() {
    setLogoutPending(true)
    try { await auth.logout() } catch (failure) { setError(failure) } finally { setLogoutPending(false) }
  }

  return <main className="login-layout">
    <section className="intro-panel">
      <a className="brand" href="/">ECON<span>NEWS</span></a>
      <div className="intro-copy"><p className="eyebrow">GÓC LÀM VIỆC CỦA NGƯỜI BIÊN TẬP</p>
        <h1>Theo dõi thông tin.<br /><span>Kết nối diễn biến.</span></h1>
        <p>Tổng hợp tin kinh tế, đối chiếu nguồn và xây dựng dòng thời gian cho những câu chuyện đáng theo dõi.</p>
        <div className="topic-list"><span>Chính sách kinh tế</span><span>Hoạt động doanh nghiệp</span></div>
      </div>
      <p className="intro-footer">EconNews · Dự án tổng hợp và theo dõi sự kiện kinh tế</p>
    </section>
    <section className="form-panel">
      <div className="login-card"><p className="eyebrow">TRANG QUẢN TRỊ</p><h2>Đăng nhập</h2>
        <p className="muted">Sử dụng tài khoản quản trị đã được cấp.</p>
        {auth.notice && <div className="notice" role="status">{auth.notice}</div>}
        {error && <div className="error-banner" role="alert">{error.message}
          {error.retryAfter && <p>Thử lại sau khoảng {error.retryAfter} giây.</p>}
          {error.requestId && <small>Mã tra cứu: {error.requestId}</small>}
        </div>}
        <form onSubmit={submit}>
          <label htmlFor="username">Tên đăng nhập</label>
          <input id="username" name="username" autoComplete="username" value={username}
            maxLength={100} required disabled={pending} onChange={(event) => setUsername(event.target.value)}
            aria-invalid={Boolean(fieldError('username'))} aria-describedby={fieldError('username') ? 'username-error' : undefined} />
          {fieldError('username') && <small id="username-error" className="field-error">{fieldError('username')}</small>}
          <label htmlFor="password">Mật khẩu</label>
          <div className="password-input"><input id="password" name="password" autoComplete="current-password"
            type={visible ? 'text' : 'password'} value={password} required maxLength={72} disabled={pending}
            onChange={(event) => setPassword(event.target.value)} aria-invalid={Boolean(fieldError('password'))}
            aria-describedby={fieldError('password') ? 'password-error' : undefined} />
            <button className="text-button" type="button" onClick={() => setVisible(!visible)} aria-pressed={visible}>
              {visible ? 'Ẩn' : 'Hiện'}</button></div>
          {fieldError('password') && <small id="password-error" className="field-error">{fieldError('password')}</small>}
          <button className="primary-button" type="submit" disabled={pending}>{pending ? 'Đang đăng nhập…' : 'Đăng nhập'}</button>
        </form>
        <p className="form-note">Chỉ dành cho quản trị viên. Nếu chưa có tài khoản, liên hệ người quản lý hệ thống.</p>
        {auth.notice?.includes('chưa xác nhận') && <button className="secondary-button" disabled={logoutPending} onClick={finishLogout}>
          {logoutPending ? 'Đang xử lý…' : 'Thử đăng xuất trên máy chủ lại'}</button>}
      </div>
    </section>
  </main>
}
