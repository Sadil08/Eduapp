-- Phase 2: Complete Image Handling Schema Updates
-- Run this after the application has been stopped

-- 1. Add new columns to questions table
ALTER TABLE questions 
ADD COLUMN IF NOT EXISTS extracted_text TEXT,
ADD COLUMN IF NOT EXISTS requires_image_display BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS allow_image_answer BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS answer_type_hint VARCHAR(20) DEFAULT 'essay';

-- 2. Create question_model_answers table
CREATE TABLE IF NOT EXISTS question_model_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_text TEXT,
    image_url VARCHAR(500),
    extracted_text TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_qma_question ON question_model_answers(question_id);

-- 3. Migrate existing correctAnswerText to question_model_answers
INSERT INTO question_model_answers (question_id, answer_text, created_at)
SELECT id, correct_answer_text, created_at
FROM questions
WHERE correct_answer_text IS NOT NULL AND correct_answer_text != '';

-- 4. Add extraction quality tracking
ALTER TABLE questions
ADD COLUMN IF NOT EXISTS extraction_confidence FLOAT;

ALTER TABLE student_answers
ADD COLUMN IF NOT EXISTS extraction_confidence FLOAT;

-- 5. Add subject extraction hints
ALTER TABLE subjects
ADD COLUMN IF NOT EXISTS extraction_hints JSONB;

-- Update with default hints for existing subjects
UPDATE subjects SET extraction_hints = '{"content_type": "equations", "common_symbols": ["∫", "∑", "∂", "√"]}'::jsonb
WHERE name = 'Mathematics' AND extraction_hints IS NULL;

UPDATE subjects SET extraction_hints = '{"content_type": "mixed", "common_symbols": ["→", "⇌", "Δ", "H₂O"]}'::jsonb
WHERE name = 'Chemistry' AND extraction_hints IS NULL;

UPDATE subjects SET extraction_hints = '{"content_type": "formulas", "common_symbols": ["F=ma", "v=d/t", "E=mc²"]}'::jsonb
WHERE name = 'Physics' AND extraction_hints IS NULL;

UPDATE subjects SET extraction_hints = '{"content_type": "diagrams", "common_symbols": []}'::jsonb
WHERE name = 'Biology' AND extraction_hints IS NULL;

-- 6. Add comment for backward compatibility
COMMENT ON COLUMN questions.correct_answer_text IS 'Deprecated: Use question_model_answers table instead';
