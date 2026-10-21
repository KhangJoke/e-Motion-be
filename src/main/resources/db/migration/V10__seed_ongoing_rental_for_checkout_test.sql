-- ==============================================================================
-- V10__seed_ongoing_rental_for_checkout_test.sql
-- Seed an ongoing rental with completed check-in for customer.duong@gmail.com
-- Allows testing the complete check-out flow (checklist checkout & checkout payment)
-- ==============================================================================

-- 1. Seed Rental with status ONGOING
INSERT INTO rentals (
    rental_id,
    rental_status,
    start_time,
    end_time,
    cancel_notified,
    overdue_notified,
    expiring_notified,
    rent_fee,
    discount_point,
    created_at,
    submission_id,
    contract_url,
    submission_url,
    contract_status,
    vehicle_id,
    station_id,
    user_id,
    staff_id
)
VALUES (
    10,
    'ONGOING',
    NOW() - INTERVAL '5 hours',
    NOW() + INTERVAL '19 hours',
    false,
    false,
    false,
    900000.0,
    0,
    NOW() - INTERVAL '6 hours',
    1001,
    'https://docuseal.com/d/demo-contract-customer-duong',
    'https://docuseal.com/s/demo-contract-customer-duong',
    'SIGNED',
    2,
    1,
    (SELECT user_id FROM users WHERE email = 'customer.duong@gmail.com' LIMIT 1),
    1
)
ON CONFLICT (rental_id) DO UPDATE
SET rental_status = EXCLUDED.rental_status,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    cancel_notified = EXCLUDED.cancel_notified,
    overdue_notified = EXCLUDED.overdue_notified,
    expiring_notified = EXCLUDED.expiring_notified,
    rent_fee = EXCLUDED.rent_fee,
    discount_point = EXCLUDED.discount_point,
    contract_url = EXCLUDED.contract_url,
    submission_url = EXCLUDED.submission_url,
    contract_status = EXCLUDED.contract_status,
    vehicle_id = EXCLUDED.vehicle_id,
    station_id = EXCLUDED.station_id,
    user_id = EXCLUDED.user_id,
    staff_id = EXCLUDED.staff_id;

-- 2. Seed Deposit with status HOLD
INSERT INTO deposits (
    deposit_id,
    deposit_status,
    deposit_amount,
    created_at,
    rental_id
)
VALUES (
    10,
    'HOLD',
    5000000.0,
    NOW() - INTERVAL '5 hours',
    10
)
ON CONFLICT (deposit_id) DO UPDATE
SET deposit_status = EXCLUDED.deposit_status,
    deposit_amount = EXCLUDED.deposit_amount,
    rental_id = EXCLUDED.rental_id;

-- 3. Seed Initial Rental Payment (Rent fee + Deposit)
INSERT INTO payments (
    payment_id,
    total_amount,
    payment_method,
    payment_type,
    payment_status,
    created_at,
    is_delete,
    txn_ref,
    payment_description,
    response_code,
    transaction_no,
    bank_code,
    pay_date,
    user_id,
    rental_id,
    deposit_id
)
VALUES (
    10,
    5900000.0,
    'VNPAY',
    'RENTAL',
    'SUCCESS',
    NOW() - INTERVAL '5 hours',
    false,
    'TXN_RENTAL_010_DUONG',
    'Thanh toan tien thue xe va dat coc cho don thue #10',
    '00',
    '999999',
    'NCB',
    NOW() - INTERVAL '5 hours',
    (SELECT user_id FROM users WHERE email = 'customer.duong@gmail.com' LIMIT 1),
    10,
    10
)
ON CONFLICT (payment_id) DO UPDATE
SET total_amount = EXCLUDED.total_amount,
    payment_method = EXCLUDED.payment_method,
    payment_type = EXCLUDED.payment_type,
    payment_status = EXCLUDED.payment_status,
    txn_ref = EXCLUDED.txn_ref,
    payment_description = EXCLUDED.payment_description,
    user_id = EXCLUDED.user_id,
    rental_id = EXCLUDED.rental_id,
    deposit_id = EXCLUDED.deposit_id,
    transaction_no = EXCLUDED.transaction_no;

-- 4. Seed Check-in Checklist (CheckType = CHECK_IN)
INSERT INTO rental_checklists (
    checklist_id,
    check_type,
    fee,
    img,
    current_battery,
    created_at,
    staff_id,
    rental_id
)
VALUES (
    10,
    'CHECK_IN',
    0.0,
    '["https://res.cloudinary.com/emotion/checkin/vf5_front.jpg","https://res.cloudinary.com/emotion/checkin/vf5_back.jpg","https://res.cloudinary.com/emotion/checkin/vf5_interior.jpg"]',
    88.0,
    NOW() - INTERVAL '5 hours',
    1,
    10
)
ON CONFLICT (checklist_id) DO UPDATE
SET check_type = EXCLUDED.check_type,
    fee = EXCLUDED.fee,
    img = EXCLUDED.img,
    current_battery = EXCLUDED.current_battery,
    staff_id = EXCLUDED.staff_id,
    rental_id = EXCLUDED.rental_id;

-- 5. Update Vehicle status to ONGOING
UPDATE vehicles
SET vehicle_status = 'ONGOING'
WHERE vehicle_id = 2;

-- 6. Synchronize sequence values with seeded primary keys
SELECT setval(pg_get_serial_sequence('rentals', 'rental_id'), coalesce(max(rental_id), 1)) FROM rentals;
SELECT setval(pg_get_serial_sequence('deposits', 'deposit_id'), coalesce(max(deposit_id), 1)) FROM deposits;
SELECT setval(pg_get_serial_sequence('payments', 'payment_id'), coalesce(max(payment_id), 1)) FROM payments;
SELECT setval(pg_get_serial_sequence('rental_checklists', 'checklist_id'), coalesce(max(checklist_id), 1)) FROM rental_checklists;
