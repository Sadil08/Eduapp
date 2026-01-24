-- Create custom_bundles table for student-created bundles
CREATE TABLE IF NOT EXISTS custom_bundles (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    purchased_at TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_custom_bundle_status CHECK (status IN ('CREATED', 'PURCHASED', 'APPROVED'))
);

-- Create join table for custom bundles and papers (many-to-many)
CREATE TABLE IF NOT EXISTS custom_bundle_papers (
    custom_bundle_id BIGINT NOT NULL REFERENCES custom_bundles(id) ON DELETE CASCADE,
    paper_id BIGINT NOT NULL REFERENCES papers(id) ON DELETE CASCADE,
    PRIMARY KEY (custom_bundle_id, paper_id)
);

-- Create system_config table for admin-configurable settings
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert default custom bundle paper price
INSERT INTO system_config (config_key, config_value, description)
VALUES ('custom_bundle_paper_price', '2.00', 'Price per paper when creating custom bundles')
ON CONFLICT (config_key) DO NOTHING;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_custom_bundles_creator ON custom_bundles(creator_id);
CREATE INDEX IF NOT EXISTS idx_custom_bundles_status ON custom_bundles(status);
CREATE INDEX IF NOT EXISTS idx_custom_bundle_papers_bundle ON custom_bundle_papers(custom_bundle_id);
CREATE INDEX IF NOT EXISTS idx_custom_bundle_papers_paper ON custom_bundle_papers(paper_id);
