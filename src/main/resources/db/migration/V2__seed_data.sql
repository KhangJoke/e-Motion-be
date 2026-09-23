-- ==============================================================================
-- V2__seed_data.sql: Demo & Seeding Dataset for e-Motion EV Rental System
-- Target Database: PostgreSQL 17
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. USERS SEED DATA
-- Default Passwords:
-- Admin:    Admin@123 -> $2a$10$kBPL/8/uvi4u78Tz7N5u7uI7cTgP3AEKpSgXnB9/5u/JjRBQkP2uW
-- Staff/User: 123456  -> $2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW
-- ------------------------------------------------------------------------------
INSERT INTO users (user_id, full_name, phone, email, password, role, enabled, blocked, created_at, point)
VALUES
(1, 'Admin', '0901000001', 'admin@emotion.vn', '$2a$10$kBPL/8/uvi4u78Tz7N5u7uI7cTgP3AEKpSgXnB9/5u/JjRBQkP2uW', 'ROLE_ADMIN', true, false, NOW(), 1000),
(2, 'Nguyen Van Staff Q1', '0902000001', 'staff.quan1@emotion.vn', '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW', 'ROLE_STAFF', true, false, NOW(), 100),
(3, 'Tran Thi Staff Cầu Giấy', '0902000002', 'staff.caugiay@emotion.vn', '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW', 'ROLE_STAFF', true, false, NOW(), 100),
(4, 'Le Hoang Anh', '0903000001', 'customer.anh@gmail.com', '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW', 'ROLE_USER', true, false, NOW(), 250),
(5, 'Pham Thanh Binh', '0903000002', 'customer.binh@gmail.com', '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW', 'ROLE_USER', true, false, NOW(), 50),
(6, 'Đo Quynh Chi', '0903000003', 'customer.chi@gmail.com', '$2a$10$ddkVQGCZxmtA9BAiUgbvYOwMkADeHWp7vhu1tc07MGvovdGASlDOW', 'ROLE_USER', true, false, NOW(), 0)
ON CONFLICT (user_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 2. STATIONS SEED DATA (TP_HCM & HANOI)
-- ------------------------------------------------------------------------------
INSERT INTO stations (station_id, station_name, station_address, station_latitude, station_longitude, station_status, station_city, is_delete, created_at)
VALUES
(1, 'Trạm Sạc & Thuê Xe Quận 1', 'Số 12 Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', 10.7818, 106.6998, 'ACTIVE', 'TP_HCM', false, NOW()),
(2, 'Trạm Sạc & Thuê Xe Khu Công Nghệ Cao', 'Xa lộ Hà Nội, Phường Hiệp Phú, TP. Thủ Đức, TP. Hồ Chí Minh', 10.8544, 106.7876, 'ACTIVE', 'TP_HCM', false, NOW()),
(3, 'Trạm Sạc & Thuê Xe Cầu Giấy', 'Số 234 Xuân Thủy, Phường Dịch Vọng Hậu, Cầu Giấy, Hà Nội', 21.0368, 105.7828, 'ACTIVE', 'HANOI', false, NOW()),
(4, 'Trạm Sạc & Thuê Xe Hoàn Kiếm', 'Số 1 Tràng Tiền, Phường Phan Chu Trinh, Hoàn Kiếm, Hà Nội', 21.0245, 105.8572, 'ACTIVE', 'HANOI', false, NOW())
ON CONFLICT (station_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 3. STAFFS SEED DATA
-- ------------------------------------------------------------------------------
INSERT INTO staffs (staff_id, station_id, is_delete, user_id)
VALUES
(1, 1, false, 2),
(2, 3, false, 3)
ON CONFLICT (staff_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 4. VEHICLES SEED DATA
-- ------------------------------------------------------------------------------
INSERT INTO vehicles (
    vehicle_id, vehicle_name, description, brand, vehicle_status, vehicle_category,
    seats, price_4_hours, price_8_hours, price_12_hours, price_day, deposit_fee,
    consumption_rate, current_battery_level, battery_capacity, plate_number,
    last_maintenance, is_delete, point, station_id
)
VALUES
(
    1, 'VinFast VF 3 Plus 2024',
    'Mẫu mini điện thông minh, nhỏ gọn linh hoạt trong đô thị với quãng đường di chuyển lên tới 215 km/lần sạc.',
    'VINFAST', 'AVAILABLE', 'SUV',
    4, 300000, 420000, 480000, 600000, 3000000,
    8.5, 95, 18.64, '51K-933.21',
    NOW() - INTERVAL '10 days', false, 50, 1
),
(
    2, 'VinFast VF 5 Plus',
    'Xe SUV hạng A hiện đại, tiện nghi, trang bị đầy đủ tính năng thông minh và an toàn vượt trội.',
    'VINFAST', 'AVAILABLE', 'SUV',
    5, 450000, 630000, 720000, 900000, 5000000,
    11.2, 88, 37.23, '51K-552.19',
    NOW() - INTERVAL '15 days', false, 70, 1
),
(
    3, 'VinFast VF 8 Plus',
    'SUV điện hạng D mạnh mẽ, công nghệ hỗ trợ lái nâng cao ADAS cấp độ 2, tầm vận hành trên 400km.',
    'VINFAST', 'AVAILABLE', 'SUV',
    5, 900000, 1260000, 1440000, 1800000, 10000000,
    16.5, 72, 87.7, '51K-888.66',
    NOW() - INTERVAL '5 days', false, 120, 1
),
(
    4, 'VinFast VF 9 Plus 6 Chỗ',
    'SUV điện full-size hạng E cao cấp, ghế cơ trưởng thương gia, massage, màn hình giải trí đa phương tiện sang trọng.',
    'VINFAST', 'AVAILABLE', 'SUV',
    7, 1400000, 1960000, 2240000, 2800000, 15000000,
    20.0, 100, 123.0, '30L-999.88',
    NOW() - INTERVAL '2 days', false, 200, 3
),
(
    5, 'Tesla Model 3 Long Range',
    'Sedan thuần điện hàng đầu thế giới với khả năng tăng tốc 0-100km/h trong 4.4 giây, Autopilot tích hợp sẵn.',
    'TESLA', 'AVAILABLE', 'SEDAN',
    5, 1200000, 1680000, 1920000, 2400000, 12000000,
    14.0, 90, 82.0, '30L-333.12',
    NOW() - INTERVAL '7 days', false, 150, 3
),
(
    6, 'Tesla Model Y Performance',
    'Crossover điện gầm cao hiệu năng cao, không gian hành lý rộng rãi, vận hành êm ái trên mọi cung đường.',
    'TESLA', 'MAINTAINED', 'CROSSOVER',
    5, 1300000, 1820000, 2080000, 2600000, 15000000,
    15.5, 45, 78.1, '51K-777.99',
    NOW() - INTERVAL '1 day', false, 180, 2
),
(
    7, 'BYD Atto 3 Extended',
    'Mẫu SUV điện gia đình trẻ trung trang bị pin Blade an toàn độc quyền, nội thất thiết kế theo phong cách gym năng động.',
    'BYD', 'AVAILABLE', 'SUV',
    5, 600000, 840000, 960000, 1200000, 7000000,
    13.0, 85, 60.48, '30L-444.56',
    NOW() - INTERVAL '8 days', false, 90, 4
)
ON CONFLICT (vehicle_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 5. IMG VEHICLES SEED DATA
-- ------------------------------------------------------------------------------
INSERT INTO img_vehicles (img_id, img_url, main_img, vehicle_id)
VALUES
(1, 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf', true, 1),
(2, 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341', false, 1),
(3, 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d', true, 2),
(4, 'https://images.unsplash.com/photo-1503376780353-7e6692767b70', false, 2),
(5, 'https://images.unsplash.com/photo-1617788138017-80ad40651399', true, 3),
(6, 'https://images.unsplash.com/photo-1563720223185-11003d516935', true, 4),
(7, 'https://images.unsplash.com/photo-1560958089-b8a1929cea89', true, 5),
(8, 'https://images.unsplash.com/photo-1570125909232-eb263c188f7e', true, 6),
(9, 'https://images.unsplash.com/photo-1502877338535-766e1452684a', true, 7)
ON CONFLICT (img_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 6. DOCUMENTS SEED DATA (CCCD & Driving License)
-- ------------------------------------------------------------------------------
INSERT INTO documents (doc_id, doc_img_url, doc_type, doc_number, user_id)
VALUES
(1, 'https://res.cloudinary.com/emotion/docs/cccd_lehoanganh.jpg', 'CCCD', '079201008899', 4),
(2, 'https://res.cloudinary.com/emotion/docs/gplx_lehoanganh.jpg', 'LICENSE', '790188990123', 4),
(3, 'https://res.cloudinary.com/emotion/docs/cccd_phamthanhbinh.jpg', 'CCCD', '001202004455', 5),
(4, 'https://res.cloudinary.com/emotion/docs/gplx_phamthanhbinh.jpg', 'LICENSE', '010199445566', 5)
ON CONFLICT (doc_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 7. VEHICLE LOGS SEED DATA (Maintenance)
-- ------------------------------------------------------------------------------
INSERT INTO vehicle_logs (log_id, repair_items, total_cost, images, created_at, vehicle_id, staff_id, rental_id)
VALUES
(
    1,
    '[{"item":"Thay lọc gió cabin điều hòa","cost":800000.0},{"item":"Cân chỉnh góc đặt bánh xe và kiểm tra hệ thống treo","cost":1200000.0}]',
    2000000.0,
    '["https://images.unsplash.com/photo-1563720223185-11003d516935","https://images.unsplash.com/photo-1503376780353-7e6692767b70"]',
    NOW() - INTERVAL '1 day', 6, 1, NULL
)
ON CONFLICT (log_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 8. REPORTS SEED DATA
-- ------------------------------------------------------------------------------
INSERT INTO reports (report_id, title, description, status, type, is_deleted, created_at, user_id, staff_id)
VALUES
(1, 'Hỏi về quy trình hoàn tiền cọc giữ xe', 'Khách hàng thắc mắc thời gian hoàn cọc qua VNPay mất bao lâu sau khi trả xe thành công.', 'APPROVED', 'REPORT_USER', false, NOW() - INTERVAL '2 days', 4, 1),
(2, 'Đề xuất điều phối xe từ trạm Thủ Đức về Quận 1', 'Nhu cầu thuê xe tại Quận 1 cuối tuần tăng cao, đề xuất chuyển 02 xe VF 5 từ trạm Khu Công Nghệ Cao về Quận 1.', 'PENDING', 'VEHICLE_TRANSFER', false, NOW() - INTERVAL '1 day', NULL, 1)
ON CONFLICT (report_id) DO NOTHING;

-- ------------------------------------------------------------------------------
-- 9. SYNC SEQUENCES WITH SEEDED PRIMARY KEYS
-- ------------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('users', 'user_id'), coalesce(max(user_id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('stations', 'station_id'), coalesce(max(station_id), 1)) FROM stations;
SELECT setval(pg_get_serial_sequence('staffs', 'staff_id'), coalesce(max(staff_id), 1)) FROM staffs;
SELECT setval(pg_get_serial_sequence('vehicles', 'vehicle_id'), coalesce(max(vehicle_id), 1)) FROM vehicles;
SELECT setval(pg_get_serial_sequence('img_vehicles', 'img_id'), coalesce(max(img_id), 1)) FROM img_vehicles;
SELECT setval(pg_get_serial_sequence('documents', 'doc_id'), coalesce(max(doc_id), 1)) FROM documents;
SELECT setval(pg_get_serial_sequence('vehicle_logs', 'log_id'), coalesce(max(log_id), 1)) FROM vehicle_logs;
SELECT setval(pg_get_serial_sequence('reports', 'report_id'), coalesce(max(report_id), 1)) FROM reports;
