-- Add wallet and referral fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS wallet_balance DECIMAL(19, 2) DEFAULT 0.00;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_code VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_referred_by') THEN
        ALTER TABLE users ADD CONSTRAINT fk_referred_by FOREIGN KEY (referred_by_id) REFERENCES users(id);
    END IF;
END $$;

-- Create wallet_transactions table
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL, -- TOP_UP, DEBIT, REFERRAL_CREDIT
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create app_settings table
CREATE TABLE IF NOT EXISTS app_settings (
    settings_key VARCHAR(255) PRIMARY KEY,
    settings_value VARCHAR(255) NOT NULL
);

-- Initialize default referral percentage (0.5%)
INSERT INTO app_settings (settings_key, settings_value) 
VALUES ('referral_percentage', '0.5')
ON CONFLICT (settings_key) DO NOTHING;
