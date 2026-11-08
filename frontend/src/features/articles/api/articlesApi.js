import { authenticatedRequest } from '../../../api/httpClient'
const base = '/admin/articles'
export const articlesApi = {
  list: (status, page, signal) =>
    authenticatedRequest(`${base}?status=${status}&page=${page}&size=10`, { signal }),
  detail: (id, signal) => authenticatedRequest(`${base}/${id}`, { signal }),
  classify: (id, contentType, version) =>
    authenticatedRequest(`${base}/${id}/classification`, {
      method: 'PATCH',
      body: { contentType, version },
    }),
  publish: (id, version) =>
    authenticatedRequest(`${base}/${id}/publish`, { method: 'POST', body: { version } }),
  reject: (id, reason, version) =>
    authenticatedRequest(`${base}/${id}/reject`, { method: 'POST', body: { reason, version } }),
  hide: (id, reason, version) =>
    authenticatedRequest(`${base}/${id}/hide`, { method: 'POST', body: { reason, version } }),
}
