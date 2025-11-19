package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.repository.AIAnalysisRepository;

@ExtendWith(MockitoExtension.class)
public class AIAnalysisServiceTest {

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;

    @InjectMocks
    private AIAnalysisService aiAnalysisService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        AIAnalysis analysis = new AIAnalysis();
        when(aiAnalysisRepository.findAll()).thenReturn(List.of(analysis));

        // Act
        List<AIAnalysis> result = aiAnalysisService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}