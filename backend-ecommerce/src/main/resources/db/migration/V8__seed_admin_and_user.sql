-- Insert Admin user
-- Password is '123456' (BCrypt hash: $2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG)
INSERT INTO users (email, password, full_name, phone_number, address, role_id, is_active)
VALUES ('admin@gmail.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'System Admin', '0123456789', 'Hanoi', 2, true);

-- Insert regular User
-- Password is '123456'
INSERT INTO users (email, password, full_name, phone_number, address, role_id, is_active)
VALUES ('user@gmail.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Normal User', '0987654321', 'HCM', 1, true);
