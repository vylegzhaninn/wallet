ALTER TABLE transactions
    ADD COLUMN user_id_from BIGINT,
    ADD COLUMN user_id_to BIGINT;

UPDATE transactions t
    SET user_id_from = a.user_id
    FROM accounts a
    WHERE t.from_account_id = a.id;

UPDATE transactions t
    SET user_id_to = a.user_id
    FROM accounts a
    WHERE t.to_account_id = a.id;

ALTER TABLE transactions
    ALTER COLUMN user_id_from SET NOT NULL,
    ALTER COLUMN user_id_to SET NOT NULL;
