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

    @Mapping(source = "bundle.id", target = "bundleId")
    PaperDto toDto(Paper entity);

    @Mapping(target = "bundle", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Paper toEntity(PaperDto dto);

    List<PaperDto> toDtoList(List<Paper> papers);

    // New methods for Summary and Detail DTOs
    @Named("toSummaryDto")
    @Mapping(source = "bundle.id", target = "bundleId")
    PaperSummaryDto toSummaryDto(Paper entity);

    @Mapping(source = "bundle.id", target = "bundleId")
    PaperDetailDto toDetailDto(Paper entity);

    @IterableMapping(qualifiedByName = "toSummaryDto")
    List<PaperSummaryDto> toSummaryDtoList(List<Paper> papers);

    @Mapping(source = "bundle.id", target = "bundleId")
    PaperAttemptDto toAttemptDto(Paper entity);
}
