package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.dto.QuestionOptionDto;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionOptionMapper {

    // Maps QuestionOption entity to DTO, extracting question ID for frontend
    @Mapping(source = "question.id", target = "questionId")
    QuestionOptionDto toDto(QuestionOption entity);

    // Maps DTO to entity, ignoring question relationship set in service layer
    @Mapping(target = "question", ignore = true)
    QuestionOption toEntity(QuestionOptionDto dto);

    // Maps list of entities to list of DTOs
    List<QuestionOptionDto> toDtoList(List<QuestionOption> options);
}
