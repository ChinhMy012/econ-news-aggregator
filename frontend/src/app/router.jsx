import { EventsAdminPage } from '../features/events/pages/EventsAdminPage'
import { EventAdminDetailPage } from '../features/events/pages/EventAdminDetailPage'
import { DocumentsPage } from '../features/documents/pages/DocumentsPage'
import { PublicEventsPage, PublicEventDetailPage } from '../features/events/pages/PublicEventsPage'
import { ArticleDetailPage } from '../features/articles/pages/ArticleDetailPage'
import { PendingArticlesPage } from '../features/articles/pages/PendingArticlesPage'
import { Navigate, Route, Routes, Link } from 'react-router-dom'
import { RequireAdmin } from '../auth/RequireAdmin'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { SourcesPage } from '../features/sources/pages/SourcesPage'
import { AdminHomePage } from '../features/auth/pages/AdminHomePage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/events" replace />} />
      <Route path="/events" element={<PublicEventsPage />} />
      <Route path="/events/:id" element={<PublicEventDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAdmin />}>
        <Route path="/admin" element={<AdminHomePage />} />
        <Route path="/admin/events" element={<EventsAdminPage />} />
        <Route path="/admin/events/:id" element={<EventAdminDetailPage />} />
        <Route path="/admin/documents" element={<DocumentsPage />} />
        <Route path="/admin/sources" element={<SourcesPage />} />
        <Route path="/admin/articles" element={<PendingArticlesPage />} />
        <Route path="/admin/articles/:id" element={<ArticleDetailPage />} />
      </Route>
      <Route
        path="*"
        element={
          <main className="state-screen">
            <h1>Không tìm thấy trang</h1>
            <Link to="/">Về trang chính</Link>
          </main>
        }
      />
    </Routes>
  )
}
