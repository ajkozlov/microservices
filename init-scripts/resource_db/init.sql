BEGIN;

CREATE TABLE resource (
    id BIGSERIAL PRIMARY KEY,
    mp3 VARCHAR,
    storage_type VARCHAR
);

COMMIT;