import { authenticatedRequest } from '../../../api/httpClient'
const base = '/admin/sources'
export const sourcesApi = {
  list: (page = 0, signal) => authenticatedRequest(`${base}?page=${page}&size=10`, { signal }),
  create: (body) => authenticatedRequest(base, { method: 'POST', body }),
  update: (id, body) => authenticatedRequest(`${base}/${id}`, { method: 'PUT', body }),
  feeds: (id, page = 0, signal) =>
    authenticatedRequest(`${base}/${id}/feeds?page=${page}&size=10`, { signal }),
  createFeed: (id, body) => authenticatedRequest(`${base}/${id}/feeds`, { method: 'POST', body }),
  updateFeed: (sourceId, id, body) =>
    authenticatedRequest(`${base}/${sourceId}/feeds/${id}`, { method: 'PUT', body }),
}
