CREATE INDEX idx_orders_user_total_price
    ON orders (user_id, total_price);

CREATE INDEX idx_orders_user_status_total_price
    ON orders (user_id, status, total_price);
