-- Step 1: Delete analytics associated with duplicate answers 
-- (answers that will be deleted in Step 2: those with smaller IDs for same attempt+question)
DELETE FROM ai_analyses
WHERE answer_id IN (
    SELECT a.id
    FROM student_answers a
    JOIN student_answers b
    ON a.attempt_id = b.attempt_id
    AND a.question_id = b.question_id
    AND a.id < b.id
);

-- Step 2: Delete duplicate answers (keeping the one with max ID)
DELETE FROM student_answers a
USING student_answers b
WHERE a.attempt_id = b.attempt_id
AND a.question_id = b.question_id
AND a.id < b.id;

-- Step 3: Add unique constraint to prevent future duplicates
ALTER TABLE student_answers
ADD CONSTRAINT unique_attempt_question UNIQUE (attempt_id, question_id);
