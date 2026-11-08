import { authenticatedRequest } from '../../../api/httpClient'
export const documentsApi = {
  list: (page, signal) => authenticatedRequest(`/admin/documents?page=${page}&size=10`, { signal }),
  create: (body) => authenticatedRequest('/admin/documents', { method: 'POST', body }),
  update: (id, body) => authenticatedRequest(`/admin/documents/${id}`, { method: 'PUT', body }),
}
