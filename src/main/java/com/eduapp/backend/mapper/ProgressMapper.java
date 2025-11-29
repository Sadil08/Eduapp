package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.ProgressDto;
import com.eduapp.backend.model.Progress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "bundle.id", target = "bundleId")
    @Mapping(source = "paper.id", target = "paperId")
    ProgressDto toDto(Progress entity);
}
