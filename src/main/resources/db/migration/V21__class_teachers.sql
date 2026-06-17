-- BT-4.1: a school class can have many teachers. Join table + backfill the existing single
-- teacher_id so current classes keep their teacher.
CREATE TABLE IF NOT EXISTS class_teachers (
    class_id   BIGINT NOT NULL REFERENCES school_classes (id) ON DELETE CASCADE,
    teacher_id BIGINT NOT NULL,
    PRIMARY KEY (class_id, teacher_id)
);

CREATE INDEX IF NOT EXISTS idx_class_teachers_teacher ON class_teachers (teacher_id);

INSERT INTO class_teachers (class_id, teacher_id)
SELECT id, teacher_id FROM school_classes WHERE teacher_id IS NOT NULL
ON CONFLICT DO NOTHING;
