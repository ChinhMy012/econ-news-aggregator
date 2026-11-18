import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth'

export function RequireAdmin() {
  const auth = useAuth()
  const location = useLocation()
  if (auth.status !== 'authenticated' || auth.user?.role !== 'ADMIN') {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return <Outlet />
}
