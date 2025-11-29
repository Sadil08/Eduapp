package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.AIAnalysisDto;
import com.eduapp.backend.model.AIAnalysis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AIAnalysisMapper {

    @Mapping(source = "answer.id", target = "answerId")
    AIAnalysisDto toDto(AIAnalysis entity);
}
