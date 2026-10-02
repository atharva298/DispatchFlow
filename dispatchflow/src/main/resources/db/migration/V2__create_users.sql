CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN', 'OPERATOR'))
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));
