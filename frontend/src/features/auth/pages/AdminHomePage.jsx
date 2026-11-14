import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { authenticatedRequest } from "../../../api/httpClient";
import { WorkspaceIcon } from "../../../components/layout/WorkspaceIcon";

const metrics = [
  {
    title: "Nguồn tin",
    endpoint: "/admin/sources?size=1",
    href: "/admin/sources",
    icon: "source",
    tone: "blue",
  },
  {
    title: "Bài chờ duyệt",
    endpoint: "/admin/articles?status=PENDING&size=5",
    href: "/admin/articles",
    icon: "article",
    tone: "amber",
  },
  {
    title: "Sự kiện công khai",
    endpoint: "/admin/events?status=PUBLISHED&size=1",
    href: "/admin/events",
    icon: "event",
    tone: "green",
  },
  {
    title: "Tài liệu tham chiếu",
    endpoint: "/admin/documents?size=1",
    href: "/admin/documents",
    icon: "document",
    tone: "violet",
  },
];

export function AdminHomePage() {
  const [results, setResults] = useState(null);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    Promise.allSettled(
      metrics.map(({ endpoint }) =>
        authenticatedRequest(endpoint, { signal: controller.signal }),
      ),
    ).then((data) => {
      if (!controller.signal.aborted) setResults(data);
    });
    return () => controller.abort();
  }, [refresh]);

  const pendingResult = results?.[1];
  const pending =
    pendingResult?.status === "fulfilled" ? pendingResult.value.data : [];
  const hasError = results?.some(({ status }) => status === "rejected");

  return (
    <main className="workspace-dashboard">
      <section className="dashboard-intro">
        <div>
          <p className="dashboard-kicker">ECONNEWS STUDIO</p>
          <h1>Tổng quan biên tập</h1>
          <p>Theo dõi nguồn tin, hàng chờ duyệt và nội dung công khai.</p>
        </div>
        <Link className="primary-button" to="/admin/events">
          Quản lý sự kiện <WorkspaceIcon name="arrow" size={17} />
        </Link>
      </section>

      {hasError && (
        <div className="error-banner" role="alert">
          Một số số liệu chưa tải được.
          <button
            className="secondary-button"
            onClick={() => {
              setResults(null);
              setRefresh((n) => n + 1);
            }}
          >
            Tải lại
          </button>
        </div>
      )}

      <section
        className="dashboard-metrics"
        aria-label="Số liệu nội dung"
        aria-busy={!results}
      >
        {metrics.map(({ title, href, icon, tone }, index) => {
          const result = results?.[index];
          const total =
            result?.status === "fulfilled"
              ? result.value.meta?.totalItems
              : null;
          return (
            <Link className="dashboard-metric" to={href} key={title}>
              <span className={`dashboard-icon ${tone}`}>
                <WorkspaceIcon name={icon} />
              </span>
              <span className="dashboard-metric-title">{title}</span>
              <strong>
                {total == null ? "—" : total.toLocaleString("vi-VN")}
              </strong>
              <span className="dashboard-metric-arrow">
                <WorkspaceIcon name="arrow" size={16} />
              </span>
            </Link>
          );
        })}
      </section>

      <section className="dashboard-panel">
        <header>
          <div>
            <h2>Hàng chờ biên tập</h2>
            <p>Các bài mới nhất cần phân loại và duyệt.</p>
          </div>
          <Link className="secondary-button" to="/admin/articles">
            Xem tất cả <WorkspaceIcon name="arrow" size={16} />
          </Link>
        </header>
        {!results && (
          <p className="dashboard-empty" role="status">
            Đang tải hàng chờ…
          </p>
        )}
        {pendingResult?.status === "fulfilled" && !pending.length && (
          <p className="dashboard-empty">Hàng chờ hiện chưa có bài.</p>
        )}
        {pending.length > 0 && (
          <ul className="dashboard-queue">
            {pending.map((article) => (
              <li key={article.id}>
                <span className="dashboard-icon blue">
                  <WorkspaceIcon name="article" size={17} />
                </span>
                <Link to={`/admin/articles/${article.id}`}>
                  <strong>{article.title}</strong>
                  <small>{article.sourceName}</small>
                </Link>
                <span className="dashboard-pending">Chờ duyệt</span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
