package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.dto.PaperSummaryDto;
import com.eduapp.backend.dto.PaperDetailDto;
import com.eduapp.backend.dto.PaperAttemptDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { QuestionMapper.class })
public interface PaperMapper {

    @Mapping(target = "bundleIds", expression = "java(entity.getBundles().stream().map(b -> b.getId()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    PaperDto toDto(Paper entity);

    @Mapping(target = "bundles", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Paper toEntity(PaperDto dto);

    List<PaperDto> toDtoList(List<Paper> papers);

    // New methods for Summary and Detail DTOs
    @Named("toSummaryDto")
    @Mapping(target = "bundleIds", expression = "java(entity.getBundles().stream().map(b -> b.getId()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    PaperSummaryDto toSummaryDto(Paper entity);

    @Mapping(target = "bundleIds", expression = "java(entity.getBundles().stream().map(b -> b.getId()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    PaperDetailDto toDetailDto(Paper entity);

    @IterableMapping(qualifiedByName = "toSummaryDto")
    List<PaperSummaryDto> toSummaryDtoList(List<Paper> papers);

    @Mapping(target = "bundleIds", expression = "java(entity.getBundles().stream().map(b -> b.getId()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    PaperAttemptDto toAttemptDto(Paper entity);
}
