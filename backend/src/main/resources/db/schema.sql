-- Rag-AiChat database schema.
-- Idempotent: safe to run repeatedly via spring.sql.init.

-- Remove legacy tables that are no longer used.
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS flyway_schema_history;

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS users (
    id varchar(36) PRIMARY KEY,
    username varchar(100) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    display_name varchar(100) NOT NULL,
    role varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS knowledge_bases (
    id varchar(36) PRIMARY KEY,
    name varchar(120) NOT NULL,
    description varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',
    created_by_id varchar(36),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS document_files (
    id varchar(36) PRIMARY KEY,
    knowledge_base_id varchar(36) NOT NULL REFERENCES knowledge_bases(id),
    file_name varchar(255) NOT NULL,
    original_path varchar(500) NOT NULL,
    mime_type varchar(120) NOT NULL,
    file_size bigint NOT NULL,
    parser varchar(40) NOT NULL DEFAULT 'TIKA',
    status varchar(40) NOT NULL DEFAULT 'UPLOADED',
    parse_error varchar(2000),
    chunk_count integer NOT NULL DEFAULT 0,
    uploaded_by_id varchar(36),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS document_chunks (
    id varchar(36) PRIMARY KEY,
    document_file_id varchar(36) NOT NULL REFERENCES document_files(id),
    knowledge_base_id varchar(36) NOT NULL REFERENCES knowledge_bases(id),
    chunk_index integer NOT NULL,
    content text NOT NULL,
    embedding_provider varchar(80) NOT NULL,
    embedding_model varchar(120) NOT NULL,
    embedding vector(1536),
    created_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_sessions (
    id varchar(36) PRIMARY KEY,
    user_id varchar(36) NOT NULL REFERENCES users(id),
    title varchar(200) NOT NULL,
    knowledge_base_scope jsonb,
    is_deleted boolean NOT NULL DEFAULT false,
    deleted_at timestamp,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id varchar(36) PRIMARY KEY,
    session_id varchar(36) NOT NULL REFERENCES chat_sessions(id),
    user_id varchar(36) NOT NULL REFERENCES users(id),
    role varchar(20) NOT NULL,
    content text NOT NULL,
    source_json jsonb,
    created_at timestamp NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_files_knowledge_base ON document_files(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_knowledge_base ON document_chunks(knowledge_base_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_file ON document_chunks(document_file_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages(session_id);

-- Keep the vector index compatible with pre-existing databases.
CREATE INDEX IF NOT EXISTS idx_document_chunks_embedding
    ON document_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Keep the soft-delete columns compatible with pre-existing databases.
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false;
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS deleted_at timestamp;

-- Default development administrator.
INSERT INTO users (id, username, password_hash, display_name, role, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$10$/f8qxkNpu1H2SZ/HwFRSk.qs/rGmYkOiUhDrv6YzXeph/f3b/EuV2',
    '管理员',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;
