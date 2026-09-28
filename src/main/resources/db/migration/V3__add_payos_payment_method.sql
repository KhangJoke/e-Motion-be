-- V3: Cho phép phương thức thanh toán PAYOS trong bảng payments
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_method_check;
ALTER TABLE payments ADD CONSTRAINT payments_payment_method_check 
    CHECK (payment_method::text = ANY (ARRAY['VNPAY'::character varying, 'CASH'::character varying, 'PAYOS'::character varying]::text[]));
