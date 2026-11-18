import { useEffect, useState } from 'react'
import { authenticatedRequest } from '../../../api/httpClient'
import { documentsApi } from '../../documents/api/documentsApi'
import { ContentError, ContentPager } from './EventParts'
export function SourcePicker({ initial, busy, error, onSave, onCancel }) {
  const [kind, setKind] = useState('ARTICLE')
  const [page, setPage] = useState(0)
  const [response, setResponse] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [selected, setSelected] = useState(
    () => new Map(initial.map((s) => [`${s.kind}-${s.id}`, s])),
  )
  useEffect(() => {
    const abort = new AbortController()
    const call =
      kind === 'ARTICLE'
        ? authenticatedRequest(`/admin/articles?status=PUBLISHED&page=${page}&size=10`, {
            signal: abort.signal,
          })
        : documentsApi.list(page, abort.signal)
    call
      .then((result) => {
        if (!abort.signal.aborted) setResponse(result)
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setLoadError(failure)
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false)
      })
    return () => abort.abort()
  }, [kind, page])
  function changeKind(value) {
    setLoading(true)
    setLoadError(null)
    setPage(0)
    setKind(value)
  }
  function toggle(source) {
    setSelected((old) => {
      const next = new Map(old)
      const key = `${source.kind}-${source.id}`
      if (next.has(key)) next.delete(key)
      else next.set(key, source)
      return next
    })
  }
  return (
    <form
      className="content-form"
      onSubmit={(e) => {
        e.preventDefault()
        const values = [...selected.values()]
        void onSave({
          articleIds: values.filter((s) => s.kind === 'ARTICLE').map((s) => s.id),
          documentIds: values.filter((s) => s.kind === 'DOCUMENT').map((s) => s.id),
        })
      }}
    >
      <fieldset disabled={busy}>
        <legend>Chọn nguồn trích dẫn</legend>
        <p>{selected.size}/50 nguồn được chọn</p>
        <ul className="selected-sources">
          {[...selected].map(([key, source]) => (
            <li key={key}>
              <span>{source.title}</span>
              <button type="button" className="catalog-reload" onClick={() => toggle(source)}>
                Bỏ chọn {source.kind === 'ARTICLE' ? 'bài' : 'tài liệu'} #{source.id}
              </button>
            </li>
          ))}
        </ul>
        <label htmlFor="citation-kind">Tìm trong</label>
        <select
          id="citation-kind"
          disabled={loading}
          value={kind}
          onChange={(e) => changeKind(e.target.value)}
        >
          <option value="ARTICLE">Bài viết đã công khai</option>
          <option value="DOCUMENT">Tài liệu tham chiếu</option>
        </select>
        <ContentError error={loadError} />
        {loading ? (
          <p role="status">Đang tải nguồn…</p>
        ) : (
          !loadError && (
            <>
              <ul className="source-options">
                {response?.data.map((item) => {
                  const usable = kind === 'ARTICLE' || item.enabled
                  const source = { id: item.id, kind, title: item.title }
                  const checked = selected.has(`${kind}-${item.id}`)
                  return (
                    <li key={item.id}>
                      <label>
                        <input
                          type="checkbox"
                          checked={checked}
                          disabled={(!usable || selected.size >= 50) && !checked}
                          onChange={() => toggle(source)}
                        />
                        <span>
                          {item.title}
                          <small>
                            {kind === 'ARTICLE' ? item.sourceName : item.issuer}
                            {!usable ? ' · Đang tắt' : ''}
                          </small>
                        </span>
                      </label>
                    </li>
                  )
                })}
              </ul>
              {response?.data.length === 0 && (
                <p>Chưa có nguồn phù hợp. Hãy duyệt bài hoặc tạo tài liệu trước.</p>
              )}
              <ContentPager
                meta={response?.meta}
                busy={busy || loading}
                onPage={(next) => {
                  setLoading(true)
                  setLoadError(null)
                  setPage(next)
                }}
              />
            </>
          )
        )}
        <ContentError error={error} />
        <div className="catalog-actions">
          <button className="primary-button" disabled={loading} type="submit">
            {busy ? 'Đang lưu…' : 'Lưu nguồn'}
          </button>
          <button className="secondary-button" type="button" onClick={onCancel}>
            Hủy
          </button>
        </div>
      </fieldset>
    </form>
  )
}
