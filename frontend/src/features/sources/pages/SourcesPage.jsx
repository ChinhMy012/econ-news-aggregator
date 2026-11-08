import { CollectionWidget } from '../../collections/components/CollectionWidget'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { sourcesApi } from '../api/sourcesApi'
import { CatalogForm } from '../components/CatalogForm'
import './sources.css'
function Pager({ meta, busy, change }) {
  if (!meta || meta.totalPages < 2) return null
  return (
    <nav className="catalog-actions" aria-label="Phân trang">
      <button
        className="secondary-button"
        disabled={busy || meta.currentPage === 0}
        onClick={() => change(meta.currentPage - 1)}
      >
        Trước
      </button>
      <span>
        Trang {meta.currentPage + 1}/{meta.totalPages}
      </span>
      <button
        className="secondary-button"
        disabled={busy || meta.currentPage + 1 >= meta.totalPages}
        onClick={() => change(meta.currentPage + 1)}
      >
        Sau
      </button>
    </nav>
  )
}
function ErrorNotice({ error }) {
  return (
    error && (
      <div className="field-error" role="alert">
        <p>{error.message}</p>
        {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
      </div>
    )
  )
}
function FeedPanel({ source }) {
  const [page, setPage] = useState(0)
  const [reload, setReload] = useState(0)
  const [list, setList] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [editor, setEditor] = useState(null)
  const [saveError, setSaveError] = useState(null)
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    const abort = new AbortController()
    sourcesApi
      .feeds(source.id, page, abort.signal)
      .then((response) => {
        if (!abort.signal.aborted) setList(response)
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [source.id, page, reload])
  async function save(body) {
    setBusy(true)
    setSaveError(null)
    try {
      if (editor.record) await sourcesApi.updateFeed(source.id, editor.record.id, body)
      else await sourcesApi.createFeed(source.id, body)
      setEditor(null)
      setLoading(true)
      setError(null)
      setPage(0)
      setReload((n) => n + 1)
    } catch (failure) {
      setSaveError(failure)
    } finally {
      setBusy(false)
    }
  }
  function edit(record) {
    setEditor({ record })
    setSaveError(null)
  }
  return (
    <section className="catalog-panel">
      <div className="catalog-heading">
        <div>
          <p className="eyebrow">KÊNH RSS</p>
          <h2>{source.name}</h2>
        </div>
        <button
          className="primary-button"
          disabled={busy || Boolean(editor)}
          onClick={() => edit(null)}
        >
          Thêm kênh
        </button>
      </div>
      {!source.enabled && (
        <p className="catalog-warning">
          Nguồn đang tắt. Các kênh bên dưới sẽ không được thu thập, dù đang bật riêng.
        </p>
      )}
      {editor && (
        <CatalogForm
          key={editor.record?.id ?? 'new'}
          kind="feed"
          record={editor.record}
          busy={busy}
          error={saveError}
          onSave={save}
          onCancel={() => setEditor(null)}
        />
      )}
      <ErrorNotice error={error} />
      <button
        className="catalog-reload"
        disabled={loading || busy || Boolean(editor)}
        onClick={() => {
          setLoading(true)
          setError(null)
          setReload((n) => n + 1)
        }}
      >
        Tải lại kênh
      </button>
      {loading ? (
        <p role="status">Đang tải kênh…</p>
      ) : (
        !error && (
          <>
            {list?.data.length === 0 && (
              <p className="muted">Chưa có kênh RSS. Thêm đường dẫn đầu tiên cho nguồn này.</p>
            )}
            <ul className="catalog-list">
              {list?.data.map((feed) => (
                <li key={feed.id} className="catalog-feed-row">
                  <div>
                    <h3>{feed.name}</h3>
                    <p className="catalog-url">{feed.url}</p>
                    <span
                      className={
                        source.enabled && feed.enabled ? 'status-badge' : 'catalog-disabled'
                      }
                    >
                      {!feed.enabled
                        ? 'Kênh đang tắt'
                        : !source.enabled
                          ? 'Tạm dừng theo nguồn'
                          : 'Kênh đang bật'}
                    </span>
                  </div>
                  <button
                    className="secondary-button"
                    disabled={busy || Boolean(editor)}
                    onClick={() => edit(feed)}
                  >
                    Sửa kênh
                  </button>
                  <CollectionWidget source={source} feed={feed} editing={busy || Boolean(editor)} />
                </li>
              ))}
            </ul>
            <Pager
              meta={list?.meta}
              busy={busy || Boolean(editor)}
              change={(next) => {
                setLoading(true)
                setError(null)
                setPage(next)
              }}
            />
          </>
        )
      )}
    </section>
  )
}
export function SourcesPage() {
  const [page, setPage] = useState(0)
  const [reload, setReload] = useState(0)
  const [list, setList] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selected, setSelected] = useState(null)
  const [editor, setEditor] = useState(null)
  const [saveError, setSaveError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState('')
  useEffect(() => {
    const abort = new AbortController()
    sourcesApi
      .list(page, abort.signal)
      .then((response) => {
        if (abort.signal.aborted) return
        setList(response)
        setSelected((old) =>
          old ? (response.data.find((item) => item.id === old.id) ?? null) : null,
        )
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [page, reload])
  async function save(body) {
    setBusy(true)
    setSaveError(null)
    setNotice('')
    try {
      if (editor.record) await sourcesApi.update(editor.record.id, body)
      else await sourcesApi.create(body)
      setEditor(null)
      setLoading(true)
      setError(null)
      setPage(0)
      setReload((n) => n + 1)
      setNotice('Đã lưu nguồn tin.')
    } catch (failure) {
      setSaveError(failure)
    } finally {
      setBusy(false)
    }
  }
  function edit(record) {
    setEditor({ record })
    setSaveError(null)
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <div className="header-actions">
          <Link to="/admin/articles">Bài chờ duyệt</Link>
          <Link to="/admin">Về quản trị</Link>
        </div>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">CẤU HÌNH ĐẦU VÀO</p>
            <h1>Nguồn tin & kênh RSS</h1>
          </div>
          <button
            className="primary-button"
            disabled={busy || Boolean(editor)}
            onClick={() => edit(null)}
          >
            Thêm nguồn
          </button>
        </div>
        <p className="muted">
          Quản lý nơi lấy tin và các đường dẫn RSS. Chức năng thu thập tự động sẽ được triển khai ở
          bước tiếp theo.
        </p>
        {notice && (
          <p role="status" className="success-message">
            {notice}
          </p>
        )}
        {editor && (
          <CatalogForm
            key={editor.record?.id ?? 'new'}
            kind="source"
            record={editor.record}
            busy={busy}
            error={saveError}
            onSave={save}
            onCancel={() => setEditor(null)}
          />
        )}
        <div className="catalog-grid">
          <section className="catalog-panel">
            <div className="catalog-heading">
              <h2>Danh sách nguồn</h2>
              <button
                className="catalog-reload"
                disabled={loading || busy || Boolean(editor)}
                onClick={() => {
                  setLoading(true)
                  setError(null)
                  setReload((n) => n + 1)
                }}
              >
                Tải lại
              </button>
            </div>
            <ErrorNotice error={error} />
            {loading ? (
              <p role="status">Đang tải nguồn tin…</p>
            ) : (
              !error && (
                <>
                  <p className="muted">{list?.meta.totalItems ?? 0} nguồn tin</p>
                  {list?.data.length === 0 && (
                    <p>Chưa có nguồn tin. Bấm “Thêm nguồn” để bắt đầu.</p>
                  )}
                  <ul className="catalog-list">
                    {list?.data.map((source) => (
                      <li
                        key={source.id}
                        className={selected?.id === source.id ? 'catalog-selected' : ''}
                      >
                        <div>
                          <h3>{source.name}</h3>
                          <small>{source.code}</small>
                          <p className="catalog-url">{source.websiteUrl}</p>
                          <span className={source.enabled ? 'status-badge' : 'catalog-disabled'}>
                            {source.enabled ? 'Đang bật' : 'Đang tắt'}
                          </span>
                          <div className="catalog-actions">
                            <button
                              className="secondary-button"
                              disabled={busy || Boolean(editor)}
                              onClick={() => setSelected(source)}
                            >
                              Xem kênh
                            </button>
                            <button
                              className="secondary-button"
                              disabled={busy || Boolean(editor)}
                              onClick={() => edit(source)}
                            >
                              Sửa nguồn
                            </button>
                          </div>
                        </div>
                      </li>
                    ))}
                  </ul>
                  <Pager
                    meta={list?.meta}
                    busy={busy || Boolean(editor)}
                    change={(next) => {
                      setLoading(true)
                      setError(null)
                      setPage(next)
                    }}
                  />
                </>
              )
            )}
          </section>
          {selected && !loading && !error ? (
            <FeedPanel key={`${selected.id}-${selected.version}`} source={selected} />
          ) : (
            <section className="catalog-panel catalog-empty">
              <h2>Kênh RSS của nguồn</h2>
              <p className="muted">Chọn “Xem kênh” tại một nguồn để quản lý các đường dẫn RSS.</p>
            </section>
          )}
        </div>
      </main>
    </div>
  )
}
