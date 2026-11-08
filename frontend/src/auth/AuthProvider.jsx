import { useEffect, useSyncExternalStore } from 'react'
import { AuthContext } from './AuthContext'
import { getSessionSnapshot, subscribeSession, restoreSession, loginSession, logoutSession } from './authSession'

export function AuthProvider({ children }) {
  const session = useSyncExternalStore(subscribeSession, getSessionSnapshot, getSessionSnapshot)
  useEffect(() => { void restoreSession() }, [])
  return <AuthContext.Provider value={{ status: session.status, user: session.user, notice: session.notice,
    login: loginSession, logout: logoutSession, retry: restoreSession }}>{children}</AuthContext.Provider>
}
