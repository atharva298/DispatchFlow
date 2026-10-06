CREATE TABLE inventory_reservations (
    id UUID PRIMARY KEY,
    order_item_id UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    inventory_id UUID NOT NULL REFERENCES inventory(id),
    quantity INTEGER NOT NULL,
    CONSTRAINT ck_inventory_reservations_quantity_positive CHECK (quantity > 0),
    CONSTRAINT uk_inventory_reservation_item_stock UNIQUE (order_item_id, inventory_id)
);

CREATE INDEX ix_inventory_reservations_inventory_id ON inventory_reservations (inventory_id);
