package com.eduapp.backend.mapper;

import org.mapstruct.*;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.dto.StudentPaperAttemptDto;
import java.util.List;

@Mapper(componentModel = "spring", uses = { StudentAnswerMapper.class })
public interface StudentPaperAttemptMapper {

    // Maps StudentPaperAttempt entity to DTO, extracting paper and student IDs for frontend
    @Mapping(source = "paper.id", target = "paperId")
    @Mapping(source = "student.id", target = "studentId")
    StudentPaperAttemptDto toDto(StudentPaperAttempt entity);

    // Maps DTO to entity, ignoring relationships set in service layer
    @Mapping(target = "paper", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "answers", ignore = true)
    StudentPaperAttempt toEntity(StudentPaperAttemptDto dto);

    // Maps list of entities to list of DTOs
    List<StudentPaperAttemptDto> toDtoList(List<StudentPaperAttempt> attempts);
}
