BEGIN;

CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    username TEXT NOT NULL REFERENCES application_users(email) ON DELETE CASCADE,
    device_id TEXT,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL UNIQUE,
    access_token_expiry TIMESTAMPTZ NOT NULL,
    refresh_token_expiry TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'USED', 'REVOKED'))
);

COMMIT;
