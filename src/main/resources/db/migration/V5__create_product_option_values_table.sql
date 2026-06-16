CREATE TABLE product_option_values
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    option_group_id BIGINT       NOT NULL,
    name            VARCHAR(255) NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_option_values_group FOREIGN KEY (option_group_id) REFERENCES product_option_groups (id)
);

-- 예) "블랙, 화이트, M, L"
-- 1 = 블랙
-- 2 = 화이트
-- 3 = M
-- 4 = L

