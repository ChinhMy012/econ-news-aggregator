import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { authenticatedRequest } from '../../../api/httpClient'
import '../../collections/components/collections.css'
export function PendingArticlesPage() {
  const [page, setPage] = useState(0)
  const [reload, setReload] = useState(0)
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  useEffect(() => {
    const abort = new AbortController()
    authenticatedRequest(`/admin/articles?page=${page}&size=10`, { signal: abort.signal })
      .then((data) => {
        if (!abort.signal.aborted) setResponse(data)
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [page, reload])
  function change(next) {
    setLoading(true)
    setError(null)
    setPage(next)
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <Link to="/admin/sources">Nguồn tin & RSS</Link>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">KẾT QUẢ THU THẬP</p>
            <h1>Bài viết chờ duyệt</h1>
          </div>
          <button
            className="secondary-button"
            disabled={loading}
            onClick={() => {
              setLoading(true)
              setError(null)
              setReload((n) => n + 1)
            }}
          >
            Tải lại
          </button>
        </div>
        <p className="muted">
          Các bài bên dưới đang ở trạng thái PENDING, chưa hiển thị công khai. Chức năng phân loại
          và duyệt bài sẽ làm ở bước tiếp theo.
        </p>
        {error && (
          <div className="field-error" role="alert">
            <p>{error.message}</p>
            {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
          </div>
        )}
        {loading ? (
          <p role="status">Đang tải bài viết…</p>
        ) : (
          !error && (
            <>
              <p>{response?.meta.totalItems ?? 0} bài chờ duyệt</p>
              {response?.data.length === 0 && (
                <p>Chưa có bài. Vào quản lý nguồn RSS và chọn Thu thập ngay.</p>
              )}
              <ul className="pending-list">
                {response?.data.map((article) => (
                  <li key={article.id}>
                    <span className="status-badge">Chờ duyệt</span>
                    <h2>
                      <a href={article.originalUrl} target="_blank" rel="noopener noreferrer">
                        {article.title}
                      </a>
                    </h2>
                    {article.summary && <p className="pending-summary">{article.summary}</p>}
                    <p className="pending-meta">
                      Nguồn #{article.sourceId} · Kênh đầu tiên #{article.firstFeedId}
                      <br />
                      Xuất bản:{' '}
                      {article.publishedAt
                        ? new Date(article.publishedAt).toLocaleString('vi-VN')
                        : 'Chưa có ngày xuất bản'}
                      <br />
                      Thu thập: {new Date(article.collectedAt).toLocaleString('vi-VN')}
                    </p>
                  </li>
                ))}
              </ul>
              {response?.meta.totalPages > 1 && (
                <nav className="catalog-actions" aria-label="Phân trang bài viết">
                  <button
                    className="secondary-button"
                    disabled={page === 0}
                    onClick={() => change(page - 1)}
                  >
                    Trước
                  </button>
                  <span>
                    {page + 1}/{response.meta.totalPages}
                  </span>
                  <button
                    className="secondary-button"
                    disabled={page + 1 >= response.meta.totalPages}
                    onClick={() => change(page + 1)}
                  >
                    Sau
                  </button>
                </nav>
              )}
            </>
          )
        )}
      </main>
    </div>
  )
}
