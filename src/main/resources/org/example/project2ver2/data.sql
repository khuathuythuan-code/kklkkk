CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE,
  password VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(50)
);

CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  user_id INT,
  type VARCHAR(10) NOT NULL, -- "Thu" or "Chi"
  CONSTRAINT fk_cat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type VARCHAR(10) NOT NULL, -- "Thu" or "Chi"
  category VARCHAR(100),
  amount FLOAT NOT NULL,
  note TEXT,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    theme VARCHAR(10),
    language VARCHAR(10),
    currency VARCHAR(10),
    limit_amount FLOAT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO settings (user_id, theme, language, currency, limit_amount)
VALUES
(1, 'light', 'vi', 'VND', 5000000),
(2, 'dark', 'en', 'VND', 10000000);
