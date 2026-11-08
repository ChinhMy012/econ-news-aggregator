CREATE TABLE documents (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(300) NOT NULL CHECK (btrim(title) <> ''),
    issuer VARCHAR(200) NOT NULL CHECK (btrim(issuer) <> ''),
    url VARCHAR(2048) NOT NULL UNIQUE,
    issued_on DATE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(300) NOT NULL CHECK (btrim(title) <> ''),
    content_type VARCHAR(12) NOT NULL CHECK (content_type IN ('POLICY', 'BUSINESS')),
    overview VARCHAR(10000) NOT NULL DEFAULT '',
    status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    hidden_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (status <> 'PUBLISHED' OR btrim(overview) <> ''),
    CHECK (status <> 'HIDDEN' OR (hidden_reason IS NOT NULL AND btrim(hidden_reason) <> ''))
);
CREATE INDEX idx_events_status_id ON events(status, id DESC);
CREATE TABLE milestones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE RESTRICT,
    title VARCHAR(300) NOT NULL CHECK (btrim(title) <> ''),
    description VARCHAR(4000) NOT NULL DEFAULT '',
    date_precision VARCHAR(5) NOT NULL CHECK (date_precision IN ('YEAR', 'MONTH', 'DAY')),
    date_year INTEGER NOT NULL CHECK (date_year BETWEEN 1 AND 9999),
    date_month INTEGER CHECK (date_month BETWEEN 1 AND 12),
    date_day INTEGER CHECK (date_day BETWEEN 1 AND 31),
    status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id, event_id),
    CHECK ((date_precision = 'YEAR' AND date_month IS NULL AND date_day IS NULL)
        OR (date_precision = 'MONTH' AND date_month IS NOT NULL AND date_day IS NULL)
        OR (date_precision = 'DAY' AND date_month IS NOT NULL AND date_day IS NOT NULL)),
    CHECK (CASE WHEN date_precision = 'DAY' THEN make_date(date_year, date_month, date_day) IS NOT NULL ELSE TRUE END),
    CHECK (status <> 'PUBLISHED' OR btrim(description) <> '')
);
CREATE INDEX idx_milestones_event_date ON milestones(event_id, date_year, date_month, date_day, id);
CREATE TABLE event_articles (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE RESTRICT,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE RESTRICT,
    PRIMARY KEY (event_id, article_id)
);
CREATE INDEX idx_event_articles_article ON event_articles(article_id, event_id);
CREATE TABLE event_documents (
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE RESTRICT,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE RESTRICT,
    PRIMARY KEY (event_id, document_id)
);
CREATE INDEX idx_event_documents_document ON event_documents(document_id, event_id);
CREATE TABLE event_overview_articles (
    event_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, article_id),
    FOREIGN KEY (event_id, article_id) REFERENCES event_articles(event_id, article_id) ON DELETE RESTRICT
);
CREATE TABLE event_overview_documents (
    event_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, document_id),
    FOREIGN KEY (event_id, document_id) REFERENCES event_documents(event_id, document_id) ON DELETE RESTRICT
);
CREATE TABLE milestone_articles (
    event_id BIGINT NOT NULL,
    milestone_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    PRIMARY KEY (milestone_id, article_id),
    FOREIGN KEY (milestone_id, event_id) REFERENCES milestones(id, event_id) ON DELETE RESTRICT,
    FOREIGN KEY (event_id, article_id) REFERENCES event_articles(event_id, article_id) ON DELETE RESTRICT
);
CREATE INDEX idx_milestone_articles_article ON milestone_articles(article_id, milestone_id);
CREATE TABLE milestone_documents (
    event_id BIGINT NOT NULL,
    milestone_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    PRIMARY KEY (milestone_id, document_id),
    FOREIGN KEY (milestone_id, event_id) REFERENCES milestones(id, event_id) ON DELETE RESTRICT,
    FOREIGN KEY (event_id, document_id) REFERENCES event_documents(event_id, document_id) ON DELETE RESTRICT
);
CREATE INDEX idx_milestone_documents_document ON milestone_documents(document_id, milestone_id);

CREATE INDEX idx_overview_articles_article ON event_overview_articles(article_id, event_id);
CREATE INDEX idx_overview_documents_document ON event_overview_documents(document_id, event_id);
