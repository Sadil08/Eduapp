-- Clean up existing duplicates before creating index
-- Mark older IN_PROGRESS attempts as ABANDONED, keeping only the latest one
UPDATE student_paper_attempts spa
SET status = 'ABANDONED'
WHERE status = 'IN_PROGRESS'
  AND EXISTS (
    SELECT 1 FROM student_paper_attempts spa2
    WHERE spa2.student_id = spa.student_id
      AND spa2.paper_id = spa.paper_id
      AND (
          (spa2.origin_bundle_id IS NOT NULL AND spa2.origin_bundle_id = spa.origin_bundle_id) 
          OR (spa2.origin_bundle_id IS NULL AND spa.origin_bundle_id IS NULL)
      )
      AND (
          (spa2.origin_custom_bundle_id IS NOT NULL AND spa2.origin_custom_bundle_id = spa.origin_custom_bundle_id) 
          OR (spa2.origin_custom_bundle_id IS NULL AND spa.origin_custom_bundle_id IS NULL)
      )
      AND spa2.status = 'IN_PROGRESS'
      AND spa2.id > spa.id -- Keep the one with larger ID (latest)
  );

-- Create partial unique index to prevent multiple IN_PROGRESS attempts for the same question/paper/bundle context
-- For Custom Scoped attempts
CREATE UNIQUE INDEX idx_uni_student_paper_custom_attempt_in_progress
ON student_paper_attempts (student_id, paper_id, origin_custom_bundle_id)
WHERE status = 'IN_PROGRESS' AND origin_custom_bundle_id IS NOT NULL;

-- For Bundle Scoped attempts
CREATE UNIQUE INDEX idx_uni_student_paper_std_attempt_in_progress
ON student_paper_attempts (student_id, paper_id, origin_bundle_id)
WHERE status = 'IN_PROGRESS' AND origin_bundle_id IS NOT NULL;
