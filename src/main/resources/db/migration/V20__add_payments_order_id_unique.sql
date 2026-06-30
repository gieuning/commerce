ALTER TABLE payments
    DROP INDEX idx_payments_order_id,
    ADD CONSTRAINT uq_payments_order_id UNIQUE (order_id);
