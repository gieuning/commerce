CREATE TABLE product_option_combination_values
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    combination_id  BIGINT      NOT NULL,
    option_group_id BIGINT      NOT NULL,
    option_value_id BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_option_combination_values_combination FOREIGN KEY (combination_id) REFERENCES product_option_combinations (id),
    CONSTRAINT fk_product_option_combination_values_option_group FOREIGN KEY (option_group_id) REFERENCES product_option_groups (id),
    CONSTRAINT fk_product_option_combination_values_option_value FOREIGN KEY (option_value_id) REFERENCES product_option_values (id),
    CONSTRAINT uq_product_option_combination_value UNIQUE (combination_id, option_value_id),
    CONSTRAINT uq_product_option_combination_group UNIQUE (combination_id, option_group_id)
);
