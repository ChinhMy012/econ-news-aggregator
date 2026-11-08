import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { articlesApi } from '../api/articlesApi'
import { articleStatuses, contentTypes, articleTime } from '../utils/articleLabels'
import './articles.css'
export function ArticleDetailPage() {
  const { id } = useParams()
  return <ArticleDetail key={id} id={id} />
}
function ArticleDetail({ id }) {
  const [article, setArticle] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [reload, setReload] = useState(0)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState('')
  const [type, setType] = useState('')
  const [action, setAction] = useState(null)
  const [reason, setReason] = useState('')
  useEffect(() => {
    const abort = new AbortController()
    articlesApi
      .detail(id, abort.signal)
      .then((response) => {
        if (abort.signal.aborted) return
        setArticle(response.data)
        setType(response.data.contentType ?? '')
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [id, reload])
  function refresh() {
    setLoading(true)
    setError(null)
    setNotice('')
    setAction(null)
    setReason('')
    setReload((n) => n + 1)
  }
  async function perform(kind) {
    setSaving(true)
    setError(null)
    setNotice('')
    try {
      const response =
        kind === 'classify'
          ? await articlesApi.classify(id, type, article.version)
          : kind === 'publish'
            ? await articlesApi.publish(id, article.version)
            : kind === 'reject'
              ? await articlesApi.reject(id, reason.trim(), article.version)
              : await articlesApi.hide(id, reason.trim(), article.version)
      setArticle(response.data)
      setType(response.data.contentType ?? '')
      setAction(null)
      setReason('')
      setNotice(response.message)
    } catch (failure) {
      setError(failure)
    } finally {
      setSaving(false)
    }
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <Link to="/admin/articles">Danh sách bài viết</Link>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">DUYỆT NỘI DUNG</p>
            <h1>Chi tiết bài viết</h1>
          </div>
          <button className="secondary-button" disabled={loading || saving} onClick={refresh}>
            Tải lại dữ liệu
          </button>
        </div>
        {notice && (
          <p role="status" className="success-message">
            {notice}
          </p>
        )}
        {error && (
          <div className="field-error" role="alert">
            <p>{error.message}</p>
            {error.errors?.map((item, index) => (
              <p key={`${item.field}-${index}`}>
                {item.field}: {item.message}
              </p>
            ))}
            {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
            {error.status === 409 && (
              <p>Giữ lại nội dung cần dùng, rồi bấm Tải lại dữ liệu trước khi thao tác tiếp.</p>
            )}
          </div>
        )}
        {loading ? (
          <p role="status">Đang tải bài viết…</p>
        ) : (
          article && (
            <div className="article-detail-grid">
              <article className="article-detail-card">
                <span className="status-badge">{articleStatuses[article.status]}</span>
                <h2>{article.title}</h2>
                <p>
                  <a href={article.originalUrl} target="_blank" rel="noopener noreferrer">
                    Đọc bài gốc ↗
                  </a>
                </p>
                {article.summary ? (
                  <p className="pending-summary">{article.summary}</p>
                ) : (
                  <p className="muted">RSS không cung cấp mô tả.</p>
                )}
                <dl className="article-facts">
                  <div>
                    <dt>Nguồn</dt>
                    <dd>
                      <a href={article.sourceWebsiteUrl} target="_blank" rel="noopener noreferrer">
                        {article.sourceName}
                      </a>
                    </dd>
                  </div>
                  <div>
                    <dt>Xuất bản</dt>
                    <dd>{articleTime(article.publishedAt)}</dd>
                  </div>
                  <div>
                    <dt>Thu thập</dt>
                    <dd>{articleTime(article.collectedAt)}</dd>
                  </div>
                  <div>
                    <dt>Phân loại đã lưu</dt>
                    <dd>{contentTypes[article.contentType] ?? 'Chưa phân loại'}</dd>
                  </div>
                </dl>
                {article.moderationReason && (
                  <section className="article-reason">
                    <h3>Lý do {article.status === 'HIDDEN' ? 'ẩn' : 'loại'}</h3>
                    <p>{article.moderationReason}</p>
                  </section>
                )}
              </article>
              <section className="article-detail-card">
                <h2>Xử lý bài viết</h2>
                {article.status === 'PENDING' && (
                  <>
                    <div className="article-filter">
                      <label htmlFor="article-content-type">Phân loại</label>
                      <select
                        id="article-content-type"
                        value={type}
                        disabled={saving || Boolean(action)}
                        onChange={(event) => setType(event.target.value)}
                      >
                        <option value="">Chọn phân loại</option>
                        {Object.entries(contentTypes).map(([value, label]) => (
                          <option key={value} value={value}>
                            {label}
                          </option>
                        ))}
                      </select>
                    </div>
                    <button
                      className="secondary-button"
                      disabled={saving || Boolean(action) || !type || type === article.contentType}
                      onClick={() => void perform('classify')}
                    >
                      Lưu phân loại
                    </button>
                    <p className="muted article-help">
                      Lưu phân loại trước khi duyệt. Phân loại không tự công khai bài viết.
                    </p>
                    <div className="catalog-actions">
                      <button
                        className="primary-button"
                        disabled={
                          saving ||
                          Boolean(action) ||
                          !article.contentType ||
                          type !== article.contentType
                        }
                        onClick={() => void perform('publish')}
                      >
                        Duyệt & công khai
                      </button>
                      <button
                        className="secondary-button"
                        disabled={saving || Boolean(action)}
                        onClick={() => {
                          setAction('reject')
                          setReason('')
                          setError(null)
                        }}
                      >
                        Loại bài
                      </button>
                    </div>
                  </>
                )}
                {article.status === 'PUBLISHED' && (
                  <>
                    <p>Bài đang hiển thị qua API công khai.</p>
                    <button
                      className="secondary-button"
                      disabled={saving || Boolean(action)}
                      onClick={() => {
                        setAction('hide')
                        setReason('')
                        setError(null)
                      }}
                    >
                      Ẩn bài
                    </button>
                  </>
                )}
                {['REJECTED', 'HIDDEN'].includes(article.status) && (
                  <p>Bài không hiển thị công khai. MVP chưa hỗ trợ mở lại bài đã loại hoặc ẩn.</p>
                )}
                {action && (
                  <form
                    className="article-reason-form"
                    onSubmit={(event) => {
                      event.preventDefault()
                      void perform(action)
                    }}
                  >
                    <fieldset disabled={saving}>
                      <label htmlFor="moderation-reason">
                        Lý do {action === 'reject' ? 'loại' : 'ẩn'} bài
                      </label>
                      <textarea
                        id="moderation-reason"
                        required
                        maxLength={1000}
                        rows={5}
                        value={reason}
                        onChange={(event) => setReason(event.target.value)}
                      />
                      <small>{reason.length}/1000 ký tự</small>
                      <div className="catalog-actions">
                        <button className="primary-button" disabled={!reason.trim()} type="submit">
                          Xác nhận {action === 'reject' ? 'loại' : 'ẩn'}
                        </button>
                        <button
                          className="secondary-button"
                          type="button"
                          onClick={() => {
                            setAction(null)
                            setReason('')
                          }}
                        >
                          Hủy
                        </button>
                      </div>
                    </fieldset>
                  </form>
                )}
                {saving && <p role="status">Đang lưu thay đổi…</p>}
              </section>
            </div>
          )
        )}
      </main>
    </div>
  )
}
