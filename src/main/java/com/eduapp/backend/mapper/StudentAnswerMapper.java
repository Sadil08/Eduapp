package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.dto.StudentAnswerDto;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StudentAnswerMapper {

    // Maps StudentAnswer entity to DTO, extracting IDs for attempt, question, and selected option
    @Mapping(source = "attempt.id", target = "attemptId")
    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "selectedOption.id", target = "selectedOptionId")
    StudentAnswerDto toDto(StudentAnswer entity);

    // Maps DTO to entity, ignoring relationships set in service layer
    @Mapping(target = "attempt", ignore = true)
    @Mapping(target = "question", ignore = true)
    @Mapping(target = "selectedOption", ignore = true)
    StudentAnswer toEntity(StudentAnswerDto dto);

    // Maps list of entities to list of DTOs
    List<StudentAnswerDto> toDtoList(List<StudentAnswer> answers);
}
