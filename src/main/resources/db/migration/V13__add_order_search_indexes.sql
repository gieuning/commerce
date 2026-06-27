CREATE INDEX idx_orders_user_ordered_at
    ON orders (user_id, ordered_at);

CREATE INDEX idx_orders_user_total_price
    ON orders (user_id, total_price);

CREATE INDEX idx_orders_user_status_ordered_at
    ON orders (user_id, status, ordered_at);

CREATE INDEX idx_orders_user_status_total_price
    ON orders (user_id, status, total_price);

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);