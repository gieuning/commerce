CREATE TABLE products
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255)   NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2) NOT NULL,
    stock          INT            NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    image_url      VARCHAR(500),
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_products_status CHECK (status IN ('FOR_SALE', 'STOP_SALE', 'OUT_OF_STOCK'))
);
