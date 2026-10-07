CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES customer_orders(id),
    tracking_number VARCHAR(40) NOT NULL UNIQUE,
    carrier VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    estimated_delivery_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_shipments_status CHECK (status IN (
        'CREATED', 'DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'
    ))
);
