import { Link, useSearchParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { PublicHeader } from "../../../components/layout/PublicHeader";
import { ContentError, ContentPager } from "../../events/components/EventParts";
import { listPublicArticles } from "../api/publicArticlesApi";
import { articleTime, contentTypes } from "../utils/articleLabels";
import {
  publicArticleSearch,
  readPublicArticleQuery,
} from "../utils/publicArticleQuery";

export function PublicArticlesPage() {
  const [params, setParams] = useSearchParams();
  const { contentType, page } = readPublicArticleQuery(params);
  const search = publicArticleSearch(contentType, page);

  return (
    <div className="admin-shell">
      <PublicHeader />
      <main className="admin-main">
        <p className="eyebrow">TIN KINH TẾ ĐÃ BIÊN TẬP</p>
        <h1>Bài viết kinh tế</h1>
        <p className="muted">Đọc tóm tắt và mở bài gốc từ nguồn tin.</p>

        <div className="catalog-actions">
          <label htmlFor="article-topic">Chủ đề</label>
          <select
            id="article-topic"
            value={contentType}
            onChange={(event) =>
              setParams(publicArticleSearch(event.target.value, 0))
            }
          >
            <option value="">Tất cả chủ đề</option>
            {Object.entries(contentTypes).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <ArticleResults
          key={search}
          search={search}
          contentType={contentType}
          page={page}
          onPage={(next) => setParams(publicArticleSearch(contentType, next))}
        />
      </main>
    </div>
  );
}

function ArticleResults({ search, contentType, page, onPage }) {
  const [reload, setReload] = useState(0);
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const abort = new AbortController();
    listPublicArticles(page, contentType, abort.signal)
      .then((data) => {
        if (!abort.signal.aborted) setResponse(data);
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure);
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false);
      });
    return () => abort.abort();
  }, [page, contentType, reload]);

  function startLoading() {
    setLoading(true);
    setError(null);
  }

  function retry() {
    startLoading();
    setReload((value) => value + 1);
  }

  return (
    <>
      <button className="secondary-button" disabled={loading} onClick={retry}>
        {error ? "Thử lại" : "Tải lại"}
      </button>

      {loading && <p role="status">Đang tải bài viết…</p>}
      <ContentError error={error} />

      {!loading && !error && response && (
        <>
          {response.data.length === 0 ? (
            <div>
              <p>
                {page > 0
                  ? "Trang này không còn bài viết."
                  : contentType
                    ? "Chưa có bài viết thuộc chủ đề này."
                    : "Chưa có bài viết công khai."}
              </p>
              {page > 0 && (
                <button className="secondary-button" onClick={() => onPage(0)}>
                  Về trang đầu
                </button>
              )}
            </div>
          ) : (
            <ul className="event-cards">
              {response.data.map((article) => (
                <li key={article.id}>
                  <p className="eyebrow">{contentTypes[article.contentType]}</p>
                  <h2>
                    <Link to={{ pathname: `/articles/${article.id}`, search }}>
                      {article.title}
                    </Link>
                  </h2>
                  <p className="muted">
                    {article.sourceName} · {articleTime(article.publishedAt)}
                  </p>
                  {article.summary && (
                    <p className="event-preview">{article.summary}</p>
                  )}
                  <a
                    href={article.originalUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Đọc bài gốc ↗
                  </a>
                </li>
              ))}
            </ul>
          )}
          <ContentPager meta={response.meta} busy={loading} onPage={onPage} />
        </>
      )}
    </>
  );
}
