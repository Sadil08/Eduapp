package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.dto.StudentPaperAttemptDto;
import java.util.List;

/**
 * MapStruct mapper for converting StudentPaperAttempt entities to DTOs.
 * Used to transform paper attempts with answers and AI analysis for API
 * responses.
 */
@Mapper(componentModel = "spring", uses = { StudentAnswerMapper.class })
public interface StudentPaperAttemptMapper {

    /**
     * Maps StudentPaperAttempt entity to DTO including answers and AI analysis.
     * Extracts paper and student IDs for frontend display.
     * Note: overallFeedback and totalMarks must be set manually from
     * OverallPaperAnalysis.
     */
    @Mapping(source = "paper.id", target = "paperId")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "paper.totalMarks", target = "paperTotalMarks")
    @Mapping(target = "overallFeedback", ignore = true)
    @Mapping(target = "totalMarks", ignore = true)
    StudentPaperAttemptDto toDto(StudentPaperAttempt entity);

    /**
     * Maps DTO to entity, ignoring relationships set in service layer.
     * Not typically used in paper submission flow.
     */
    @Mapping(target = "paper", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "optedIn", ignore = true)
    StudentPaperAttempt toEntity(StudentPaperAttemptDto dto);

    /**
     * Maps list of entities to list of DTOs.
     */
    List<StudentPaperAttemptDto> toDtoList(List<StudentPaperAttempt> attempts);
}
