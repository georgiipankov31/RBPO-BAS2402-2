BEGIN;

CREATE TABLE application_users (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE CHECK (email = lower(btrim(email)) AND email <> ''),
    password_hash VARCHAR(60) NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    enabled BOOLEAN NOT NULL DEFAULT true
);

COMMIT;
