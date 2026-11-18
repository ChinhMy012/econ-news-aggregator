export const articleStatuses = {
  PENDING: 'Chờ duyệt',
  PUBLISHED: 'Đã công khai',
  REJECTED: 'Đã loại',
  HIDDEN: 'Đã ẩn',
}
export const contentTypes = { POLICY: 'Chính sách', BUSINESS: 'Doanh nghiệp / kinh doanh' }
export const articleTime = (value) =>
  value ? new Date(value).toLocaleString('vi-VN') : 'Chưa có ngày xuất bản'
