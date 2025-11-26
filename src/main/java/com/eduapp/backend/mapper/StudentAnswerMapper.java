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

    /**
     * Maps StudentAnswer entity to DTO including AI analysis results.
     * Extracts question text and marks for display purposes.
     */
    @Mapping(source = "attempt.id", target = "attemptId")
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.text", target = "questionText")
    @Mapping(source = "question.marks", target = "marksAvailable")
    @Mapping(source = "selectedOption.id", target = "selectedOptionId")
    StudentAnswerDto toDto(StudentAnswer entity);

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
