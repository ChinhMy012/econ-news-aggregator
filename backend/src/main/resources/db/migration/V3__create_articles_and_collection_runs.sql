ALTER TABLE rss_feeds ADD CONSTRAINT uk_rss_feeds_id_source UNIQUE (id, source_id);

CREATE TABLE collection_runs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id BIGINT NOT NULL,
    feed_id BIGINT NOT NULL,
    feed_url VARCHAR(2048) NOT NULL,
    requested_by BIGINT NOT NULL REFERENCES admin_users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING'
        CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED', 'INTERRUPTED')),
    received_count INTEGER NOT NULL DEFAULT 0 CHECK (received_count >= 0),
    inserted_count INTEGER NOT NULL DEFAULT 0 CHECK (inserted_count >= 0),
    duplicate_count INTEGER NOT NULL DEFAULT 0 CHECK (duplicate_count >= 0),
    invalid_count INTEGER NOT NULL DEFAULT 0 CHECK (invalid_count >= 0),
    error_code VARCHAR(60),
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMPTZ,
    FOREIGN KEY (feed_id, source_id) REFERENCES rss_feeds(id, source_id) ON DELETE RESTRICT,
    CHECK (received_count = inserted_count + duplicate_count + invalid_count),
    CHECK ((status = 'RUNNING' AND finished_at IS NULL)
        OR (status <> 'RUNNING' AND finished_at IS NOT NULL AND finished_at >= started_at))
);
CREATE UNIQUE INDEX uk_collection_runs_active_feed ON collection_runs(feed_id) WHERE status = 'RUNNING';
CREATE INDEX idx_collection_runs_feed_id_id ON collection_runs(feed_id, id DESC);

CREATE TABLE articles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    source_id BIGINT NOT NULL REFERENCES news_sources(id) ON DELETE RESTRICT,
    first_feed_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL CHECK (btrim(title) <> ''),
    original_url VARCHAR(2048) NOT NULL,
    normalized_url VARCHAR(2048) NOT NULL,
    url_hash CHAR(64) NOT NULL CHECK (url_hash ~ '^[0-9a-f]{64}$'),
    summary VARCHAR(4000),
    published_at TIMESTAMPTZ,
    collected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PUBLISHED', 'REJECTED', 'HIDDEN')),
    content_type VARCHAR(12) CHECK (content_type IN ('POLICY', 'BUSINESS')),
    moderation_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (first_feed_id, source_id) REFERENCES rss_feeds(id, source_id) ON DELETE RESTRICT,
    CONSTRAINT uk_articles_source_url_hash UNIQUE (source_id, url_hash),
    CHECK (status <> 'PUBLISHED' OR content_type IS NOT NULL),
    CHECK (status NOT IN ('REJECTED', 'HIDDEN') OR (moderation_reason IS NOT NULL AND length(btrim(moderation_reason)) > 0))
);
CREATE INDEX idx_articles_status_id ON articles(status, id DESC);
CREATE INDEX idx_articles_source_id_id ON articles(source_id, id DESC);
