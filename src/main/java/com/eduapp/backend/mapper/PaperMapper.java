package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.dto.PaperDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { QuestionMapper.class })
public interface PaperMapper {

    // Maps Paper entity to DTO, extracting bundle ID for frontend
    @Mapping(source = "bundle.id", target = "bundleId")
    PaperDto toDto(Paper paper);

    // Maps DTO to entity, ignoring relationships and audit fields set in service layer
    @Mapping(target = "bundle", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Paper toEntity(PaperDto dto);

    // Maps list of entities to list of DTOs
    List<PaperDto> toDtoList(List<Paper> papers);
}
