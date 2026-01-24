-- Add origin_custom_bundle_id to student_paper_attempts
ALTER TABLE student_paper_attempts
ADD COLUMN origin_custom_bundle_id BIGINT;

-- Add foreign key constraint
ALTER TABLE student_paper_attempts
ADD CONSTRAINT fk_spa_custom_bundle
FOREIGN KEY (origin_custom_bundle_id)
REFERENCES custom_bundles (id)
ON DELETE SET NULL;

-- Create index for performance
CREATE INDEX idx_spa_custom_bundle_id ON student_paper_attempts(origin_custom_bundle_id);
