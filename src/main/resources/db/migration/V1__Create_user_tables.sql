CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    roles VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, roles)
);

CREATE TABLE deactivated_tokens (
    id UUID PRIMARY KEY,
    keep_until TIMESTAMP NOT NULL CHECK (keep_until > NOW())
);

