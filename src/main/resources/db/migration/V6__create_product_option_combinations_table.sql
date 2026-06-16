CREATE TABLE product_option_combinations
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    product_id       BIGINT         NOT NULL,
    additional_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    stock            INT            NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_option_combinations_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT chk_product_option_combinations_stock CHECK (stock >= 0),
    CONSTRAINT chk_product_option_combinations_additional_price CHECK (additional_price >= 0),
    CONSTRAINT chk_product_option_combinations_status CHECK (status IN ('FOR_SALE', 'STOP_SALE', 'OUT_OF_STOCK'))
);

-- 실제 판매/재고 단위

-- ex)
-- 맨투맨 / 블랙 / M
-- 맨투맨 / 화이트 / L
-- 이 각각이 OptionCombination 하나
