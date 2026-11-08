import { Navigate, Route, Routes, Link } from 'react-router-dom'
import { RequireAdmin } from '../auth/RequireAdmin'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { AdminHomePage } from '../features/auth/pages/AdminHomePage'

export function AppRoutes() {
  return <Routes>
    <Route path="/" element={<Navigate to="/admin" replace />} />
    <Route path="/login" element={<LoginPage />} />
    <Route element={<RequireAdmin />}><Route path="/admin" element={<AdminHomePage />} /></Route>
    <Route path="*" element={<main className="state-screen"><h1>Không tìm thấy trang</h1><Link to="/">Về trang chính</Link></main>} />
  </Routes>
}
