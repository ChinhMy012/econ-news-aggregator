import { requestJson } from "../../../api/requestJson";

export function listPublicArticles(page, contentType, signal) {
  const query = new URLSearchParams({ page, size: 10 });
  if (contentType) query.set("contentType", contentType);
  return requestJson(`/articles?${query}`, { signal });
}

export function getPublicArticle(id, signal) {
  return requestJson(`/articles/${encodeURIComponent(id)}`, { signal });
}
