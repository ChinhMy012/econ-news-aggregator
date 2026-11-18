export const eventStatuses = { DRAFT: 'Bản nháp', PUBLISHED: 'Đã công khai', HIDDEN: 'Đã ẩn' }
export const eventTypes = { POLICY: 'Chính sách', BUSINESS: 'Doanh nghiệp / kinh doanh' }
export function milestoneDate(m) {
  if (m.datePrecision === 'YEAR') return `${m.dateYear}`
  const month = String(m.dateMonth).padStart(2, '0')
  if (m.datePrecision === 'MONTH') return `${month}/${m.dateYear}`
  return `${String(m.dateDay).padStart(2, '0')}/${month}/${m.dateYear}`
}
