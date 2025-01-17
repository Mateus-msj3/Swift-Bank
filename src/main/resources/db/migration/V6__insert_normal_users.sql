INSERT INTO users (name, username, password, enabled)
VALUES ('One user', 'one_user@email.com', '$2a$10$Adq2mH30qfRAFNH3qqbBVuMLz7KKU604cQQMQKgYCyP7pjSbMwmPS', true),
       ('Second user', 'second_user@email.com', '$2a$10$Adq2mH30qfRAFNH3qqbBVuMLz7KKU604cQQMQKgYCyP7pjSbMwmPS', true);

INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'one_user@email.com'),
        (SELECT id FROM roles WHERE name = 'USER')),
       ((SELECT id FROM users WHERE username = 'second_user@email.com'),
        (SELECT id FROM roles WHERE name = 'USER'));
