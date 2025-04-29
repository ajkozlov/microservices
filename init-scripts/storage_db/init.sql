BEGIN;

CREATE TABLE storage (
    id BIGSERIAL PRIMARY KEY,
    storage_type VARCHAR(255) NOT NULL,
    bucket VARCHAR(255),
    path VARCHAR(255)
);

insert into storage (storage_type, bucket, path) values
('PERMANENT', 'resources-bucket', '/permanent'),
('STAGING', 'resources-bucket-stage', '/temporary');

COMMIT;