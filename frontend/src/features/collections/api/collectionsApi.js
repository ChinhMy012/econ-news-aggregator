import { authenticatedRequest } from '../../../api/httpClient'
const path = (sourceId, feedId) => `/admin/sources/${sourceId}/feeds/${feedId}/collections`
export const collectionsApi = {
  run: (sourceId, feedId) =>
    authenticatedRequest(path(sourceId, feedId), {
      method: 'POST',
      signal: AbortSignal.timeout(45000),
    }),
  history: (sourceId, feedId, page = 0) =>
    authenticatedRequest(`${path(sourceId, feedId)}?page=${page}&size=5`),
}
