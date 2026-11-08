import { eventStatuses, eventTypes, milestoneDate } from '../utils/eventLabels'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { eventsApi } from '../api/eventsApi'
import { CitationList, ContentError, EventForm, MilestoneForm } from '../components/EventParts'
import { SourcePicker } from '../components/SourcePicker'
import './events.css'
export function EventAdminDetailPage() {
  const { id } = useParams()
  return <EventEditor key={id} id={id} />
}
function EventEditor({ id }) {
  const [data, setData] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null),
    [reload, setReload] = useState(0)
  const [editor, setEditor] = useState(null),
    [busy, setBusy] = useState(false),
    [notice, setNotice] = useState(''),
    [reason, setReason] = useState('')
  useEffect(() => {
    const abort = new AbortController()
    eventsApi
      .detail(id, abort.signal)
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
  function open(kind, milestone = null) {
    setEditor({ kind, milestone })
    setError(null)
    setNotice('')
    setReason('')
  }
  async function run(call) {
    setBusy(true)
    setError(null)
    setNotice('')
    try {
      const response = await call()
      setData(response.data)
      setNotice(response.message)
      setEditor(null)
    } catch (failure) {
      setError(failure)
    } finally {
      setBusy(false)
    }
  }
  const locked = busy || Boolean(editor)
  const version = data?.event.version
  function save(body) {
    if (editor.kind === 'event') return run(() => eventsApi.update(id, { ...body, version }))
    if (editor.kind === 'milestone')
      return run(() => eventsApi.milestone(id, editor.milestone?.id, { ...body, version }))
    return run(() => eventsApi.sources(id, editor.milestone?.id, { ...body, version }))
  }
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <Link className="brand" to="/admin">
          ECON<span>NEWS</span>
        </Link>
        <nav className="header-actions">
          <Link to="/admin/events">Sự kiện</Link>
          <Link to="/admin/documents">Tài liệu</Link>
        </nav>
      </header>
      <main className="admin-main">
        <div className="catalog-heading">
          <div>
            <p className="eyebrow">BIÊN TẬP SỰ KIỆN</p>
            <h1>{data?.event.title ?? 'Chi tiết sự kiện'}</h1>
          </div>
          <button
            className="secondary-button"
            disabled={busy || loading}
            onClick={() => {
              setLoading(true)
              setEditor(null)
              setError(null)
              setNotice('')
              setReload((n) => n + 1)
            }}
          >
            Tải lại dữ liệu
          </button>
        </div>
        {notice && (
          <p role="status" className="success-message">
            {notice}
          </p>
        )}
        {!editor && <ContentError error={error} />}
        {loading ? (
          <p role="status">Đang tải sự kiện…</p>
        ) : (
          data && (
            <>
              <section className="event-panel">
                <div className="catalog-heading">
                  <div>
                    <span className="status-badge">{eventStatuses[data.event.status]}</span>
                    <p>{eventTypes[data.event.contentType]}</p>
                  </div>
                  <div className="catalog-actions">
                    <button
                      className="secondary-button"
                      disabled={locked}
                      onClick={() => open('event')}
                    >
                      Sửa tổng quan
                    </button>
                    {data.event.status !== 'PUBLISHED' ? (
                      <button
                        className="primary-button"
                        disabled={locked}
                        onClick={() => void run(() => eventsApi.action(id, 'publish', { version }))}
                      >
                        Công khai sự kiện
                      </button>
                    ) : (
                      <>
                        <Link className="secondary-button" to={`/events/${id}`}>
                          Xem trang công khai
                        </Link>
                        <button
                          className="secondary-button"
                          disabled={locked}
                          onClick={() => open('hide')}
                        >
                          Ẩn sự kiện
                        </button>
                      </>
                    )}
                  </div>
                </div>
                <p className="event-body">{data.event.overview || 'Bản nháp chưa có tổng quan.'}</p>
                {data.event.hiddenReason && (
                  <p className="event-warning">Lý do ẩn: {data.event.hiddenReason}</p>
                )}
                <h2>Nguồn tổng quan</h2>
                <CitationList sources={data.overviewSources} />
                <button
                  className="secondary-button"
                  disabled={locked}
                  onClick={() => open('overviewSources')}
                >
                  Chọn nguồn tổng quan
                </button>
              </section>
              {editor && (
                <section className="event-panel event-editor-panel">
                  {editor.kind === 'event' && (
                    <EventForm
                      initial={data.event}
                      busy={busy}
                      error={error}
                      onSave={save}
                      onCancel={() => setEditor(null)}
                    />
                  )}
                  {editor.kind === 'milestone' && (
                    <MilestoneForm
                      initial={editor.milestone}
                      busy={busy}
                      error={error}
                      onSave={save}
                      onCancel={() => setEditor(null)}
                    />
                  )}
                  {['overviewSources', 'milestoneSources'].includes(editor.kind) && (
                    <SourcePicker
                      initial={editor.milestone?.sources ?? data.overviewSources}
                      busy={busy}
                      error={error}
                      onSave={save}
                      onCancel={() => setEditor(null)}
                    />
                  )}
                  {editor.kind === 'hide' && (
                    <form
                      className="content-form"
                      onSubmit={(e) => {
                        e.preventDefault()
                        void run(() =>
                          eventsApi.action(id, 'hide', { reason: reason.trim(), version }),
                        )
                      }}
                    >
                      <fieldset disabled={busy}>
                        <legend>Ẩn sự kiện</legend>
                        <label htmlFor="event-hide-reason">Lý do ẩn</label>
                        <textarea
                          id="event-hide-reason"
                          required
                          maxLength={1000}
                          value={reason}
                          onChange={(e) => setReason(e.target.value)}
                        />
                        <ContentError error={error} />
                        <div className="catalog-actions">
                          <button
                            className="primary-button"
                            disabled={!reason.trim()}
                            type="submit"
                          >
                            Xác nhận ẩn sự kiện
                          </button>
                          <button
                            className="secondary-button"
                            type="button"
                            onClick={() => setEditor(null)}
                          >
                            Hủy
                          </button>
                        </div>
                      </fieldset>
                    </form>
                  )}
                </section>
              )}
              <section className="event-panel">
                <div className="catalog-heading">
                  <h2>Mốc diễn biến</h2>
                  <button
                    className="primary-button"
                    disabled={locked}
                    onClick={() => open('milestone')}
                  >
                    Thêm mốc
                  </button>
                </div>
                {data.milestones.length === 0 && (
                  <p>Chưa có mốc. Thêm mốc, viết nội dung và gắn nguồn trước khi công khai.</p>
                )}
                <ol className="event-timeline">
                  {data.milestones.map((m) => (
                    <li key={m.id}>
                      <p className="eyebrow">
                        {milestoneDate(m)} · {m.status === 'PUBLISHED' ? 'Đã công khai' : 'Nháp'}
                      </p>
                      <h3>{m.title}</h3>
                      <p className="event-body">{m.description || 'Chưa có nội dung.'}</p>
                      <CitationList sources={m.sources} />
                      <div className="catalog-actions">
                        <button
                          className="secondary-button"
                          disabled={locked}
                          onClick={() => open('milestone', m)}
                        >
                          Sửa mốc
                        </button>
                        <button
                          className="secondary-button"
                          disabled={locked}
                          onClick={() => open('milestoneSources', m)}
                        >
                          Chọn nguồn mốc
                        </button>
                        <button
                          className="secondary-button"
                          disabled={locked}
                          onClick={() =>
                            void run(() =>
                              eventsApi.milestoneAction(
                                id,
                                m.id,
                                m.status === 'PUBLISHED' ? 'unpublish' : 'publish',
                                { version },
                              ),
                            )
                          }
                        >
                          {m.status === 'PUBLISHED' ? 'Chuyển mốc về nháp' : 'Công khai mốc'}
                        </button>
                      </div>
                    </li>
                  ))}
                </ol>
              </section>
            </>
          )
        )}
      </main>
    </div>
  )
}
