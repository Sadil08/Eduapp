package com.eduapp.backend.mapper;

import com.eduapp.backend.dto.QuestionModelAnswerDto;
import com.eduapp.backend.model.QuestionModelAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionModelAnswerMapper {

    public QuestionModelAnswerDto toDto(QuestionModelAnswer modelAnswer) {
        if (modelAnswer == null) {
            return null;
        }

        QuestionModelAnswerDto dto = new QuestionModelAnswerDto();
        dto.setId(modelAnswer.getId());
        dto.setQuestionId(modelAnswer.getQuestion().getId());
        dto.setAnswerText(modelAnswer.getAnswerText());
        dto.setImageUrl(modelAnswer.getImageUrl());
        dto.setExtractedText(modelAnswer.getExtractedText());

        return dto;
    }

    public List<QuestionModelAnswerDto> toDtoList(List<QuestionModelAnswer> modelAnswers) {
        if (modelAnswers == null) {
            return List.of();
        }
        return modelAnswers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public QuestionModelAnswer toEntity(QuestionModelAnswerDto dto) {
        if (dto == null) {
            return null;
        }

        QuestionModelAnswer modelAnswer = new QuestionModelAnswer();
        modelAnswer.setId(dto.getId());
        // Note: Question relationship should be set by the service layer
        modelAnswer.setAnswerText(dto.getAnswerText());
        modelAnswer.setImageUrl(dto.getImageUrl());
        modelAnswer.setExtractedText(dto.getExtractedText());

        return modelAnswer;
    }
}
