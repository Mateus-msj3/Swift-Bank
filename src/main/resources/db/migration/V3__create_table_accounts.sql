CREATE TABLE accounts
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    owner_name VARCHAR(255)          NULL,
    balance    DECIMAL               NOT NULL,
    created_at datetime              NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);