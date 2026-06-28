CREATE TABLE payment_receipts
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    payment_id      BIGINT         NOT NULL,
    pg_provider     VARCHAR(20)    NOT NULL,
    payment_key     VARCHAR(255)   NOT NULL,
    receipt_url     VARCHAR(1000)  NOT NULL,
    total_amount    DECIMAL(10, 2) NOT NULL,
    supplied_amount DECIMAL(10, 2),
    vat             DECIMAL(10, 2),
    issued_at       DATETIME(6),
    raw_payload     TEXT,
    created_at      DATETIME(6)    NOT NULL,
    updated_at      DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_payment_receipts_payment_id UNIQUE (payment_id),
    CONSTRAINT chk_payment_receipts_total_amount CHECK (total_amount > 0)
);

CREATE INDEX idx_payment_receipts_payment_key
    ON payment_receipts (payment_key);
