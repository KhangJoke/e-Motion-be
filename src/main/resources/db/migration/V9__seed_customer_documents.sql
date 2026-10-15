-- ==============================================================================
-- V9__seed_customer_documents.sql: Seed customer user with real CCCD and License
-- ==============================================================================

-- 1. Insert customer user Vu Duc Duong
INSERT INTO users (user_id, full_name, phone, email, password, role, enabled, blocked, created_at, point)
VALUES (
    7,
    'Vũ Đức Dương',
    '0903000004',
    'customer.duong@gmail.com',
    '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW',
    'ROLE_USER',
    true,
    false,
    NOW(),
    500
)
ON CONFLICT (user_id) DO UPDATE 
SET full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    role = EXCLUDED.role,
    enabled = EXCLUDED.enabled,
    blocked = EXCLUDED.blocked,
    point = EXCLUDED.point;

-- 2. Insert CCCD & Driver's License documents
INSERT INTO documents (doc_id, doc_img_url, doc_type, doc_number, user_id)
VALUES
(
    5,
    'https://res.cloudinary.com/dy45rrkhf/image/upload/v1761852957/CCCD/mrmkvntq7hb2rjzj3rdq.jpg',
    'CCCD',
    '001090060881',
    7
),
(
    6,
    'https://res.cloudinary.com/dy45rrkhf/image/upload/v1763672533/LICENSE/x9np1tdyhsfof6p1hc5e.jpg',
    'LICENSE',
    '761178007557',
    7
)
ON CONFLICT (doc_id) DO UPDATE
SET doc_img_url = EXCLUDED.doc_img_url,
    doc_type = EXCLUDED.doc_type,
    doc_number = EXCLUDED.doc_number,
    user_id = EXCLUDED.user_id;

-- 3. Sync sequences with seeded primary keys
SELECT setval(pg_get_serial_sequence('users', 'user_id'), coalesce(max(user_id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('documents', 'doc_id'), coalesce(max(doc_id), 1)) FROM documents;
