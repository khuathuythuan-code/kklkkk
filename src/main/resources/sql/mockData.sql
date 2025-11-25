DELIMITER $$

CREATE PROCEDURE insert_diverse_transactions()
BEGIN
    DECLARE y INT;
    DECLARE m INT;
    DECLARE d INT;
    DECLARE amt FLOAT;
    DECLARE i INT;

    -- Các loại Thu và Chi cho mỗi user
    DECLARE thu_user1 VARCHAR(255);
    DECLARE chi_user1 VARCHAR(255);
    DECLARE thu_user2 VARCHAR(255);
    DECLARE chi_user2 VARCHAR(255);

    SET thu_user1 = 'Salary,Freelance,Investment';
    SET chi_user1 = 'Food,Transport,Entertainment,Shopping';
    SET thu_user2 = 'Salary,Investments,Gift';
    SET chi_user2 = 'Food,Transport,Shopping,Health';

    SET y = 2024;
    WHILE y <= 2026 DO
        SET m = 1;
        WHILE m <= 12 DO
            -- User 1 - Thu
            SET i = 1;
            WHILE i <= 3 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Thu','Salary',FLOOR(3000 + RAND()*3000),'Monthly salary',CONCAT(y,'-',m,'-',d,' 09:00:00'),'Cash',NOW());
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Thu','Freelance',FLOOR(500 + RAND()*2000),'Freelance project',CONCAT(y,'-',m,'-',d,' 14:00:00'),'Bank Transfer',NOW());
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Thu','Investment',FLOOR(200 + RAND()*1000),'Investment profit',CONCAT(y,'-',m,'-',d,' 16:00:00'),'Bank Transfer',NOW());
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- User 1 - Chi
            SET i = 1;
            WHILE i <= 4 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Chi','Food',FLOOR(50 + RAND()*300),'Meal',CONCAT(y,'-',m,'-',d,' 12:00:00'),'Card',NOW());
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Chi','Transport',FLOOR(20 + RAND()*100),'Transport',CONCAT(y,'-',m,'-',d,' 08:00:00'),'Cash',NOW());
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Chi','Entertainment',FLOOR(50 + RAND()*300),'Movie/Leisure',CONCAT(y,'-',m,'-',d,' 20:00:00'),'Card',NOW());
                    WHEN 4 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(1,'Chi','Shopping',FLOOR(100 + RAND()*500),'Shopping',CONCAT(y,'-',m,'-',d,' 18:00:00'),'Card',NOW());
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- User 2 - Thu
            SET i = 1;
            WHILE i <= 3 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Thu','Salary',FLOOR(4000 + RAND()*3500),'Monthly salary',CONCAT(y,'-',m,'-',d,' 09:30:00'),'Bank Transfer',NOW());
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Thu','Investments',FLOOR(300 + RAND()*1000),'Investment income',CONCAT(y,'-',m,'-',d,' 15:00:00'),'Bank Transfer',NOW());
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Thu','Gift',FLOOR(100 + RAND()*500),'Received gift',CONCAT(y,'-',m,'-',d,' 11:00:00'),'Cash',NOW());
                END CASE;
                SET i = i + 1;
            END WHILE;

            -- User 2 - Chi
            SET i = 1;
            WHILE i <= 4 DO
                SET d = FLOOR(1 + RAND()*28);
                CASE i
                    WHEN 1 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Chi','Food',FLOOR(60 + RAND()*350),'Meal',CONCAT(y,'-',m,'-',d,' 12:30:00'),'Card',NOW());
                    WHEN 2 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Chi','Transport',FLOOR(30 + RAND()*120),'Transport',CONCAT(y,'-',m,'-',d,' 08:30:00'),'Cash',NOW());
                    WHEN 3 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Chi','Shopping',FLOOR(150 + RAND()*600),'Shopping',CONCAT(y,'-',m,'-',d,' 19:00:00'),'Card',NOW());
                    WHEN 4 THEN INSERT INTO transactions(user_id,type,category,amount,note,created_at,transaction_method,updated_at)
                        VALUES(2,'Chi','Health',FLOOR(50 + RAND()*400),'Health expenses',CONCAT(y,'-',m,'-',d,' 10:00:00'),'Card',NOW());
                END CASE;
                SET i = i + 1;
            END WHILE;

            SET m = m + 1;
        END WHILE;
        SET y = y + 1;
    END WHILE;
END$$

DELIMITER ;

-- Gọi procedure để chèn dữ liệu
CALL insert_diverse_transactions();
