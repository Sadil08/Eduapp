package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.repository.QuestionOptionRepository;

@ExtendWith(MockitoExtension.class)
public class QuestionOptionServiceTest {

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @InjectMocks
    private QuestionOptionService questionOptionService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        List<QuestionOption> options = List.of(new QuestionOption());
        when(questionOptionRepository.findAll()).thenReturn(options);

        // Act
        List<QuestionOption> result = questionOptionService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}