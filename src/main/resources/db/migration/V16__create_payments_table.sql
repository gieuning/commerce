CREATE TABLE payments
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    order_id          BIGINT         NOT NULL,
    user_id           BIGINT         NOT NULL,
    payment_key       VARCHAR(255),
    merchant_order_id VARCHAR(255)   NOT NULL,
    pg_provider       VARCHAR(20)    NOT NULL,
    method            VARCHAR(30)    NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    amount            DECIMAL(10, 2) NOT NULL,
    approved_at       DATETIME(6),
    cancelled_at      DATETIME(6),
    failure_code      VARCHAR(255),
    failure_message   TEXT,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_payments_payment_key UNIQUE (payment_key),
    CONSTRAINT uq_payments_merchant_order_id UNIQUE (merchant_order_id),
    CONSTRAINT chk_payments_amount CHECK (amount > 0)
);

CREATE INDEX idx_payments_order_id
    ON payments (order_id);

CREATE INDEX idx_payments_user_status
    ON payments (user_id, status);
