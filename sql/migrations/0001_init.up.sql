BEGIN;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL CHECK (btrim(name) <> '')
);

CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    number TEXT NOT NULL UNIQUE CHECK (btrim(number) <> ''),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    available BOOLEAN NOT NULL
);

CREATE TABLE housing_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL CHECK (status IN ('NEW', 'APPROVED', 'CANCELLED', 'COMPLETED'))
);

CREATE TABLE residence_contracts (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES housing_applications(id) ON DELETE CASCADE,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL CHECK (check_out_date >= check_in_date),
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED'))
);

CREATE TABLE room_transfer_requests (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL REFERENCES residence_contracts(id) ON DELETE CASCADE,
    target_room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL CHECK (status IN ('NEW', 'APPROVED', 'CANCELLED'))
);

COMMIT;
