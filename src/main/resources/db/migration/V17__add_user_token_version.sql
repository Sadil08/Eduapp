-- N-S5: per-user JWT token version. Incremented on password reset (and future "log out
-- everywhere") so all previously-issued tokens are rejected by the auth filter.
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0;
