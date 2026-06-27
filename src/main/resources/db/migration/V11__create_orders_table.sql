CREATE TABLE orders
(
    id                  BIGINT         NOT NULL AUTO_INCREMENT,
    user_id             BIGINT         NOT NULL,
    status              VARCHAR(20)    NOT NULL,
    total_product_price DECIMAL(10, 2) NOT NULL,
    discount_amount     DECIMAL(10, 2) NOT NULL,
    shipping_fee        DECIMAL(10, 2) NOT NULL,
    total_price         DECIMAL(10, 2) NOT NULL,
    ordered_at          DATETIME(6)    NOT NULL,
    created_at          DATETIME(6)    NOT NULL,
    updated_at          DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_orders_status CHECK (status IN ('CREATED', 'PAID', 'CANCELLED')),
    CONSTRAINT chk_orders_total_product_price CHECK (total_product_price >= 0),
    CONSTRAINT chk_orders_discount_amount CHECK (discount_amount >= 0),
    CONSTRAINT chk_orders_shipping_fee CHECK (shipping_fee >= 0),
    CONSTRAINT chk_orders_total_price CHECK (total_price >= 0)
);
