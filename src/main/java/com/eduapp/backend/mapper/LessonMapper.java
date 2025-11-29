package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.LessonDto;
import com.eduapp.backend.model.Lesson;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PaperBundleMapper.class})
public interface LessonMapper {

    // Maps Lesson entity to DTO, extracting subject ID for frontend
    @Mapping(source = "subject.id", target = "subjectId")
    LessonDto toDto(Lesson lesson);

    // Maps DTO to entity, ignoring relationships and audit fields set in service layer
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "bundles", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Lesson toEntity(LessonDto dto);

    // Maps list of entities to list of DTOs
    List<LessonDto> toDtoList(List<Lesson> lessons);
}
