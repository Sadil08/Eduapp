package com.eduapp.backend.dto;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;

import java.util.List;

/**
 * A school-paper question as shown to a STUDENT while attempting. Deliberately OMITS the
 * marking scheme: no {@code correctAnswerText}, no {@code modelAnswerImageUrl}, and the MCQ
 * options carry NO {@code isCorrect} flag. The question's own {@code imageUrl} IS included.
 */
public record StudentQuestionDto(Long id, String text, String type, Integer marks, String imageUrl,
        List<StudentMcqOptionDto> options) {

    public static StudentQuestionDto from(Question q, List<QuestionOption> options) {
        return new StudentQuestionDto(q.getId(), q.getText(),
                q.getType() != null ? q.getType().name() : null, q.getMarks(), q.getImageUrl(),
                options == null ? List.of() : options.stream().map(StudentMcqOptionDto::from).toList());
    }
}
