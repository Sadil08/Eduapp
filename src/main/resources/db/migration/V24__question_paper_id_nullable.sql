-- A question belongs to EITHER a consumer Paper (paper_id) OR a school-tier SchoolPaper
-- (school_paper_id). School questions legitimately have a NULL paper_id, but the legacy
-- schema kept questions.paper_id NOT NULL — so inserting a school question failed with
-- "null value in column paper_id violates not-null constraint". ddl-auto=update does not
-- relax an existing NOT NULL, so drop it explicitly here.
ALTER TABLE questions ALTER COLUMN paper_id DROP NOT NULL;
