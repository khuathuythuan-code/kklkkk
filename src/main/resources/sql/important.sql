-- xóa các giao dịch từ sau 0h ngày hôm nay để chức năng đặt mục tiêu chạy đúng
DELETE FROM transactions
WHERE created_at > CURDATE();


--nếu hiện lỗi #1304 - PROCEDURE insert_diverse_transactions already exists
-- thì copy câu trên
DROP PROCEDURE IF EXISTS insert_diverse_transactions;


--Xóa sạch dữ liệu, reset AUTO_INCREMENT
TRUNCATE TABLE transactions;


--Nếu bị báo lỗi foreign key constraint khi TRUNCATE:
--Cách A – Tắt FK tạm thời:
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;


--Cách B – Xóa theo điều kiện(An toàn hơn, không tắt FK)
DELETE FROM transactions WHERE user_id = 1;
