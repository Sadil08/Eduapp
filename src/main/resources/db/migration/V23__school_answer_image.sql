-- Image-answer support for school attempts (handwritten responses): store the uploaded image
-- URL and its AI-extracted text alongside the answer.
ALTER TABLE school_student_answers ADD COLUMN IF NOT EXISTS image_url TEXT;
ALTER TABLE school_student_answers ADD COLUMN IF NOT EXISTS extracted_text TEXT;
