CREATE TABLE payment_cancellations
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    payment_id          BIGINT         NOT NULL,
    pg_provider         VARCHAR(20)    NOT NULL,
    payment_key         VARCHAR(255)   NOT NULL,
    pg_cancellation_key VARCHAR(255),
    cancel_amount       DECIMAL(10, 2) NOT NULL,
    cancel_reason       VARCHAR(255)   NOT NULL,
    cancelled_at        DATETIME(6)    NOT NULL,
    request_payload     TEXT,
    response_payload    TEXT,
    created_at          DATETIME(6)    NOT NULL,
    updated_at          DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_payment_cancellations_cancel_amount CHECK (cancel_amount > 0)
);

CREATE INDEX idx_payment_cancellations_payment_id
    ON payment_cancellations (payment_id);

CREATE INDEX idx_payment_cancellations_payment_key
    ON payment_cancellations (payment_key);

CREATE INDEX idx_payment_cancellations_cancelled_at
    ON payment_cancellations (cancelled_at);
