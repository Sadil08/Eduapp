package com.eduapp.backend.repository;

import com.eduapp.backend.model.SchoolStudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SchoolStudentAnswerRepository extends JpaRepository<SchoolStudentAnswer, Long> {

    List<SchoolStudentAnswer> findByAttemptId(Long attemptId);

    Optional<SchoolStudentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    // Bounded by cohort size × questions; used for per-question cohort analysis.
    List<SchoolStudentAnswer> findByAttemptIdIn(Collection<Long> attemptIds);
}
