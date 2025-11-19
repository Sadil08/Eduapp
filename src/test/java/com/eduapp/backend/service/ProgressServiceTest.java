package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Progress;
import com.eduapp.backend.repository.ProgressRepository;

@ExtendWith(MockitoExtension.class)
public class ProgressServiceTest {

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private ProgressService progressService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        Progress progress = new Progress();
        when(progressRepository.findAll()).thenReturn(List.of(progress));

        // Act
        List<Progress> result = progressService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}