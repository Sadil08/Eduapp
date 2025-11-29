-- Sample Data for EduApp Database
-- Note: Passwords are BCrypt hashed for 'password'
-- $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi = 'password'

-- Insert Admin User
INSERT INTO users (email, password, username, role, created_at, updated_at) VALUES
('admin@eduapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin', 'ADMIN', NOW(), NOW());

-- Insert Student User
INSERT INTO users (email, password, username, role, created_at, updated_at) VALUES
('student@eduapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student', 'STUDENT', NOW(), NOW());

-- Insert Subjects
INSERT INTO subjects (name, description, created_by, created_at) VALUES
('Mathematics', 'Advanced Mathematics including Algebra, Calculus, and Geometry', 1, NOW()),
('Physics', 'Physics covering Mechanics, Thermodynamics, and Electromagnetism', 1, NOW()),
('Chemistry', 'Chemistry including Organic, Inorganic, and Physical Chemistry', 1, NOW()),
('Biology', 'Biology covering Cell Biology, Genetics, and Ecology', 1, NOW());

-- Insert Lessons
INSERT INTO lessons (name, description, subject_id, created_by, created_at) VALUES
('Algebra Fundamentals', 'Basic algebraic operations and equations', 1, 1, NOW()),
('Calculus Basics', 'Introduction to differential and integral calculus', 1, 1, NOW()),
('Mechanics', 'Classical mechanics and motion', 2, 1, NOW()),
('Thermodynamics', 'Laws of thermodynamics and heat transfer', 2, 1, NOW()),
('Organic Chemistry', 'Carbon compounds and reactions', 3, 1, NOW()),
('Cell Biology', 'Structure and function of cells', 4, 1, NOW());

-- Insert Paper Bundles
INSERT INTO paper_bundles (name, description, price, type, exam_type, subject_id, lesson_id, is_past_paper, created_by, created_at) VALUES
('Algebra Practice Set', 'Comprehensive algebra practice questions', 9.99, 'MCQ', 'Mid-term', 1, 1, false, 1, NOW()),
('Calculus Mock Exam', 'Full calculus mock examination', 14.99, 'MIXED', 'Final', 1, 2, true, 1, NOW()),
('Physics Mechanics Bundle', 'Mechanics theory and problems', 12.99, 'ESSAY', 'Mid-term', 2, 3, false, 1, NOW()),
('Thermodynamics Papers', 'Thermodynamics past papers', 11.99, 'MCQ', 'Final', 2, 4, true, 1, NOW()),
('Organic Chemistry Lab', 'Organic chemistry practical questions', 13.99, 'MIXED', 'Practical', 3, 5, false, 1, NOW()),
('Biology Cell Quiz', 'Cell biology multiple choice questions', 8.99, 'MCQ', 'Quiz', 4, 6, false, 1, NOW());

-- Insert Papers
INSERT INTO papers (name, description, bundle_id, type, max_free_attempts, created_by, created_at) VALUES
('Algebra Basics MCQ', 'Multiple choice questions on basic algebra', 1, 'MCQ', 2, 1, NOW()),
('Advanced Algebra Problems', 'Complex algebraic equations and word problems', 1, 'MIXED', 2, 1, NOW()),
('Calculus Derivatives', 'Questions on differentiation and derivatives', 2, 'MIXED', 3, 1, NOW()),
('Integral Calculus', 'Integration problems and applications', 2, 'ESSAY', 2, 1, NOW()),
('Newton Laws', 'Problems based on Newton''s laws of motion', 3, 'ESSAY', 2, 1, NOW()),
('Heat Transfer', 'Thermodynamics heat transfer problems', 4, 'MCQ', 2, 1, NOW()),
('Organic Reactions', 'Organic chemistry reaction mechanisms', 5, 'MIXED', 3, 1, NOW()),
('Cell Structure', 'Cell biology structure and function', 6, 'MCQ', 2, 1, NOW());

-- Insert Questions
INSERT INTO questions (paper_id, text, type, correct_answer_text, created_by, created_at) VALUES
(1, 'Solve for x: 2x + 3 = 7', 'MCQ', '2', 1, NOW()),
(1, 'What is the slope of the line y = 3x + 2?', 'MCQ', '3', 1, NOW()),
(2, 'Solve the system of equations: 2x + y = 5 and x - y = 1', 'ESSAY', 'x = 2, y = 1', 1, NOW()),
(3, 'Find the derivative of f(x) = x^2 + 3x + 1', 'MCQ', '2x + 3', 1, NOW()),
(4, 'Evaluate the integral: ∫(2x + 1)dx', 'ESSAY', 'x^2 + x + C', 1, NOW()),
(5, 'A 5kg object is moving at 10m/s. Calculate its kinetic energy.', 'MCQ', '250 J', 1, NOW()),
(6, 'What is the first law of thermodynamics?', 'ESSAY', 'Energy cannot be created or destroyed, only transformed', 1, NOW()),
(7, 'What is the product of CH3-CH2-OH + HCl?', 'MCQ', 'CH3-CH2-Cl', 1, NOW()),
(8, 'What is the function of mitochondria in a cell?', 'MCQ', 'Energy production', 1, NOW());

