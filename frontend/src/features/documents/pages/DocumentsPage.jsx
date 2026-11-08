import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { documentsApi } from '../api/documentsApi'
import { ContentError, ContentPager } from '../../events/components/EventParts'
import '../../events/pages/events.css'
function DocumentForm({ initial, busy, error, onSave, onCancel }) {
  const [title, setTitle] = useState(initial?.title ?? ''),
    [issuer, setIssuer] = useState(initial?.issuer ?? '')
  const [url, setUrl] = useState(initial?.url ?? ''),
    [issuedOn, setIssuedOn] = useState(initial?.issuedOn ?? '')
  const [enabled, setEnabled] = useState(initial?.enabled ?? true)
  return (
    <form
      className="content-form"
      onSubmit={(e) => {
        e.preventDefault()
        void onSave({
          title: title.trim(),
          issuer: issuer.trim(),
          issuedOn: issuedOn || null,
          enabled,
          ...(initial ? { version: initial.version } : { url: url.trim() }),
        })
      }}
    >
      <fieldset disabled={busy}>
        <legend>{initial ? 'Sửa tài liệu' : 'Thêm tài liệu tham chiếu'}</legend>
        <label>
          Tên tài liệu
          <input
            required
            maxLength={300}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </label>
        <label>
          Cơ quan ban hành
          <input
            required
            maxLength={200}
            value={issuer}
            onChange={(e) => setIssuer(e.target.value)}
          />
        </label>
        <label>
          URL tài liệu
          <input
            type="url"
            required
            maxLength={2048}
            disabled={Boolean(initial)}
            value={url}
            placeholder="https://..."
            onChange={(e) => setUrl(e.target.value)}
          />
        </label>
        <small>
          Chỉ lưu liên kết HTTPS, không tải file. URL giữ nguyên sau khi tạo để bảo toàn nguồn trích
          dẫn.
        </small>
        <label>
          Ngày ban hành (nếu biết)
          <input type="date" value={issuedOn} onChange={(e) => setIssuedOn(e.target.value)} />
        </label>
        <label className="content-checkbox">
          <input type="checkbox" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />{' '}
          Bật tài liệu để sử dụng làm nguồn
        </label>
        <ContentError error={error} />
        <div className="catalog-actions">
          <button className="primary-button" type="submit">
            {busy ? 'Đang lưu…' : 'Lưu tài liệu'}
          </button>
          <button className="secondary-button" type="button" onClick={onCancel}>
            Hủy
          </button>
        </div>
      </fieldset>
    </form>
  )
}
export function DocumentsPage() {
  const [page, setPage] = useState(0),
    [reload, setReload] = useState(0),
    [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(true),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(null),
    [saveError, setSaveError] = useState(null)
  const [editor, setEditor] = useState(null),
    [notice, setNotice] = useState('')
  useEffect(() => {
    const abort = new AbortController()
    documentsApi
      .list(page, abort.signal)
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
  async function save(body) {
    setBusy(true)
    setSaveError(null)
    try {
      const result = editor.record
        ? await documentsApi.update(editor.record.id, body)
        : await documentsApi.create(body)
      setNotice(result.message)
      setEditor(null)
      setError(null)
      setLoading(true)
      setPage(0)
      setReload((n) => n + 1)
    } catch (failure) {
      setSaveError(failure)
    } finally {
      setBusy(false)
    }
  }
  function open(record) {
    setEditor({ record })
    setSaveError(null)
    setNotice('')
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <nav className="header-actions">
          <Link to="/admin/events">Sự kiện</Link>
          <Link to="/admin">Quản trị</Link>
        </nav>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">NGUỒN CĂN CỨ</p>
            <h1>Tài liệu tham chiếu</h1>
          </div>
          <button className="primary-button" disabled={Boolean(editor)} onClick={() => open(null)}>
            Thêm tài liệu
          </button>
        </div>
        <p className="muted">
          Lưu văn bản, báo cáo hoặc thông cáo để trích dẫn trong tổng quan và mốc diễn biến.
        </p>
        {notice && (
          <p role="status" className="success-message">
            {notice}
          </p>
        )}
        {editor && (
          <DocumentForm
            key={editor.record?.id ?? 'new'}
            initial={editor.record}
            busy={busy}
            error={saveError}
            onSave={save}
            onCancel={() => setEditor(null)}
          />
        )}
        <button
          className="secondary-button"
          disabled={busy || loading || Boolean(editor)}
          onClick={() => {
            setLoading(true)
            setError(null)
            setReload((n) => n + 1)
          }}
        >
          Tải lại tài liệu
        </button>
        <ContentError error={error} />
        {loading ? (
          <p role="status">Đang tải tài liệu…</p>
        ) : (
          !error && (
            <>
              <ul className="event-cards">
                {response?.data.map((doc) => (
                  <li key={doc.id}>
                    <span className="status-badge">{doc.enabled ? 'Đang bật' : 'Đang tắt'}</span>
                    <h2>
                      <a href={doc.url} target="_blank" rel="noopener noreferrer">
                        {doc.title} ↗
                      </a>
                    </h2>
                    <p>{doc.issuer}</p>
                    <p>Ban hành: {doc.issuedOn ?? 'Chưa có ngày'}</p>
                    <button
                      className="secondary-button"
                      disabled={busy || Boolean(editor)}
                      onClick={() => open(doc)}
                    >
                      Sửa tài liệu
                    </button>
                  </li>
                ))}
              </ul>
              {response?.data.length === 0 && <p>Chưa có tài liệu tham chiếu.</p>}
              <ContentPager
                meta={response?.meta}
                busy={busy || Boolean(editor)}
                onPage={(next) => {
                  setLoading(true)
                  setError(null)
                  setPage(next)
                }}
              />
            </>
          )
        )}
      </main>
    </div>
  )
}
