import { PublicHeader } from "../../../components/layout/PublicHeader";
import { eventTypes, milestoneDate } from "../utils/eventLabels";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { eventsApi } from "../api/eventsApi";
import {
  CitationList,
  ContentError,
  ContentPager,
} from "../components/EventParts";
import "./events.css";
export function PublicEventsPage() {
  const [contentType, setContentType] = useState("");
  const [page, setPage] = useState(0),
    [reload, setReload] = useState(0),
    [response, setResponse] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null);
  useEffect(() => {
    const abort = new AbortController();
    eventsApi
      .publicList(page, contentType, abort.signal)
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

  function changeTopic(value) {
    if (value === contentType) return;
    setLoading(true);
    setError(null);
    setPage(0);
    setContentType(value);
  }

  return (
    <div className="editorial">
      <PublicHeader />
      <main className="editorial-wrap">
        <section className="editorial-hero">
          <div>
            <p className="editorial-kicker">
              <span /> HỒ SƠ KINH TẾ
            </p>
            <h1>
              Kinh tế qua <em>dòng thời gian.</em>
            </h1>
            <p>
              Theo dõi những bước ngoặt về chính sách và doanh nghiệp, với các
              mốc và tài liệu để đối chiếu.
            </p>
            <a className="editorial-cta" href="#event-list">
              Khám phá sự kiện <span aria-hidden="true">↓</span>
            </a>
          </div>
          <TimelineArtwork />
        </section>

        <section id="event-list" className="editorial-feed">
          <div className="editorial-section-head">
            <div>
              <p className="editorial-kicker">THƯ VIỆN DIỄN BIẾN</p>
              <h2>Sự kiện kinh tế</h2>
            </div>
            <div
              className="editorial-filters"
              role="group"
              aria-label="Lọc theo chủ đề"
            >
              {[["", "Tất cả"], ...Object.entries(eventTypes)].map(
                ([value, label]) => (
                  <button
                    key={value}
                    type="button"
                    aria-pressed={contentType === value}
                    onClick={() => changeTopic(value)}
                  >
                    {value === "BUSINESS" ? "Doanh nghiệp" : label}
                  </button>
                ),
              )}
            </div>
          </div>
          <ContentError error={error} />
          {error && (
            <button
              className="secondary-button"
              onClick={() => {
                setLoading(true);
                setError(null);
                setReload((n) => n + 1);
              }}
            >
              Thử lại
            </button>
          )}
          {loading && (
            <p className="editorial-empty" role="status">
              Đang tải sự kiện…
            </p>
          )}
          {!loading && !error && response && (
            <>
              <p className="editorial-count">
                {response.meta.totalItems} sự kiện
              </p>
              {response.data.length === 0 && (
                <div className="editorial-empty">
                  <h3>Chưa có sự kiện trong chủ đề này</h3>
                  <p>Các hồ sơ đã công khai sẽ xuất hiện tại đây.</p>
                </div>
              )}
              <div className="editorial-grid">
                {response.data.map((event, index) => (
                  <article key={event.id} className="editorial-card">
                    <div
                      className={`editorial-card-cover ${event.contentType === "BUSINESS" ? "business" : ""}`}
                      aria-hidden="true"
                    >
                      <TimelineArtwork />
                      <span>
                        {String(page * 10 + index + 1).padStart(2, "0")}
                      </span>
                    </div>
                    <div className="editorial-card-body">
                      <p className="editorial-tag">
                        {eventTypes[event.contentType]}
                      </p>
                      <h3>
                        <Link to={`/events/${event.id}`}>{event.title}</Link>
                      </h3>
                      <p className="event-preview">{event.overview}</p>
                      <Link
                        className="editorial-more"
                        to={`/events/${event.id}`}
                      >
                        Xem dòng thời gian <span aria-hidden="true">↗</span>
                      </Link>
                    </div>
                  </article>
                ))}
              </div>
              <ContentPager
                meta={response.meta}
                busy={loading}
                onPage={(next) => {
                  setLoading(true);
                  setError(null);
                  setPage(next);
                }}
              />
            </>
          )}
        </section>
      </main>
      <footer className="editorial-footer">
        EconNews <span>Hồ sơ sự kiện và bài viết kinh tế</span>
      </footer>
    </div>
  );
}

function TimelineArtwork() {
  return (
    <svg
      className="editorial-art"
      viewBox="0 0 540 330"
      fill="none"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <path
        d="M0 66H540 M0 132H540 M0 198H540 M0 264H540 M90 0V330 M180 0V330 M270 0V330 M360 0V330 M450 0V330"
        stroke="currentColor"
        opacity=".19"
      />
      <path
        d="M0 278 101 207 176 232 270 124 347 155 444 64 540 15V330H0Z"
        fill="currentColor"
        opacity=".12"
      />
      <path
        d="M0 278 101 207 176 232 270 124 347 155 444 64 540 15"
        stroke="currentColor"
        strokeWidth="2.5"
      />
      <path
        d="M101 207v123 M270 124v206 M444 64v266"
        stroke="currentColor"
        strokeDasharray="4 6"
        opacity=".5"
      />
      <circle cx="101" cy="207" r="7" fill="currentColor" />
      <circle cx="270" cy="124" r="7" fill="currentColor" />
      <circle cx="444" cy="64" r="7" fill="currentColor" />
      <circle cx="444" cy="64" r="23" stroke="currentColor" opacity=".45" />
    </svg>
  );
}
export function PublicEventDetailPage() {
  const { id } = useParams();
  return <PublicEventDetail key={id} id={id} />;
}
function PublicEventDetail({ id }) {
  const [data, setData] = useState(null),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(null),
    [reload, setReload] = useState(0);
  useEffect(() => {
    const abort = new AbortController();
    eventsApi
      .publicDetail(id, abort.signal)
      .then((result) => {
        if (!abort.signal.aborted) setData(result.data);
      })
      .catch((failure) => {
        if (!abort.signal.aborted) setError(failure);
      })
      .finally(() => {
        if (!abort.signal.aborted) setLoading(false);
      });
    return () => abort.abort();
  }, [id, reload]);
  return (
    <div className="editorial">
      <PublicHeader />
      <main className="editorial-wrap editorial-detail">
        <Link className="editorial-back" to="/events">
          ← Tất cả sự kiện
        </Link>
        <ContentError error={error} />
        {loading ? (
          <p role="status">Đang tải diễn biến…</p>
        ) : (
          !error && data && <EventStory data={data} />
        )}
        {error && (
          <button
            className="secondary-button"
            onClick={() => {
              setLoading(true);
              setError(null);
              setReload((n) => n + 1);
            }}
          >
            Thử lại
          </button>
        )}
      </main>
    </div>
  );
}

function EventStory({ data }) {
  const { event, milestones, overviewSources } = data;
  const latestMilestone = milestones.at(-1);

  return (
    <>
      <header className="editorial-detail-title">
        <p className="editorial-tag">{eventTypes[event.contentType]}</p>
        <h1>{event.title}</h1>
        <p>{milestones.length} mốc diễn biến</p>
      </header>

      <nav className="event-story-nav" aria-label="Nội dung hồ sơ">
        <a href="#event-overview">Tóm tắt</a>
        <a href="#event-timeline">Dòng thời gian</a>
        <a href="#event-references">Nguồn tổng quan</a>
      </nav>

      <section id="event-overview" className="event-panel editorial-panel">
        <h2>Sự kiện này nói về điều gì?</h2>
        <p className="event-body event-story-overview">{event.overview}</p>
        {latestMilestone && (
          <div className="event-story-latest">
            <h3>Mốc gần nhất trong hồ sơ</h3>
            <p>{milestoneDate(latestMilestone)}</p>
            <a href={`#milestone-${latestMilestone.id}`}>
              {latestMilestone.title}
            </a>
          </div>
        )}
      </section>

      <section id="event-timeline" className="event-panel editorial-panel">
        <h2>Dòng thời gian</h2>
        <p className="muted">
          Các mốc được trình bày theo thứ tự từ sớm đến muộn.
        </p>
        <ol className="event-timeline">
          {milestones.map((milestone) => (
            <li key={milestone.id} id={`milestone-${milestone.id}`}>
              <p className="eyebrow">{milestoneDate(milestone)}</p>
              <h3>{milestone.title}</h3>
              <p className="event-body">{milestone.description}</p>
              <h4>Nguồn xác nhận mốc</h4>
              <CitationList sources={milestone.sources} />
            </li>
          ))}
        </ol>
      </section>

      <section id="event-references" className="event-panel editorial-panel">
        <h2>Nguồn tổng quan</h2>
        <CitationList sources={overviewSources} />
      </section>
    </>
  );
}