-- Insert Question Options (for MCQ questions)
INSERT INTO question_options (key_label, text, is_correct, order_index, question_id) VALUES
('A', '1', false, 1, 1),
('B', '2', true, 2, 1),
('C', '3', false, 3, 1),
('D', '4', false, 4, 1),
('A', '1', false, 1, 2),
('B', '2', false, 2, 2),
('C', '3', true, 3, 2),
('D', '4', false, 4, 2),
('A', '2x', false, 1, 4),
('B', 'x^2', false, 2, 4),
('C', '2x + 3', true, 3, 4),
('D', '3x^2', false, 4, 4),
('A', '100 J', false, 1, 6),
('B', '150 J', false, 2, 6),
('C', '200 J', false, 3, 6),
('D', '250 J', true, 4, 6),
('A', 'CH3-CH2-OH', false, 1, 8),
('B', 'CH3-CH2-Cl', true, 2, 8),
('C', 'CH3-CH3', false, 3, 8),
('D', 'CH2-CH2-OH', false, 4, 8),
('A', 'Protein synthesis', false, 1, 9),
('B', 'Energy production', true, 2, 9),
('C', 'Waste removal', false, 3, 9),
('D', 'Cell division', false, 4, 9);

-- Insert Student Bundle Access (give student access to some bundles)
INSERT INTO student_bundle_accesses (student_id, bundle_id, purchased_at, expires_at) VALUES
(2, 1, NOW(), NOW() + INTERVAL '1 year'),
(2, 6, NOW(), NOW() + INTERVAL '1 year');

-- Insert Student Paper Attempts
INSERT INTO student_paper_attempts (student_id, paper_id, attempt_number, started_at, completed_at, status, time_taken_minutes) VALUES
(2, 1, 1, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '10 minutes', 'SUBMITTED', 20),
(2, 8, 1, NOW() - INTERVAL '15 minutes', NULL, 'IN_PROGRESS', NULL);

-- Insert Student Answers
INSERT INTO student_answers (attempt_id, question_id, answer_text, selected_option_id, submitted_at) VALUES
(1, 1, NULL, 2, NOW() - INTERVAL '25 minutes'), -- Correct answer
(1, 2, NULL, 3, NOW() - INTERVAL '20 minutes'), -- Correct answer
(2, 9, NULL, 2, NOW() - INTERVAL '10 minutes'); -- Correct answer

-- Insert Progress
INSERT INTO progresses (student_id, subject_id, lesson_id, completion_percentage, last_accessed) VALUES
(2, 1, 1, 75.0, NOW() - INTERVAL '1 day'),
(2, 4, 6, 50.0, NOW() - INTERVAL '2 hours');

-- Insert Leaderboard Entries
INSERT INTO leaderboard_entries (user_id, subject_id, score, paper_id, is_anonymous, created_at) VALUES
(2, 1, 85, 1, false, NOW() - INTERVAL '1 day'),
(2, 4, 78, 6, false, NOW() - INTERVAL '2 hours');

-- Insert Cart Items
INSERT INTO carts (user_id) VALUES (2);

-- Insert cart bundles (many-to-many relationship)
INSERT INTO cart_bundles (cart_id, bundle_id) VALUES
(1, 2),
(1, 3);

-- Insert Notifications
INSERT INTO notifications (user_id, message, type, is_read, sent_at) VALUES
(2, 'Welcome! You have successfully registered.', 'INFO', true, NOW()),
(2, 'You have purchased Algebra Practice Set.', 'SUCCESS', true, NOW() - INTERVAL '1 day'),
(2, 'You have an incomplete quiz in Cell Biology.', 'REMINDER', false, NOW() - INTERVAL '2 hours');

-- Insert AI Analysis (sample analysis for completed attempt)
INSERT INTO ai_analyses (answer_id, feedback, lessons_to_review, marks, created_at) VALUES
(1, 'Correct answer. Good understanding of basic algebra.', 'Keep practicing similar problems.', 100, NOW()),
(2, 'Correct answer. Well done on slope calculation.', 'Try more complex linear equations.', 100, NOW());

-- Insert Overall Paper Analysis
INSERT INTO overall_paper_analyses (attempt_id, created_at, lessons_lacking, overall_feedback, total_marks) VALUES
(1, NOW(), 'Could improve on word problems', 'Strong in basic algebra concepts. Practice more application-based questions', 95);