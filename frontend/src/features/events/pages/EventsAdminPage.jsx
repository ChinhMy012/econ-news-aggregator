import { eventStatuses, eventTypes } from '../utils/eventLabels'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { eventsApi } from '../api/eventsApi'
import { ContentError, ContentPager, EventForm } from '../components/EventParts'
import './events.css'
export function EventsAdminPage() {
  const navigate = useNavigate()
  const [status, setStatus] = useState('ALL'),
    [page, setPage] = useState(0),
    [reload, setReload] = useState(0)
  const [response, setResponse] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null)
  const [creating, setCreating] = useState(false),
    [busy, setBusy] = useState(false),
    [saveError, setSaveError] = useState(null)
  useEffect(() => {
    const abort = new AbortController()
    eventsApi
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
  async function save(body) {
    setBusy(true)
    setSaveError(null)
    try {
      const result = await eventsApi.create(body)
      navigate(`/admin/events/${result.data.event.id}`)
    } catch (failure) {
      setSaveError(failure)
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <nav className="header-actions">
          <Link to="/admin/documents">Tài liệu</Link>
          <Link to="/events">Trang công khai</Link>
          <Link to="/admin">Quản trị</Link>
        </nav>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">TỔ CHỨC CÂU CHUYỆN KINH TẾ</p>
            <h1>Quản lý sự kiện</h1>
          </div>
          <button
            className="primary-button"
            disabled={creating}
            onClick={() => {
              setCreating(true)
              setSaveError(null)
            }}
          >
            Tạo sự kiện
          </button>
        </div>
        {creating && (
          <EventForm
            busy={busy}
            error={saveError}
            onSave={save}
            onCancel={() => setCreating(false)}
          />
        )}
        <div className="catalog-actions">
          <label htmlFor="event-status-filter">Trạng thái</label>
          <select
            id="event-status-filter"
            disabled={loading || busy}
            value={status}
            onChange={(e) => {
              setLoading(true)
              setError(null)
              setPage(0)
              setStatus(e.target.value)
            }}
          >
            <option value="ALL">Tất cả</option>
            {Object.entries(eventStatuses).map(([v, label]) => (
              <option key={v} value={v}>
                {label}
              </option>
            ))}
          </select>
          <button
            className="secondary-button"
            disabled={loading || busy}
            onClick={() => {
              setLoading(true)
              setError(null)
              setReload((n) => n + 1)
            }}
          >
            Tải lại
          </button>
        </div>
        <ContentError error={error} />
        {loading ? (
          <p role="status">Đang tải sự kiện…</p>
        ) : (
          !error && (
            <>
              <p>{response?.meta.totalItems ?? 0} sự kiện</p>
              <ul className="event-cards">
                {response?.data.map((event) => (
                  <li key={event.id}>
                    <span className="status-badge">{eventStatuses[event.status]}</span>
                    <p className="eyebrow">{eventTypes[event.contentType]}</p>
                    <h2>
                      <Link to={`/admin/events/${event.id}`}>{event.title}</Link>
                    </h2>
                    <p className="event-preview">{event.overview || 'Chưa có tổng quan.'}</p>
                  </li>
                ))}
              </ul>
              {response?.data.length === 0 && <p>Chưa có sự kiện trong trạng thái đang chọn.</p>}
              <ContentPager
                meta={response?.meta}
                busy={loading}
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
