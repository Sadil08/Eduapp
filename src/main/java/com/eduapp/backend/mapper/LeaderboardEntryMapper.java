package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.LeaderboardEntryDto;
import com.eduapp.backend.model.LeaderboardEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaderboardEntryMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "paper.id", target = "paperId")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "paper.totalMarks", target = "paperTotalMarks")
    @Mapping(target = "percentage", ignore = true)
    LeaderboardEntryDto toDto(LeaderboardEntry entity);

    @org.mapstruct.AfterMapping
    default void calculatePercentage(@org.mapstruct.MappingTarget LeaderboardEntryDto dto, LeaderboardEntry entity) {
        if (entity.getPaper() != null && entity.getPaper().getTotalMarks() != null
                && entity.getPaper().getTotalMarks() > 0 && entity.getScore() != null) {
            dto.setPercentage((double) entity.getScore() / entity.getPaper().getTotalMarks() * 100);
        }
    }
}
