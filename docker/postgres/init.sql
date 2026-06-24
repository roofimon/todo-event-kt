-- Provisioned by Postgres on first container start (/docker-entrypoint-initdb.d).
-- The app runs with ddl-auto=none, so this is the source of truth for the schema.
-- Column names/types must match the JPA entities (UserEntity, NotificationEntity).

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
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id           UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type         VARCHAR(255) NOT NULL,
    message      VARCHAR(255) NOT NULL,
    is_read      BOOLEAN NOT NULL,
    created_at   TIMESTAMPTZ
);

-- Seed users (ids match UUID.nameUUIDFromBytes(name), as the former UserSeeder produced).
INSERT INTO users (id, name, email) VALUES
    ('64489c85-dc2f-3078-bb85-cd87214b3810', 'Alice', 'alice@example.com'),
    ('2fc1c0be-b992-3d70-9697-5cfebf9d5c3b', 'Bob',   'bob@example.com'),
    ('150c16d9-d096-370a-b359-6111d7402397', 'Carol', 'carol@example.com')
ON CONFLICT (id) DO NOTHING;
