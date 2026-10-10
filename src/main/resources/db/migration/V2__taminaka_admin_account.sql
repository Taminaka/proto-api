-- Required admin account for the demo flow.
-- Password: Admin123!

INSERT INTO users (email, password_hash, full_name, active_mode, status, is_verified, created_at, updated_at)
VALUES (
    'admin@taminaka.com',
    '$2a$10$/0x0VlNC4HItxEaXjonNHuXE3no0ecdeZhriaWeNCOZRlPZ5l1ksi',
    'Administrador Tamaka',
    'TURISTA',
    'ACTIVE',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    is_verified = true,
    status = 'ACTIVE';

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@taminaka.com'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'TURISTA' FROM users WHERE email = 'admin@taminaka.com'
ON CONFLICT DO NOTHING;
