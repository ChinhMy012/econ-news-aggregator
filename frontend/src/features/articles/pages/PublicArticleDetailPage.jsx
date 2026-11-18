import { useEffect, useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { PublicHeader } from "../../../components/layout/PublicHeader";
import { ContentError } from "../../events/components/EventParts";
import { getPublicArticle } from "../api/publicArticlesApi";
import { articleTime, contentTypes } from "../utils/articleLabels";
import {
  publicArticleSearch,
  readPublicArticleQuery,
} from "../utils/publicArticleQuery";

export function PublicArticleDetailPage() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const { contentType, page } = readPublicArticleQuery(params);
  const listSearch = publicArticleSearch(contentType, page);
  return <PublicArticleDetail key={id} id={id} listSearch={listSearch} />;
}

function PublicArticleDetail({ id, listSearch }) {
  const [article, setArticle] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    const abort = new AbortController();
    getPublicArticle(id, abort.signal)
      .then((result) => {
        if (!abort.signal.aborted) setArticle(result.data);
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure);
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false);
      });
    return () => abort.abort();
  }, [id, reload]);

  function retry() {
    setError(null);
    setLoading(true);
    setReload((value) => value + 1);
  }

  return (
    <div className="admin-shell">
      <PublicHeader />
      <main className="admin-main">
        <Link to={{ pathname: "/articles", search: listSearch }}>
          ← Danh sách bài viết
        </Link>
        {loading && <p role="status">Đang tải bài viết…</p>}

        {error?.status === 404 && <h1>Bài viết không còn khả dụng</h1>}
        {error && error.status !== 404 && (
          <>
            <ContentError error={error} />
            <button className="secondary-button" onClick={retry}>
              Thử lại
            </button>
          </>
        )}

        {!loading && !error && article && (
          <article className="event-panel">
            <p className="eyebrow">{contentTypes[article.contentType]}</p>
            <h1>{article.title}</h1>
            <p className="muted">
              {article.sourceName} · {articleTime(article.publishedAt)}
            </p>
            <h2>Tóm tắt</h2>
            <p className="event-body">
              {article.summary || "Bài viết chưa có tóm tắt."}
            </p>
            <a
              href={article.originalUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              Đọc bài gốc ↗
            </a>
          </article>
        )}
      </main>
    </div>
  );
}
