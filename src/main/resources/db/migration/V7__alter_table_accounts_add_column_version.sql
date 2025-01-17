ALTER TABLE accounts
    ADD COLUMN version INT DEFAULT 0;

UPDATE accounts
SET version = 0;

ALTER TABLE accounts
    MODIFY COLUMN version INT NOT NULL;
