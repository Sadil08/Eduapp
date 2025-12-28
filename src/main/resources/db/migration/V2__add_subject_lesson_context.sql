-- Add subject_id column to papers table
ALTER TABLE papers
ADD COLUMN subject_id BIGINT,
ADD CONSTRAINT fk_papers_subject
    FOREIGN KEY (subject_id)
    REFERENCES subjects(id)
    ON DELETE SET NULL;

-- Add lesson_id column to questions table
ALTER TABLE questions
ADD COLUMN lesson_id BIGINT,
ADD CONSTRAINT fk_questions_lesson
    FOREIGN KEY (lesson_id)
    REFERENCES lessons(id)
    ON DELETE SET NULL;

-- Add indexes for better query performance
CREATE INDEX idx_papers_subject_id ON papers(subject_id);
CREATE INDEX idx_questions_lesson_id ON questions(lesson_id);
