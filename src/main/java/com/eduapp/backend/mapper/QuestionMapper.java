package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.dto.QuestionDto;
import com.eduapp.backend.dto.QuestionAttemptDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { QuestionOptionMapper.class })
public interface QuestionMapper {

    // Maps Question entity to DTO, extracting paper ID for frontend
    @Mapping(source = "paper.id", target = "paperId")
    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "lesson.name", target = "lessonName")
    QuestionDto toDto(Question entity);

    // Maps DTO to entity, ignoring relationships set in service layer
    @Mapping(target = "paper", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Question toEntity(QuestionDto dto);

    // Maps list of entities to list of DTOs
    List<QuestionDto> toDtoList(List<Question> questions);

    @Mapping(source = "paper.id", target = "paperId")
    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "requiresImageDisplay", target = "requiresImageDisplay")
    @Mapping(source = "hideQuestionText", target = "hideQuestionText")
    @Mapping(source = "allowImageAnswer", target = "allowImageAnswer")
    @Mapping(source = "answerTypeHint", target = "answerTypeHint")
    QuestionAttemptDto toAttemptDto(Question entity);
}
