-- ============================================
-- ANALYTICS PERFORMANCE INDEXES - SIMPLIFIED
-- ============================================
-- This migration creates essential indexes for analytics performance
-- Only creates indexes on tables that are confirmed to exist

-- ============================================
-- USER INDEXES (already partially done in V6)
-- ============================================
CREATE INDEX IF NOT EXISTS idx_users_created_at 
ON users(created_at);

-- ============================================
-- STUDENT PAPER ATTEMPTS INDEXES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_attempts_student 
ON student_paper_attempts(student_id);

CREATE INDEX IF NOT EXISTS idx_attempts_status 
ON student_paper_attempts(status);

CREATE INDEX IF NOT EXISTS idx_attempts_paper 
ON student_paper_attempts(paper_id);

CREATE INDEX IF NOT EXISTS idx_attempts_started 
ON student_paper_attempts(started_at);

CREATE INDEX IF NOT EXISTS idx_attempts_completed 
ON student_paper_attempts(completed_at);

-- ============================================
-- STUDENT ANSWERS INDEXES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_answers_attempt 
ON student_answers(attempt_id);

CREATE INDEX IF NOT EXISTS idx_answers_question 
ON student_answers(question_id);

CREATE INDEX IF NOT EXISTS idx_answers_draft 
ON student_answers(attempt_id, is_draft);

-- ============================================
-- LEADERBOARD INDEXES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_leaderboard_user 
ON leaderboard_entries(user_id);

CREATE INDEX IF NOT EXISTS idx_leaderboard_paper 
ON leaderboard_entries(paper_id);

CREATE INDEX IF NOT EXISTS idx_leaderboard_score 
ON leaderboard_entries(score DESC);

-- ============================================
-- QUESTIONS INDEXES
-- ============================================
CREATE INDEX IF NOT EXISTS idx_questions_paper 
ON questions(paper_id);
