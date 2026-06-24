-- Test-only H2 schema + seed, run lazily via H2 INIT on first connection.
-- Idempotent (IF NOT EXISTS / MERGE) so it is safe when test contexts share the
-- in-memory `eventdriven` database. Mirrors docker/postgres/init.sql.

CREATE TABLE IF NOT EXISTS users (
    id    UUID PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tasks (
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(32) NOT NULL,
    assignee_id UUID,
    created_at  TIMESTAMP(9) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(9) WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id           UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type         VARCHAR(255) NOT NULL,
    message      VARCHAR(255) NOT NULL,
    is_read      BOOLEAN NOT NULL,
    created_at   TIMESTAMP(9) WITH TIME ZONE
);

MERGE INTO users (id, name, email) KEY(id) VALUES
    ('64489c85-dc2f-3078-bb85-cd87214b3810', 'Alice', 'alice@example.com'),
    ('2fc1c0be-b992-3d70-9697-5cfebf9d5c3b', 'Bob',   'bob@example.com'),
    ('150c16d9-d096-370a-b359-6111d7402397', 'Carol', 'carol@example.com');
