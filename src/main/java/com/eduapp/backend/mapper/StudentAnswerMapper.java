package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.dto.StudentAnswerDto;
import java.util.List;

/**
 * MapStruct mapper for converting StudentAnswer entities to DTOs.
 * Used to transform student answers with AI analysis results for API responses.
 */
@Mapper(componentModel = "spring")
public interface StudentAnswerMapper {

    @Mapping(source = "attempt.id", target = "attemptId")
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.text", target = "questionText")
    @Mapping(source = "question.imageUrl", target = "questionImageUrl")
    @Mapping(source = "question.hideQuestionText", target = "hideQuestionText")
    @Mapping(source = "question.marks", target = "marksAvailable")
    @Mapping(source = "selectedOption.id", target = "selectedOptionId")
    @Mapping(source = "selectedOption.text", target = "selectedOptionText")
    @Mapping(source = "question.correctAnswerText", target = "correctAnswerText")
    @Mapping(source = "question.modelAnswerImageUrl", target = "correctAnswerImageUrl")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "extractedText", target = "extractedText")
    @Mapping(source = "extractionConfidence", target = "extractionConfidence")
    @Mapping(target = "correctOptionId", ignore = true)
    @Mapping(target = "correctOptionText", ignore = true)
    StudentAnswerDto toDto(StudentAnswer entity);

    /**
     * Post-mapping to populate correct option details for MCQ questions.
     * Finds the correct option from the question's options list.
     * Also populates answerText from selectedOption if null (backward
     * compatibility).
     */
    @AfterMapping
    default void populateCorrectOption(@MappingTarget StudentAnswerDto dto, StudentAnswer entity) {
        // Populate answerText from selected option if it's null (for backward
        // compatibility)
        if ((dto.getAnswerText() == null || dto.getAnswerText().isEmpty())
                && entity.getSelectedOption() != null) {
            dto.setAnswerText(entity.getSelectedOption().getText());
        }

        // Populate correct option details for MCQs
        if (entity.getQuestion() != null && entity.getQuestion().getOptions() != null) {
            entity.getQuestion().getOptions().stream()
                    .filter(option -> option.getIsCorrect() != null && option.getIsCorrect())
                    .findFirst()
                    .ifPresent(correctOption -> {
                        dto.setCorrectOptionId(correctOption.getId());
                        dto.setCorrectOptionText(correctOption.getText());
                    });
        }
    }

    /**
     * Maps DTO to entity, ignoring relationships set in service layer.
     * Not typically used in paper submission flow.
     */
    @Mapping(target = "attempt", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "selectedOption", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    StudentAnswer toEntity(StudentAnswerDto dto);

    /**
     * Post-mapping to set Question entity reference from questionId in DTO.
     * Required for draft saving to work correctly.
     */
    @AfterMapping
    default void setQuestionReference(@MappingTarget StudentAnswer entity, StudentAnswerDto dto) {
        if (dto.getQuestionId() != null) {
            com.eduapp.backend.model.Question question = new com.eduapp.backend.model.Question();
            question.setId(dto.getQuestionId());
            entity.setQuestion(question);
        }
    }

    /**
     * Maps list of entities to list of DTOs.
     */
    List<StudentAnswerDto> toDtoList(List<StudentAnswer> answers);

    /**
     * Maps list of DTOs to list of entities.
     */
    List<StudentAnswer> toEntityList(List<StudentAnswerDto> dtos);
}
