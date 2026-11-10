package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.QuestionOption;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    /** Batch-load options for many questions at once (avoids N+1 on the attempt/teacher views). */
    List<QuestionOption> findByQuestionIdInOrderByOrderIndexAsc(List<Long> questionIds);
}
