import { useState } from 'react'
import { eventTypes } from '../utils/eventLabels'
export function ContentError({ error }) {
  return (
    error && (
      <div className="field-error" role="alert">
        <p>{error.message}</p>
        {error.errors?.map((e, i) => (
          <p key={`${e.field}-${i}`}>
            {e.field}: {e.message}
          </p>
        ))}
        {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
        {error.code === 'VERSION_CONFLICT' && (
          <p>Giữ lại nội dung cần dùng rồi tải lại dữ liệu trước khi sửa tiếp.</p>
        )}
      </div>
    )
  )
}
export function ContentPager({ meta, busy, onPage }) {
  return (
    meta?.totalPages > 1 && (
      <nav className="catalog-actions" aria-label="Phân trang">
        <button
          className="secondary-button"
          disabled={busy || meta.currentPage === 0}
          onClick={() => onPage(meta.currentPage - 1)}
        >
          Trước
        </button>
        <span>
          {meta.currentPage + 1}/{meta.totalPages}
        </span>
        <button
          className="secondary-button"
          disabled={busy || meta.currentPage + 1 >= meta.totalPages}
          onClick={() => onPage(meta.currentPage + 1)}
        >
          Sau
        </button>
      </nav>
    )
  )
}
export function CitationList({ sources }) {
  return sources.length ? (
    <ul className="event-citations">
      {sources.map((s) => (
        <li key={`${s.kind}-${s.id}`}>
          <span>
            {s.kind === 'ARTICLE' ? 'Bài viết' : 'Tài liệu'} · {s.publisher}
          </span>
          <a href={s.url} target="_blank" rel="noopener noreferrer">
            {s.title} ↗
          </a>
          {s.usable === false && <strong className="field-error">Nguồn không còn khả dụng</strong>}
        </li>
      ))}
    </ul>
  ) : (
    <p className="muted">Chưa có nguồn tham chiếu.</p>
  )
}
export function EventForm({ initial, busy, error, onSave, onCancel }) {
  const [title, setTitle] = useState(initial?.title ?? '')
  const [type, setType] = useState(initial?.contentType ?? 'POLICY')
  const [overview, setOverview] = useState(initial?.overview ?? '')
  return (
    <form
      className="content-form"
      onSubmit={(e) => {
        e.preventDefault()
        void onSave({ title: title.trim(), contentType: type, overview: overview.trim() })
      }}
    >
      <fieldset disabled={busy}>
        <legend>{initial ? 'Sửa sự kiện' : 'Tạo sự kiện nháp'}</legend>
        <label>
          Tiêu đề sự kiện
          <input
            required
            maxLength={300}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </label>
        <label htmlFor="event-type">Loại nội dung</label>
        <select id="event-type" value={type} onChange={(e) => setType(e.target.value)}>
          {Object.entries(eventTypes).map(([v, label]) => (
            <option key={v} value={v}>
              {label}
            </option>
          ))}
        </select>
        <label htmlFor="event-overview">Tổng quan</label>
        <textarea
          id="event-overview"
          rows={6}
          maxLength={10000}
          value={overview}
          onChange={(e) => setOverview(e.target.value)}
        />
        <small>
          Bản nháp có thể chưa hoàn thiện tổng quan. Công khai cần có tổng quan, nguồn tổng quan và
          ít nhất một mốc công khai.
        </small>
        <ContentError error={error} />
        <div className="catalog-actions">
          <button className="primary-button" type="submit">
            {busy ? 'Đang lưu…' : 'Lưu sự kiện'}
          </button>
          <button className="secondary-button" type="button" onClick={onCancel}>
            Hủy
          </button>
        </div>
      </fieldset>
    </form>
  )
}
export function MilestoneForm({ initial, busy, error, onSave, onCancel }) {
  const [title, setTitle] = useState(initial?.title ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [precision, setPrecision] = useState(initial?.datePrecision ?? 'YEAR')
  const [year, setYear] = useState(initial?.dateYear ?? new Date().getFullYear())
  const [month, setMonth] = useState(initial?.dateMonth ?? 1)
  const [day, setDay] = useState(initial?.dateDay ?? 1)
  return (
    <form
      className="content-form"
      onSubmit={(e) => {
        e.preventDefault()
        void onSave({
          title: title.trim(),
          description: description.trim(),
          datePrecision: precision,
          dateYear: Number(year),
          dateMonth: precision === 'YEAR' ? null : Number(month),
          dateDay: precision === 'DAY' ? Number(day) : null,
        })
      }}
    >
      <fieldset disabled={busy}>
        <legend>{initial ? 'Sửa mốc diễn biến' : 'Thêm mốc nháp'}</legend>
        <label>
          Tiêu đề mốc
          <input
            required
            maxLength={300}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </label>
        <label htmlFor="milestone-description">Nội dung mốc</label>
        <textarea
          id="milestone-description"
          rows={5}
          maxLength={4000}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <label htmlFor="milestone-precision">Độ chính xác thời gian</label>
        <select
          id="milestone-precision"
          value={precision}
          onChange={(e) => setPrecision(e.target.value)}
        >
          <option value="YEAR">Chỉ biết năm</option>
          <option value="MONTH">Biết tháng và năm</option>
          <option value="DAY">Biết ngày cụ thể</option>
        </select>
        <div className="event-date-fields">
          <label>
            Năm
            <input
              required
              type="number"
              min={1}
              max={9999}
              value={year}
              onChange={(e) => setYear(e.target.value)}
            />
          </label>
          {precision !== 'YEAR' && (
            <label>
              Tháng
              <input
                required
                type="number"
                min={1}
                max={12}
                value={month}
                onChange={(e) => setMonth(e.target.value)}
              />
            </label>
          )}
          {precision === 'DAY' && (
            <label>
              Ngày
              <input
                required
                type="number"
                min={1}
                max={31}
                value={day}
                onChange={(e) => setDay(e.target.value)}
              />
            </label>
          )}
        </div>
        <ContentError error={error} />
        <div className="catalog-actions">
          <button className="primary-button" type="submit">
            {busy ? 'Đang lưu…' : 'Lưu mốc'}
          </button>
          <button className="secondary-button" type="button" onClick={onCancel}>
            Hủy
          </button>
        </div>
      </fieldset>
    </form>
  )
}
