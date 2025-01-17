INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('USER');

INSERT INTO users (name, username, password, enabled)
VALUES ('Super Admin', 'admin@email.com', '$2a$10$YeEZ4Bjulki1Ka8.Ir/jOufSeSKlS8jMs83eo3SmxZoVI7scT38Ji', true), -- Senha: admin
       ('Normal User', 'user@email.com', '$2a$10$Adq2mH30qfRAFNH3qqbBVuMLz7KKU604cQQMQKgYCyP7pjSbMwmPS', true);-- Senha: user

INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'admin@email.com'),
        (SELECT id FROM roles WHERE name = 'ADMIN')),
       ((SELECT id FROM users WHERE username = 'user@email.com'),
        (SELECT id FROM roles WHERE name = 'USER'));
