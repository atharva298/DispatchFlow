CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id),
    warehouse_id VARCHAR(64) NOT NULL,
    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_inventory_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT ck_inventory_available_non_negative CHECK (available_quantity >= 0),
    CONSTRAINT ck_inventory_reserved_non_negative CHECK (reserved_quantity >= 0)
);

CREATE INDEX ix_inventory_product_id ON inventory (product_id);
