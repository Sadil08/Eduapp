-- Add draft flag to student answers for in-progress save
ALTER TABLE student_answers 
ADD COLUMN IF NOT EXISTS is_draft BOOLEAN NOT NULL DEFAULT true;

-- Add index for efficient draft queries
CREATE INDEX IF NOT EXISTS idx_answer_draft_attempt 
ON student_answers(attempt_id, is_draft);

-- Add comment
COMMENT ON COLUMN student_answers.is_draft IS 'True for draft/autosaved answers, false for final submission';
