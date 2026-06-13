-- Migration for extraction tracking, user location, and analysis resubmission features
-- Phase 3-5 implementation

-- Add user location and login tracking fields
ALTER TABLE users
ADD COLUMN IF NOT EXISTS country VARCHAR(2),
ADD COLUMN IF NOT EXISTS registration_ip VARCHAR(45),
ADD COLUMN IF NOT EXISTS last_login_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_login_ip VARCHAR(45);

-- Create extraction tracking table
CREATE TABLE IF NOT EXISTS question_extraction_tracking (
    id BIGSERIAL PRIMARY KEY,
    student_paper_attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    extraction_count INT NOT NULL DEFAULT 0,
    max_extractions INT NOT NULL DEFAULT 2,
    last_extraction_time TIMESTAMP,
    CONSTRAINT fk_extraction_attempt 
        FOREIGN KEY (student_paper_attempt_id) 
        REFERENCES student_paper_attempts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_extraction_question 
        FOREIGN KEY (question_id) 
        REFERENCES questions(id) 
        ON DELETE CASCADE,
    CONSTRAINT uq_attempt_question 
        UNIQUE (student_paper_attempt_id, question_id)
);

-- Add fields to student_paper_attempts for analysis resubmission and elapsed time
-- Step 1: Add columns as nullable first
ALTER TABLE student_paper_attempts
ADD COLUMN IF NOT EXISTS elapsed_time_seconds INT,
ADD COLUMN IF NOT EXISTS analysis_completed BOOLEAN,
ADD COLUMN IF NOT EXISTS analysis_attempted BOOLEAN,
ADD COLUMN IF NOT EXISTS analysis_error TEXT,
ADD COLUMN IF NOT EXISTS submission_count INT,
ADD COLUMN IF NOT EXISTS last_submission_time TIMESTAMP;

-- Step 2: Update existing rows with default values
UPDATE student_paper_attempts
SET 
    analysis_completed = COALESCE(analysis_completed, FALSE),
    analysis_attempted = COALESCE(analysis_attempted, FALSE),
    submission_count = COALESCE(submission_count, 0)
WHERE analysis_completed IS NULL OR analysis_attempted IS NULL OR submission_count IS NULL;

-- Step 3: Add NOT NULL constraints after data is populated
ALTER TABLE student_paper_attempts
ALTER COLUMN analysis_completed SET NOT NULL,
ALTER COLUMN analysis_completed SET DEFAULT FALSE,
ALTER COLUMN analysis_attempted SET NOT NULL,
ALTER COLUMN analysis_attempted SET DEFAULT FALSE,
ALTER COLUMN submission_count SET NOT NULL,
ALTER COLUMN submission_count SET DEFAULT 0;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_extraction_tracking_attempt 
    ON question_extraction_tracking(student_paper_attempt_id);
CREATE INDEX IF NOT EXISTS idx_extraction_tracking_question 
    ON question_extraction_tracking(question_id);
CREATE INDEX IF NOT EXISTS idx_users_country 
    ON users(country);
CREATE INDEX IF NOT EXISTS idx_users_last_login 
    ON users(last_login_time);

-- Comments for documentation
COMMENT ON TABLE question_extraction_tracking IS 'Tracks AI extraction attempts per question per attempt to enforce limits and prevent API abuse';
COMMENT ON COLUMN question_extraction_tracking.extraction_count IS 'Number of times extraction has been performed for this question in this attempt';
COMMENT ON COLUMN question_extraction_tracking.max_extractions IS 'Maximum allowed extractions (default: 2)';
COMMENT ON COLUMN student_paper_attempts.analysis_completed IS 'True if AI analysis succeeded, false if failed or not attempted';
COMMENT ON COLUMN student_paper_attempts.analysis_attempted IS 'True if analysis was attempted (success or failure)';
COMMENT ON COLUMN student_paper_attempts.analysis_error IS 'Error message if analysis failed';
COMMENT ON COLUMN student_paper_attempts.submission_count IS 'Number of times this attempt has been submitted (allows resubmission on analysis failure)';
