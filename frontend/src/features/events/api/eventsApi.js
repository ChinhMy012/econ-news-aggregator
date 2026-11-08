import { authenticatedRequest } from '../../../api/httpClient'
import { requestJson } from '../../../api/requestJson'
const base = '/admin/events'
export const eventsApi = {
  list: (status, page, signal) =>
    authenticatedRequest(`${base}?status=${status}&page=${page}&size=10`, { signal }),
  detail: (id, signal) => authenticatedRequest(`${base}/${id}`, { signal }),
  create: (body) => authenticatedRequest(base, { method: 'POST', body }),
  update: (id, body) => authenticatedRequest(`${base}/${id}`, { method: 'PUT', body }),
  action: (id, action, body) =>
    authenticatedRequest(`${base}/${id}/${action}`, { method: 'POST', body }),
  milestone: (id, milestoneId, body) =>
    authenticatedRequest(`${base}/${id}/milestones${milestoneId ? `/${milestoneId}` : ''}`, {
      method: milestoneId ? 'PUT' : 'POST',
      body,
    }),
  milestoneAction: (id, milestoneId, action, body) =>
    authenticatedRequest(`${base}/${id}/milestones/${milestoneId}/${action}`, {
      method: 'POST',
      body,
    }),
  sources: (id, milestoneId, body) =>
    authenticatedRequest(
      milestoneId
        ? `${base}/${id}/milestones/${milestoneId}/sources`
        : `${base}/${id}/overview-sources`,
      { method: 'PUT', body },
    ),
  publicList: (page, signal) => requestJson(`/events?page=${page}&size=10`, { signal }),
  publicDetail: (id, signal) => requestJson(`/events/${id}`, { signal }),
}
