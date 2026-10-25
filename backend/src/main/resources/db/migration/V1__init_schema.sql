CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id varchar(36) PRIMARY KEY,
    username varchar(100) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    display_name varchar(100) NOT NULL,
    role varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE knowledge_bases (
    id varchar(36) PRIMARY KEY,
    name varchar(120) NOT NULL,
    description varchar(500),
    status varchar(20) NOT NULL DEFAULT 'ACTIVE',
    created_by_id varchar(36),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE document_files (
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

CREATE TABLE document_chunks (
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

CREATE TABLE chat_sessions (
    id varchar(36) PRIMARY KEY,
    user_id varchar(36) NOT NULL REFERENCES users(id),
    title varchar(200) NOT NULL,
    knowledge_base_scope jsonb,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE TABLE chat_messages (
    id varchar(36) PRIMARY KEY,
    session_id varchar(36) NOT NULL REFERENCES chat_sessions(id),
    user_id varchar(36) NOT NULL REFERENCES users(id),
    role varchar(20) NOT NULL,
    content text NOT NULL,
    source_json jsonb,
    created_at timestamp NOT NULL
);

CREATE TABLE audit_logs (
    id varchar(36) PRIMARY KEY,
    actor_id varchar(36),
    action varchar(100) NOT NULL,
    target_type varchar(80) NOT NULL,
    target_id varchar(100) NOT NULL,
    summary varchar(1000) NOT NULL,
    created_at timestamp NOT NULL
);

CREATE INDEX idx_document_files_knowledge_base ON document_files(knowledge_base_id);
CREATE INDEX idx_document_chunks_knowledge_base ON document_chunks(knowledge_base_id);
CREATE INDEX idx_document_chunks_document_file ON document_chunks(document_file_id);
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
