import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { articlesApi } from '../api/articlesApi'
import { articleStatuses, contentTypes } from '../utils/articleLabels'
import './articles.css'
import '../../collections/components/collections.css'
export function PendingArticlesPage() {
  const [status, setStatus] = useState('PENDING')
  const [page, setPage] = useState(0)
  const [reload, setReload] = useState(0)
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  useEffect(() => {
    const abort = new AbortController()
    articlesApi
      .list(status, page, abort.signal)
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
  }, [status, page, reload])
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
            <h1>Quản lý bài viết</h1>
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
          Chọn bài để xem chi tiết, phân loại và duyệt. Chỉ bài đã công khai mới xuất hiện qua API
          công khai.
        </p>
        <div className="article-filter">
          <label htmlFor="article-status-filter">Trạng thái</label>
          <select
            id="article-status-filter"
            value={status}
            disabled={loading}
            onChange={(event) => {
              setLoading(true)
              setError(null)
              setPage(0)
              setStatus(event.target.value)
            }}
          >
            {Object.entries(articleStatuses).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
            <option value="ALL">Tất cả trạng thái</option>
          </select>
        </div>
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
              <p>{response?.meta.totalItems ?? 0} bài</p>
              {response?.data.length === 0 && <p>Không có bài trong trạng thái đang chọn.</p>}
              <ul className="pending-list">
                {response?.data.map((article) => (
                  <li key={article.id}>
                    <span className="status-badge">{articleStatuses[article.status]}</span>
                    <p className="article-classification">
                      {contentTypes[article.contentType] ?? 'Chưa phân loại'}
                    </p>
                    <h2>
                      <a href={article.originalUrl} target="_blank" rel="noopener noreferrer">
                        {article.title}
                      </a>
                    </h2>
                    {article.summary && <p className="pending-summary">{article.summary}</p>}
                    <p className="pending-meta">
                      {article.sourceName} · Kênh đầu tiên #{article.firstFeedId}
                      <br />
                      Xuất bản:{' '}
                      {article.publishedAt
                        ? new Date(article.publishedAt).toLocaleString('vi-VN')
                        : 'Chưa có ngày xuất bản'}
                      <br />
                      Thu thập: {new Date(article.collectedAt).toLocaleString('vi-VN')}
                    </p>
                    <Link className="secondary-button" to={`/admin/articles/${article.id}`}>
                      Xem chi tiết & xử lý
                    </Link>
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
