CREATE TABLE transactions
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    account_id       BIGINT                NOT NULL,
    amount           DECIMAL               NOT NULL,
    transaction_type VARCHAR(255)          NOT NULL,
    created_at       datetime              NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES accounts (id);