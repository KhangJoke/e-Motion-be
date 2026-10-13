-- V7: Chuyển kiểu dữ liệu cột img trong rental_checklists sang TEXT để hỗ trợ lưu nhiều URL ảnh
ALTER TABLE rental_checklists ALTER COLUMN img TYPE TEXT;
