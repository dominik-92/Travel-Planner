-- Add account-management columns to users.
-- Idempotent: handles both a pre-feature DB (columns absent) and a partially
-- migrated DB (nullable columns added by Hibernate ddl-auto=update without
-- the NOT NULL constraint).

ALTER TABLE users ADD COLUMN IF NOT EXISTS password_version integer NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS currency varchar(255) DEFAULT 'PLN';
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token varchar(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_token_expires_at varchar(255);

-- Normalize the columns added by Hibernate's earlier ddl-auto=update runs so
-- they match the entity mapping (nullable=false on password_version, default 0).
UPDATE users SET password_version = 0 WHERE password_version IS NULL;

ALTER TABLE users ALTER COLUMN password_version SET DEFAULT 0;
ALTER TABLE users ALTER COLUMN password_version SET NOT NULL;
