-- Sample data for testing Phase 2 image handling functionality

-- Add Mathematics subject if it doesn't exist
INSERT INTO subjects (name, description, created_at, updated_at) 
VALUES ('Mathematics', 'Mathematics courses', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Add Physics subject if it doesn't exist
INSERT INTO subjects (name, description, created_at, updated_at)
VALUES ('Physics', 'Physics courses', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Sample questions with different configurations

-- Question 1: Text question + Text model answer + Allow typed/image answer
INSERT INTO questions (paper_id, text, type, correct_answer_text, marks, requires_image_display, allow_image_answer, answer_type_hint, created_at)
VALUES (1, 'Solve the quadratic equation: x^2 - 5x + 6 = 0', 'ESSAY', 'x = 2 or x = 3', 10, FALSE, TRUE, 'essay', NOW())
ON CONFLICT DO NOTHING;

-- Question 2: Text question + Restrict to typed answers only (short answer)
INSERT INTO questions (paper_id, text, type, correct_answer_text, marks, requires_image_display, allow_image_answer, answer_type_hint, created_at)
VALUES (1, 'What is the derivative of x^2?', 'ESSAY', '2x', 5, FALSE, FALSE, 'short', NOW())
ON CONFLICT DO NOTHING;

-- Question 3: Image question (requires display) + Allow image answers (diagram type)
-- Note: In production, you would upload an actual image and get the imageUrl
INSERT INTO questions (paper_id, text, type, marks, requires_image_display, allow_image_answer, answer_type_hint, image_url, extracted_text, created_at)
VALUES (
    1, 
    'Draw and label a free body diagram for the following scenario', 
    'ESSAY', 
    15, 
    TRUE, 
    TRUE, 
    'diagram',
    '/api/files/questions/sample-diagram.png', -- Placeholder
    'Draw and label a free body diagram for the following scenario: A block on an inclined plane',
    NOW()
)
ON CONFLICT DO NOTHING;

-- Add sample model answers to question_model_answers table
-- For question 1 (quadratic equation)
INSERT INTO question_model_answers (question_id, answer_text, created_at)
SELECT id, 'Using the quadratic formula: x = (5 ± √(25-24))/2 = (5 ± 1)/2. Therefore x = 3 or x = 2', NOW()
FROM questions 
WHERE text LIKE 'Solve the quadratic equation%'
LIMIT 1
ON CONFLICT DO NOTHING;

-- For question 2 (derivative)
INSERT INTO question_model_answers (question_id, answer_text, created_at)
SELECT id, 'Using the power rule: d/dx(x^2) = 2x^(2-1) = 2x', NOW()
FROM questions 
WHERE text LIKE 'What is the derivative%'
LIMIT 1
ON CONFLICT DO NOTHING;

COMMENT ON TABLE question_model_answers IS 'Sample model answers added for testing';
