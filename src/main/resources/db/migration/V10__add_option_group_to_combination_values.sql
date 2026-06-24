ALTER TABLE product_option_combination_values
    ADD COLUMN option_group_id BIGINT NULL AFTER combination_id;

UPDATE product_option_combination_values combination_value
    JOIN product_option_values option_value
        ON combination_value.option_value_id = option_value.id
SET combination_value.option_group_id = option_value.option_group_id;

ALTER TABLE product_option_combination_values
    MODIFY COLUMN option_group_id BIGINT NOT NULL;

ALTER TABLE product_option_combination_values
    ADD CONSTRAINT fk_product_option_combination_values_option_group
        FOREIGN KEY (option_group_id) REFERENCES product_option_groups (id),
    ADD CONSTRAINT uq_product_option_combination_group
        UNIQUE (combination_id, option_group_id);
