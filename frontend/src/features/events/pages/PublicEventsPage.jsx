import { eventTypes, milestoneDate } from '../utils/eventLabels'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { eventsApi } from '../api/eventsApi'
import { CitationList, ContentError, ContentPager } from '../components/EventParts'
import './events.css'
function PublicHeader() {
  return (
    <header className="admin-header">
      <Link className="brand" to="/events">
        ECON<span>NEWS</span>
      </Link>
      <nav className="header-actions">
        <Link to="/events">Sự kiện kinh tế</Link>
        <Link to="/admin">Quản trị</Link>
      </nav>
    </header>
  )
}
export function PublicEventsPage() {
  const [page, setPage] = useState(0),
    [reload, setReload] = useState(0),
    [response, setResponse] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null)
  useEffect(() => {
    const abort = new AbortController()
    eventsApi
      .publicList(page, abort.signal)
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
  return (
    <div className="admin-shell">
      <PublicHeader />
      <main className="admin-main">
        <p className="eyebrow">HIỂU KINH TẾ QUA DIỄN BIẾN</p>
        <h1>Sự kiện kinh tế</h1>
        <p className="muted">
          Theo dõi câu chuyện chính sách và doanh nghiệp qua tổng quan, các mốc thời gian và nguồn
          tham chiếu.
        </p>
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
        <ContentError error={error} />
        {loading ? (
          <p role="status">Đang tải sự kiện…</p>
        ) : (
          !error && (
            <>
              <ul className="event-cards">
                {response?.data.map((event) => (
                  <li key={event.id}>
                    <p className="eyebrow">{eventTypes[event.contentType]}</p>
                    <h2>
                      <Link to={`/events/${event.id}`}>{event.title}</Link>
                    </h2>
                    <p className="event-preview">{event.overview}</p>
                    <Link to={`/events/${event.id}`}>Xem diễn biến →</Link>
                  </li>
                ))}
              </ul>
              {response?.data.length === 0 && (
                <section className="event-panel">
                  <h2>Chưa có sự kiện công khai</h2>
                  <p>Các nội dung đã được biên tập và có nguồn căn cứ sẽ xuất hiện tại đây.</p>
                </section>
              )}
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
export function PublicEventDetailPage() {
  const { id } = useParams()
  return <PublicEventDetail key={id} id={id} />
}
function PublicEventDetail({ id }) {
  const [data, setData] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null),
    [reload, setReload] = useState(0)
  useEffect(() => {
    const abort = new AbortController()
    eventsApi
      .publicDetail(id, abort.signal)
      .then((result) => {
        if (!abort.signal.aborted) setData(result.data)
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [id, reload])
  return (
    <div className="admin-shell">
      <PublicHeader />
      <main className="admin-main">
        <Link to="/events">← Danh sách sự kiện</Link>
        <ContentError error={error} />
        {loading ? (
          <p role="status">Đang tải diễn biến…</p>
        ) : (
          !error &&
          data && (
            <>
              <p className="eyebrow event-detail-type">{eventTypes[data.event.contentType]}</p>
              <h1>{data.event.title}</h1>
              <section className="event-panel">
                <h2>Tổng quan</h2>
                <p className="event-body">{data.event.overview}</p>
                <h3>Nguồn tổng quan</h3>
                <CitationList sources={data.overviewSources} />
              </section>
              <section className="event-panel">
                <h2>Dòng thời gian</h2>
                <ol className="event-timeline">
                  {data.milestones.map((m) => (
                    <li key={m.id}>
                      <p className="eyebrow">{milestoneDate(m)}</p>
                      <h3>{m.title}</h3>
                      <p className="event-body">{m.description}</p>
                      <CitationList sources={m.sources} />
                    </li>
                  ))}
                </ol>
              </section>
            </>
          )
        )}
        {error && (
          <button
            className="secondary-button"
            onClick={() => {
              setLoading(true)
              setError(null)
              setReload((n) => n + 1)
            }}
          >
            Thử lại
          </button>
        )}
      </main>
    </div>
  )
}
