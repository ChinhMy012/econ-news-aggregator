import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/useAuth'
import { AppRoutes } from './app/router'
import './App.css'

function SessionBoundary() {
  const auth = useAuth()
  if (auth.status === 'loading') return <main className="state-screen" aria-live="polite"><span className="spinner" /><h1>Đang kiểm tra đăng nhập…</h1></main>
  if (auth.status === 'error') return <main className="state-screen"><h1>Chưa kết nối được</h1><p role="alert">{auth.notice}</p>
    <button className="primary-button" onClick={() => void auth.retry()}>Thử lại</button></main>
  return <AppRoutes />
}

export default function App() {
  return <BrowserRouter><AuthProvider><SessionBoundary /></AuthProvider></BrowserRouter>
}
