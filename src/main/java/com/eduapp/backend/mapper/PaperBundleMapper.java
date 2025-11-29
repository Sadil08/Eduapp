package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.dto.PaperBundleSummaryDto;
import com.eduapp.backend.dto.PaperBundleDetailDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { PaperMapper.class })
public interface PaperBundleMapper {

    // Maps PaperBundle entity to DTO, extracting subject and lesson IDs for frontend consumption
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "lesson.id", target = "lessonId")
    PaperBundleDto toDto(PaperBundle entity);

    // Maps DTO to entity, ignoring relationships that are set manually in service layer
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "papers", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaperBundle toEntity(PaperBundleDto dto);

    // Maps list of entities to list of DTOs
    List<PaperBundleDto> toDtoList(List<PaperBundle> bundles);

    // New methods for Summary and Detail DTOs
    @Named("toSummaryDto")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "lesson.id", target = "lessonId")
    PaperBundleSummaryDto toSummaryDto(PaperBundle entity);

    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "lesson.id", target = "lessonId")
    PaperBundleDetailDto toDetailDto(PaperBundle entity);

    @IterableMapping(qualifiedByName = "toSummaryDto")
    List<PaperBundleSummaryDto> toSummaryDtoList(List<PaperBundle> bundles);
}
