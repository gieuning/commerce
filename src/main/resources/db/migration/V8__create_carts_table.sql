CREATE TABLE carts
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT,
    guest_token VARCHAR(255),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_carts_user_id (user_id),
    UNIQUE KEY uq_carts_guest_token (guest_token),
    CONSTRAINT chk_carts_owner CHECK (
        (user_id IS NOT NULL AND guest_token IS NULL)
            OR (user_id IS NULL AND guest_token IS NOT NULL)
    ),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE cart_items
(
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    cart_id               BIGINT      NOT NULL,
    product_id            BIGINT      NOT NULL,
    option_combination_id BIGINT,
    option_key            BIGINT      GENERATED ALWAYS AS (COALESCE(option_combination_id, 0)) STORED,
    quantity              INT         NOT NULL,
    created_at            DATETIME(6) NOT NULL,
    updated_at            DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_items_cart_product_option (cart_id, product_id, option_key),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_cart_items_option_combination FOREIGN KEY (option_combination_id)
        REFERENCES product_option_combinations (id),
    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0)
);
