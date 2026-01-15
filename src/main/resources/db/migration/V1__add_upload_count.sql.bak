-- Add upload_count column to student_answers table
ALTER TABLE student_answers 
ADD COLUMN IF NOT EXISTS upload_count INTEGER DEFAULT 0;

-- Set existing records to 1 (assume they uploaded once if they have an image)
UPDATE student_answers 
SET upload_count = 1 
WHERE answer_image_url IS NOT NULL AND upload_count = 0;

-- Add comment for documentation
COMMENT ON COLUMN student_answers.upload_count IS 'Number of times student has uploaded an answer image for this question (max 2)';
