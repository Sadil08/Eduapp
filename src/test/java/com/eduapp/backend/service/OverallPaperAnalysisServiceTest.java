package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;

@ExtendWith(MockitoExtension.class)
public class OverallPaperAnalysisServiceTest {

    @Mock
    private OverallPaperAnalysisRepository overallPaperAnalysisRepository;

    @InjectMocks
    private OverallPaperAnalysisService overallPaperAnalysisService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        List<OverallPaperAnalysis> analyses = List.of(new OverallPaperAnalysis());
        when(overallPaperAnalysisRepository.findAll()).thenReturn(analyses);

        // Act
        List<OverallPaperAnalysis> result = overallPaperAnalysisService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}