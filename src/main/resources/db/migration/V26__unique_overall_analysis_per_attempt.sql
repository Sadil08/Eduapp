-- NFR-IDEM-1: exactly one overall analysis per attempt — a DB backstop behind the application-
-- level upsert in MarkingPersistenceService.persistMarking. Guards against duplicate analyses if
-- live-async marking, the sweeper, and a manual retry ever race for the same attempt.

-- Remove any pre-existing duplicates first (keep the most recent row by id), so the constraint can
-- be added cleanly on databases that ran the old non-upsert marking code.
DELETE FROM overall_paper_analyses a
USING overall_paper_analyses b
WHERE a.attempt_id = b.attempt_id
  AND a.id < b.id;

ALTER TABLE overall_paper_analyses
  ADD CONSTRAINT uq_overall_analysis_attempt UNIQUE (attempt_id);
