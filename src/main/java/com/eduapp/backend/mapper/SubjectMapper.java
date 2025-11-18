package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.SubjectDto;
import com.eduapp.backend.model.Subject;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring", uses = {LessonMapper.class})
public interface SubjectMapper {

    // Maps Subject entity to DTO, fields match by name
    SubjectDto toDto(Subject subject);

    // Maps DTO to entity, ignoring collections and audit fields set in service layer
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "bundles", ignore = true)
    Subject toEntity(SubjectDto dto);

    // Maps list of entities to list of DTOs
    List<SubjectDto> toDtoList(List<Subject> subjects);
}
