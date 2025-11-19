ALTER TABLE inventory
    ADD CONSTRAINT uk_inventory_product UNIQUE (product_id);

ALTER TABLE inventory
    ADD CONSTRAINT chk_inventory_qty_nonnegative CHECK (qty_on_hand >= 0);
