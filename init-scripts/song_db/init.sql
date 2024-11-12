BEGIN;

CREATE TABLE song (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    album VARCHAR(255),
    length VARCHAR(255),
    year INT,
    resource_id BIGINT
);

COMMIT;