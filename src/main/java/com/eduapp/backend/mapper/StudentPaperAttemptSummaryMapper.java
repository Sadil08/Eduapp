package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.dto.StudentPaperAttemptSummaryDto;
import java.util.List;

/**
 * MapStruct mapper for converting StudentPaperAttempt entities to summary DTOs.
 * Used for lightweight attempt list views without nested answer data.
 */
@Mapper(componentModel = "spring")
public interface StudentPaperAttemptSummaryMapper {

    /**
     * Maps StudentPaperAttempt entity to summary DTO.
     * Truncates overall feedback to 200 characters for list view.
     */
    @Mapping(source = "status", target = "status")
    @Mapping(target = "overallFeedbackSummary", ignore = true)
    @Mapping(target = "totalMarks", ignore = true)
    StudentPaperAttemptSummaryDto toSummaryDto(StudentPaperAttempt entity);

    /**
     * Post-mapping to populate total marks and truncated feedback.
     * Fetches data from OverallPaperAnalysis if available.
     */
    @AfterMapping
    default void populateSummaryFields(@MappingTarget StudentPaperAttemptSummaryDto dto, StudentPaperAttempt entity) {
        // Set paper total marks
        if (entity.getPaper() != null) {
            dto.setPaperTotalMarks(entity.getPaper().getTotalMarks());
        }

        // Calculate total marks from answers if available
        if (entity.getAnswers() != null && !entity.getAnswers().isEmpty()) {
            int totalMarks = entity.getAnswers().stream()
                    .mapToInt(answer -> answer.getMarksAwarded() != null ? answer.getMarksAwarded() : 0)
                    .sum();
            dto.setTotalMarks(totalMarks);
        }

        // Note: overallFeedback will be set by service layer from OverallPaperAnalysis
    }

    /**
     * Maps list of entities to list of summary DTOs.
     */
    List<StudentPaperAttemptSummaryDto> toSummaryDtoList(List<StudentPaperAttempt> attempts);
}
