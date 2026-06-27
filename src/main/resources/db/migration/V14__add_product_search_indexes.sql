CREATE INDEX idx_products_created_at
    ON products (created_at);

CREATE INDEX idx_product_option_groups_product_sort_order
    ON product_option_groups (product_id, sort_order);

CREATE INDEX idx_product_option_values_group_sort_order
    ON product_option_values (option_group_id, sort_order);

CREATE INDEX idx_product_option_combinations_product_id
    ON product_option_combinations (product_id);
