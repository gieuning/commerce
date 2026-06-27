CREATE TABLE order_items
(
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    order_id              BIGINT         NOT NULL,
    product_id            BIGINT         NOT NULL,
    option_combination_id BIGINT,
    product_name          VARCHAR(255)   NOT NULL,
    option_values         TEXT,
    unit_price            DECIMAL(10, 2) NOT NULL,
    quantity              INT            NOT NULL,
    subtotal              DECIMAL(10, 2) NOT NULL,
    created_at            DATETIME(6)    NOT NULL,
    updated_at            DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_order_items_option_combination FOREIGN KEY (option_combination_id)
        REFERENCES product_option_combinations (id),
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal >= 0)
);
