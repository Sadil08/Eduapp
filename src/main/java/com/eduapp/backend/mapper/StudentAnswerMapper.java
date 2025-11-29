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
    @Mapping(source = "question.marks", target = "marksAvailable")
    @Mapping(source = "selectedOption.id", target = "selectedOptionId")
    @Mapping(source = "question.correctAnswerText", target = "correctAnswerText")
    @Mapping(target = "correctOptionId", ignore = true)
    @Mapping(target = "correctOptionText", ignore = true)
    StudentAnswerDto toDto(StudentAnswer entity);

    /**
     * Post-mapping to populate correct option details for MCQ questions.
     * Finds the correct option from the question's options list.
     */
    @AfterMapping
    default void populateCorrectOption(@MappingTarget StudentAnswerDto dto, StudentAnswer entity) {
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
     * Maps list of entities to list of DTOs.
     */
    List<StudentAnswerDto> toDtoList(List<StudentAnswer> answers);
}
