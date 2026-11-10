-- MCQ support for school papers: record which option a student selected so marking is by
-- option identity (robust to later option-text edits), not a free-text compare.
ALTER TABLE school_student_answers ADD COLUMN IF NOT EXISTS selected_option_id BIGINT;
