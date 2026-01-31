-- Migration V10: Convert Paper-Bundle to Many-to-Many and add attempt scoping
-- This migration:
-- 1. Creates join table for Paper-Bundle Many-to-Many relationship
-- 2. Migrates existing data from papers.bundle_id to join table
-- 3. Adds origin_bundle_id to student_paper_attempts for attempt scoping
-- 4. Removes bundle_id from papers table

-- Step 1: Create the join table for Many-to-Many Paper-Bundle relationship
CREATE TABLE IF NOT EXISTS paper_bundles_papers (
    paper_bundle_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    PRIMARY KEY (paper_bundle_id, paper_id),
    CONSTRAINT fk_paper_bundles_papers_bundle
        FOREIGN KEY (paper_bundle_id)
        REFERENCES paper_bundles(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_paper_bundles_papers_paper
        FOREIGN KEY (paper_id)
        REFERENCES papers(id)
        ON DELETE CASCADE
);

-- Step 2: Migrate existing data - copy all paper-bundle relationships to join table
INSERT INTO paper_bundles_papers (paper_bundle_id, paper_id)
SELECT bundle_id, id FROM papers WHERE bundle_id IS NOT NULL;

-- Step 3: Add origin_bundle_id to student_paper_attempts
-- This field tracks which bundle context an attempt was made in
ALTER TABLE student_paper_attempts 
ADD COLUMN origin_bundle_id BIGINT;

-- Add foreign key constraint
ALTER TABLE student_paper_attempts
ADD CONSTRAINT fk_student_paper_attempts_origin_bundle
    FOREIGN KEY (origin_bundle_id)
    REFERENCES paper_bundles(id)
    ON DELETE SET NULL;

-- Step 4: Migrate existing attempts - set origin_bundle_id based on paper's bundle
-- This assumes existing attempts were made in the context of the paper's original bundle
UPDATE student_paper_attempts spa
SET origin_bundle_id = p.bundle_id
FROM papers p
WHERE spa.paper_id = p.id AND p.bundle_id IS NOT NULL;

-- Step 5: Remove the old bundle_id column from papers
ALTER TABLE papers DROP CONSTRAINT IF EXISTS fk_papers_bundle;
ALTER TABLE papers DROP COLUMN bundle_id;

-- Create indexes for performance
CREATE INDEX idx_paper_bundles_papers_paper ON paper_bundles_papers(paper_id);
CREATE INDEX idx_paper_bundles_papers_bundle ON paper_bundles_papers(paper_bundle_id);
CREATE INDEX idx_student_paper_attempts_origin_bundle ON student_paper_attempts(origin_bundle_id);
