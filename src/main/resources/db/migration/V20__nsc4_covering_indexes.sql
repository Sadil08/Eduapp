-- SCALE-4 / N-SC4: covering indexes for hot lookups that lacked one.
--
-- Most hot-path indexes already exist (V9/V13/V14: answers.attempt_id, attempts.student_id,
-- attempts.paper_id, leaderboard, extraction-tracking, school_paper_attempts' unique
-- (school_paper_id, student_id), etc.). These two close the remaining gaps:

-- Attempt-limit counting: PaperService.checkAttemptLimit calls countByStudentIdAndPaperId on
-- every paper view/attempt. The single-column indexes don't serve this composite predicate well.
CREATE INDEX IF NOT EXISTS idx_attempts_student_paper
    ON student_paper_attempts (student_id, paper_id);

-- Password-reset lookups by user (forgotPassword / resetPassword find the token by user).
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user
    ON password_reset_tokens (user_id);
