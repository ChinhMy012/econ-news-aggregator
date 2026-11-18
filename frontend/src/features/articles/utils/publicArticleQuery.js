export function readPublicArticleQuery(params) {
  const topic = params.get("contentType");
  const rawPage = params.get("page") ?? "";
  const pageNumber = /^[1-9][0-9]*$/.test(rawPage) ? Number(rawPage) : 1;

  return {
    contentType: topic === "POLICY" || topic === "BUSINESS" ? topic : "",
    page: pageNumber <= 2147483648 ? pageNumber - 1 : 0,
  };
}

export function publicArticleSearch(contentType, page) {
  const params = new URLSearchParams();
  if (contentType) params.set("contentType", contentType);
  if (page > 0) params.set("page", String(page + 1));
  return params.toString();
}
