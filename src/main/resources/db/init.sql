-- Run lazily by H2 (via INIT in the JDBC URL) on the first physical connection,
-- so the schema and seed users are created on demand rather than at startup.
-- Idempotent (IF NOT EXISTS / MERGE) since H2 may run INIT per connection.

CREATE TABLE IF NOT EXISTS users (
    id    UUID PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS notifications (
    id           UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type         VARCHAR(255) NOT NULL,
    message      VARCHAR(255) NOT NULL,
    is_read      BOOLEAN NOT NULL,
    created_at   TIMESTAMP(9) WITH TIME ZONE
);

-- Seed users (ids match UUID.nameUUIDFromBytes(name), as the former UserSeeder produced).
MERGE INTO users (id, name, email) KEY(id) VALUES
    ('64489c85-dc2f-3078-bb85-cd87214b3810', 'Alice', 'alice@example.com'),
    ('2fc1c0be-b992-3d70-9697-5cfebf9d5c3b', 'Bob',   'bob@example.com'),
    ('150c16d9-d096-370a-b359-6111d7402397', 'Carol', 'carol@example.com');
