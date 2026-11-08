import { useState } from 'react'
import { useAuth } from '../../../auth/useAuth'
import { authenticatedRequest } from '../../../api/httpClient'

export function AdminHomePage() {
  const auth = useAuth()
  const [check, setCheck] = useState({ pending: false, message: null, error: false })
  const [leaving, setLeaving] = useState(false)
  async function verify() {
    setCheck({ pending: true, message: null, error: false })
    try {
      const response = await authenticatedRequest('/auth/me')
      setCheck({ pending: false, message: `Kết nối thành công. Tài khoản ${response.data.username} có quyền quản trị.`, error: false })
    } catch (failure) { setCheck({ pending: false, message: failure.message, error: true }) }
  }
  async function logout() {
    setLeaving(true)
    try { await auth.logout() } catch { /* Thông báo được giữ trên trang đăng nhập. */ }
    finally { setLeaving(false) }
  }
  return <div className="admin-shell">
    <header className="admin-header"><a className="brand" href="/admin">ECON<span>NEWS</span></a>
      <div className="header-actions"><span>{auth.user.username}</span>
        <button className="secondary-button" onClick={logout} disabled={leaving}>{leaving ? 'Đang đăng xuất…' : 'Đăng xuất'}</button></div>
    </header>
    <main className="admin-main"><p className="eyebrow">KHÔNG GIAN QUẢN TRỊ</p>
      <h1>Xin chào, {auth.user.username}.</h1><p className="muted">Bạn đã đăng nhập vào EconNews.</p>
      <section className="session-card"><div><span className="status-badge">Đã xác thực</span>
        <h2>Thông tin tài khoản</h2><dl><div><dt>Tên đăng nhập</dt><dd>{auth.user.username}</dd></div>
          <div><dt>Quyền</dt><dd>Quản trị viên</dd></div><div><dt>Mã tài khoản</dt><dd>{auth.user.id}</dd></div></dl>
      </div><div className="connection-panel"><h3>Kiểm tra phiên</h3><p className="muted">Xác nhận tài khoản vẫn có quyền truy cập trên máy chủ.</p>
        <button className="primary-button" onClick={verify} disabled={check.pending}>{check.pending ? 'Đang kiểm tra…' : 'Kiểm tra kết nối'}</button>
        {check.message && <p className={check.error ? 'field-error' : 'success-message'} role={check.error ? 'alert' : 'status'}>{check.message}</p>}
      </div></section>
      <section className="next-section"><p className="eyebrow">GIAI ĐOẠN TIẾP THEO</p><h2>Quản lý nội dung</h2>
        <p className="muted">Các màn hình nguồn RSS, duyệt bài và sự kiện sẽ được bổ sung trong các bước triển khai tiếp theo.</p>
      </section>
    </main>
  </div>
}
