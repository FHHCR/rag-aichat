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
