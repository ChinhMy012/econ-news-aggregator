import { useState } from 'react'
export function CatalogForm({ kind, record, busy, error, onSave, onCancel }) {
  const isSource = kind === 'source'
  const [values, setValues] = useState(() => ({
    code: record?.code ?? '',
    name: record?.name ?? '',
    websiteUrl: record?.websiteUrl ?? '',
    url: record?.url ?? '',
    enabled: record?.enabled ?? true,
  }))
  const set = (key, value) => setValues((old) => ({ ...old, [key]: value }))
  const fieldErrors = error?.errors ?? []
  function submit(event) {
    event.preventDefault()
    const body = {
      name: values.name.trim(),
      enabled: values.enabled,
      ...(isSource ? { websiteUrl: values.websiteUrl.trim() } : { url: values.url.trim() }),
      ...(isSource && !record ? { code: values.code.trim() } : {}),
      ...(record ? { version: record.version } : {}),
    }
    void onSave(body)
  }
  return (
    <section className="catalog-form" aria-labelledby="catalog-form-title">
      <h2 id="catalog-form-title">
        {record ? 'Sửa' : 'Thêm'} {isSource ? 'nguồn tin' : 'kênh RSS'}
      </h2>
      <form onSubmit={submit}>
        <fieldset disabled={busy}>
          {isSource && (
            <label>
              Mã nguồn
              <input
                required
                maxLength={50}
                pattern="[a-z0-9]+(-[a-z0-9]+)*"
                value={values.code}
                disabled={Boolean(record)}
                onChange={(e) => set('code', e.target.value)}
                placeholder="vi-du-nguon"
              />
              <small>Mã duy nhất, chữ thường, số và dấu gạch ngang. Không đổi sau khi tạo.</small>
            </label>
          )}
          <label>
            Tên {isSource ? 'nguồn' : 'kênh'}
            <input
              required
              maxLength={200}
              value={values.name}
              onChange={(e) => set('name', e.target.value)}
            />
          </label>
          <label>
            {isSource ? 'Trang chủ' : 'Đường dẫn RSS'}
            <input
              type="url"
              required
              maxLength={2048}
              value={isSource ? values.websiteUrl : values.url}
              placeholder="https://..."
              onChange={(e) => set(isSource ? 'websiteUrl' : 'url', e.target.value)}
            />
            <small>Chỉ nhận HTTPS. Lưu đường dẫn chưa đồng nghĩa với RSS đã được kiểm tra.</small>
          </label>
          <label className="catalog-checkbox">
            <input
              type="checkbox"
              checked={values.enabled}
              onChange={(e) => set('enabled', e.target.checked)}
            />{' '}
            Bật {isSource ? 'nguồn' : 'kênh'}
          </label>
          {error && (
            <div role="alert" className="field-error">
              <p>{error.message}</p>
              {fieldErrors.map((item, index) => (
                <p key={`${item.field}-${index}`}>
                  {item.field}: {item.message}
                </p>
              ))}
              {error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}
              {error.status === 409 && (
                <p>Giữ lại nội dung cần sửa, bấm Hủy rồi tải lại danh sách để lấy phiên bản mới.</p>
              )}
            </div>
          )}
          <div className="catalog-actions">
            <button className="primary-button" type="submit">
              {busy ? 'Đang lưu…' : 'Lưu'}
            </button>
            <button className="secondary-button" type="button" onClick={onCancel}>
              Hủy
            </button>
          </div>
        </fieldset>
      </form>
    </section>
  )
}
