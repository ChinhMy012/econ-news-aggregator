import { useState } from 'react'
import { collectionsApi } from '../api/collectionsApi'
import './collections.css'
const statuses = {
  RUNNING: 'Đang chạy',
  SUCCESS: 'Thành công',
  PARTIAL_SUCCESS: 'Thành công một phần',
  FAILED: 'Thất bại',
  INTERRUPTED: 'Bị gián đoạn',
}
const reasons = {
  RSS_TIMEOUT: 'Nguồn phản hồi quá chậm.',
  RSS_URL_BLOCKED: 'Đường dẫn không đáp ứng chính sách tải RSS.',
  RSS_FETCH_FAILED: 'Không kết nối được nguồn hoặc địa chỉ đích bị chặn.',
  RSS_REDIRECT_BLOCKED: 'Nguồn trả về chuyển hướng. Hãy kiểm tra lại URL kênh.',
  RSS_HTTP_ERROR: 'Nguồn trả về HTTP không thành công.',
  RSS_EMPTY_RESPONSE: 'Nguồn trả về nội dung rỗng.',
  RSS_CONTENT_TYPE_INVALID: 'Nội dung trả về không phải XML/RSS được hỗ trợ.',
  RSS_TOO_LARGE: 'RSS vượt giới hạn 2 MB.',
  RSS_XML_INVALID: 'XML không hợp lệ hoặc có cấu trúc bị chặn.',
  RSS_FORMAT_UNSUPPORTED: 'Chỉ hỗ trợ RSS 2.0 và Atom.',
  RSS_TOO_MANY_ENTRIES: 'RSS vượt giới hạn 500 mục.',
  INVALID_ENTRIES: 'Có mục thiếu tiêu đề hoặc URL hợp lệ.',
  FEED_CHANGED: 'Nguồn/kênh bị tắt hoặc URL thay đổi trong lúc tải. Không lưu bài.',
  STALE_RUN: 'Lần chạy cũ không hoàn tất.',
  RSS_DOWNLOADER_BUSY: 'Bộ tải đang bận.',
  RSS_INTERRUPTED: 'Tác vụ tải bị gián đoạn.',
  COLLECTION_STORAGE_ERROR: 'Không lưu được kết quả thu thập.',
}
function time(value) {
  return value ? new Date(value).toLocaleString('vi-VN') : '—'
}
export function CollectionWidget({ source, feed, editing }) {
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(false)
  const [history, setHistory] = useState(null)
  const [error, setError] = useState(null)
  const [result, setResult] = useState(null)
  const [open, setOpen] = useState(false)
  async function load(page = 0) {
    setLoading(true)
    setError(null)
    setOpen(true)
    try {
      setHistory(await collectionsApi.history(source.id, feed.id, page))
    } catch (failure) {
      setError(failure)
    } finally {
      setLoading(false)
    }
  }
  async function run() {
    setBusy(true)
    setError(null)
    setResult(null)
    setOpen(true)
    try {
      const response = await collectionsApi.run(source.id, feed.id)
      setResult(response.data)
      setHistory(await collectionsApi.history(source.id, feed.id))
    } catch (failure) {
      setError(failure)
    } finally {
      setBusy(false)
    }
  }
  return (
    <section className="collection-widget" aria-label={`Thu thập ${feed.name}`}>
      <div className="catalog-actions">
        <button
          className="primary-button"
          disabled={busy || loading || editing || !source.enabled || !feed.enabled}
          onClick={run}
        >
          {busy ? 'Đang thu thập…' : 'Thu thập ngay'}
        </button>
        <button className="secondary-button" disabled={busy || loading} onClick={() => void load()}>
          Lịch sử thu thập
        </button>
        {open && (
          <button
            className="catalog-reload"
            disabled={busy || loading}
            onClick={() => setOpen(false)}
          >
            Ẩn kết quả
          </button>
        )}
      </div>
      {busy && (
        <p role="status">Đang tải RSS và lưu bài mới vào hàng chờ duyệt. Không cần bấm lại.</p>
      )}
      {error && (
        <div role="alert" className="field-error">
          <p>{error.message}</p>
          {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
          <p>Nếu mất kết nối khi đang chạy, hãy xem lịch sử trước khi thu thập lại.</p>
        </div>
      )}
      {open && result && (
        <p
          role="status"
          className={
            ['SUCCESS', 'PARTIAL_SUCCESS'].includes(result.status)
              ? 'success-message'
              : 'field-error'
          }
        >
          Kết quả: {statuses[result.status]}. Bài mới: {result.insertedCount}; trùng:{' '}
          {result.duplicateCount}; lỗi mục tin: {result.invalidCount}.
        </p>
      )}
      {open && loading && <p role="status">Đang tải lịch sử…</p>}
      {open && history && (
        <div className="collection-history">
          {history.data.length === 0 && <p>Chưa có lần thu thập nào.</p>}
          {history.data.map((run) => (
            <article className="collection-run" key={run.id}>
              <h4>
                #{run.id} · {statuses[run.status]}
              </h4>
              <p>
                {time(run.startedAt)} → {time(run.finishedAt)}
              </p>
              <dl>
                <div>
                  <dt>Nhận</dt>
                  <dd>{run.receivedCount}</dd>
                </div>
                <div>
                  <dt>Mới</dt>
                  <dd>{run.insertedCount}</dd>
                </div>
                <div>
                  <dt>Trùng</dt>
                  <dd>{run.duplicateCount}</dd>
                </div>
                <div>
                  <dt>Lỗi mục</dt>
                  <dd>{run.invalidCount}</dd>
                </div>
              </dl>
              {run.errorCode && (
                <p className="field-error">
                  {reasons[run.errorCode] ?? 'Lần chạy chưa hoàn tất.'} ({run.errorCode})
                </p>
              )}
            </article>
          ))}
          {history.meta.totalPages > 1 && (
            <nav className="catalog-actions" aria-label="Phân trang lịch sử">
              <button
                className="secondary-button"
                disabled={busy || loading || history.meta.currentPage === 0}
                onClick={() => void load(history.meta.currentPage - 1)}
              >
                Trước
              </button>
              <span>
                {history.meta.currentPage + 1}/{history.meta.totalPages}
              </span>
              <button
                className="secondary-button"
                disabled={
                  busy || loading || history.meta.currentPage + 1 >= history.meta.totalPages
                }
                onClick={() => void load(history.meta.currentPage + 1)}
              >
                Sau
              </button>
            </nav>
          )}
        </div>
      )}
    </section>
  )
}
