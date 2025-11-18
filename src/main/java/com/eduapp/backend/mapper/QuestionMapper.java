package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.dto.QuestionDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { QuestionOptionMapper.class })
public interface QuestionMapper {

    // Maps Question entity to DTO, extracting paper ID for frontend
    @Mapping(source = "paper.id", target = "paperId")
    QuestionDto toDto(Question entity);

    // Maps DTO to entity, ignoring relationships set in service layer
    @Mapping(target = "paper", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Question toEntity(QuestionDto dto);

    // Maps list of entities to list of DTOs
    List<QuestionDto> toDtoList(List<Question> questions);
}
