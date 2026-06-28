CREATE TABLE payment_events
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    payment_id       BIGINT      NOT NULL,
    event_type       VARCHAR(30) NOT NULL,
    payment_status   VARCHAR(20) NOT NULL,
    pg_provider      VARCHAR(20) NOT NULL,
    pg_event_id      VARCHAR(255),
    request_payload  TEXT,
    response_payload TEXT,
    failure_code     VARCHAR(255),
    failure_message  TEXT,
    occurred_at      DATETIME(6) NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_payment_events_payment_id
    ON payment_events (payment_id);

CREATE INDEX idx_payment_events_pg_event_id
    ON payment_events (pg_event_id);
