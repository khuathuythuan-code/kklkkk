DROP PROCEDURE IF EXISTS insert_diverse_transactions;
DELIMITER $$

CREATE PROCEDURE insert_diverse_transactions()
BEGIN
    DECLARE y INT;
    DECLARE m INT;
    DECLARE d INT;
    DECLARE i INT;

    SET y = 2022;

    WHILE y <= 2026 DO
        SET m = 1;

        WHILE m <= 12 DO

            -- ============================
            -- USER 1 - THU (3 loại)
            -- ============================
            SET i = 1;
            WHILE i <= 3 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Thu','Salary',FLOOR(3000 + RAND()*3000),'Monthly salary',
                        CONCAT(y,'-',m,'-',d,' 09:00:00'),'Cash',
                        CONCAT(y,'-',m,'-',d,' 09:00:00'));
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Thu','Freelance',FLOOR(500 + RAND()*2000),'Freelance project',
                        CONCAT(y,'-',m,'-',d,' 14:00:00'),'Bank Transfer',
                        CONCAT(y,'-',m,'-',d,' 14:00:00'));
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Thu','Investment',FLOOR(200 + RAND()*1000),'Investment profit',
                        CONCAT(y,'-',m,'-',d,' 16:00:00'),'Bank Transfer',
                        CONCAT(y,'-',m,'-',d,' 16:00:00'));
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- ============================
            -- USER 1 - CHI (4 loại)
            -- ============================
            SET i = 1;
            WHILE i <= 4 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Chi','Food',FLOOR(50 + RAND()*300),'Meal',
                        CONCAT(y,'-',m,'-',d,' 12:00:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 12:00:00'));
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Chi','Transport',FLOOR(20 + RAND()*100),'Transport',
                        CONCAT(y,'-',m,'-',d,' 08:00:00'),'Cash',
                        CONCAT(y,'-',m,'-',d,' 08:00:00'));
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Chi','Entertainment',FLOOR(50 + RAND()*300),'Movie/Leisure',
                        CONCAT(y,'-',m,'-',d,' 20:00:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 20:00:00'));
                    WHEN 4 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (1,'Chi','Shopping',FLOOR(100 + RAND()*500),'Shopping',
                        CONCAT(y,'-',m,'-',d,' 18:00:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 18:00:00'));
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- ============================
            -- USER 2 - THU (3 loại)
            -- ============================
            SET i = 1;
            WHILE i <= 3 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Thu','Salary',FLOOR(4000 + RAND()*3500),'Monthly salary',
                        CONCAT(y,'-',m,'-',d,' 09:30:00'),'Bank Transfer',
                        CONCAT(y,'-',m,'-',d,' 09:30:00'));
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Thu','Investments',FLOOR(300 + RAND()*1000),'Investment income',
                        CONCAT(y,'-',m,'-',d,' 15:00:00'),'Bank Transfer',
                        CONCAT(y,'-',m,'-',d,' 15:00:00'));
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Thu','Gift',FLOOR(100 + RAND()*500),'Received gift',
                        CONCAT(y,'-',m,'-',d,' 11:00:00'),'Cash',
                        CONCAT(y,'-',m,'-',d,' 11:00:00'));
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- ============================
            -- USER 2 - CHI (4 loại)
            -- ============================
            SET i = 1;
            WHILE i <= 4 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Chi','Food',FLOOR(60 + RAND()*350),'Meal',
                        CONCAT(y,'-',m,'-',d,' 12:30:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 12:30:00'));
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Chi','Transport',FLOOR(30 + RAND()*120),'Transport',
                        CONCAT(y,'-',m,'-',d,' 08:30:00'),'Cash',
                        CONCAT(y,'-',m,'-',d,' 08:30:00'));
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Chi','Shopping',FLOOR(150 + RAND()*600),'Shopping',
                        CONCAT(y,'-',m,'-',d,' 19:00:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 19:00:00'));
                    WHEN 4 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES (2,'Chi','Health',FLOOR(50 + RAND()*400),'Health expenses',
                        CONCAT(y,'-',m,'-',d,' 10:00:00'),'Card',
                        CONCAT(y,'-',m,'-',d,' 10:00:00'));
                END CASE;
                SET i = i + 1;
            END WHILE;

            SET m = m + 1;
        END WHILE;

        SET y = y + 1;
    END WHILE;

END$$
DELIMITER ;

CALL insert_diverse_transactions();


-- xóa các giao dịch từ sau 0h ngày hôm nay để chức năng đặt mục tiêu chạy đúng
DELETE FROM transactions
WHERE created_at > CURDATE();