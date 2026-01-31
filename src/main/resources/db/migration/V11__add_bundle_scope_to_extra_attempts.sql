-- Add origin_bundle_id column to extra_attempt_purchases for bundle-scoped attempt purchases
ALTER TABLE extra_attempt_purchases 
ADD COLUMN IF NOT EXISTS origin_bundle_id BIGINT;

-- Add foreign key constraint
ALTER TABLE extra_attempt_purchases 
ADD CONSTRAINT fk_extra_attempt_purchases_origin_bundle 
FOREIGN KEY (origin_bundle_id) REFERENCES paper_bundles(id) ON DELETE SET NULL;

-- Add index for bundle-scoped queries
CREATE INDEX IF NOT EXISTS idx_extra_attempt_purchases_bundle 
ON extra_attempt_purchases(user_id, paper_id, origin_bundle_id);
