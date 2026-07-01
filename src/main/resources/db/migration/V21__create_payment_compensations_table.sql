CREATE TABLE payment_compensations
(
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    payment_id    BIGINT         NOT NULL,
    pg_provider   VARCHAR(20)    NOT NULL,
    payment_key   VARCHAR(255)   NOT NULL,
    cancel_amount DECIMAL(10, 2) NOT NULL,
    reason        VARCHAR(255)   NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    attempt_count INT            NOT NULL,
    max_attempts  INT            NOT NULL,
    next_retry_at DATETIME(6),
    last_error    TEXT,
    created_at    DATETIME(6)    NOT NULL,
    updated_at    DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_payment_compensations_cancel_amount CHECK (cancel_amount > 0)
);

CREATE INDEX idx_payment_compensations_payment_id
    ON payment_compensations (payment_id);

-- 스케줄러 폴링: WHERE status = 'PENDING' AND next_retry_at <= now
CREATE INDEX idx_payment_compensations_status_next_retry
    ON payment_compensations (status, next_retry_at);
