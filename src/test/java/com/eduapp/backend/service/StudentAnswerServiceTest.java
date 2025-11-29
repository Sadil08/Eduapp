package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.repository.StudentAnswerRepository;

@ExtendWith(MockitoExtension.class)
public class StudentAnswerServiceTest {

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @InjectMocks
    private StudentAnswerService studentAnswerService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        List<StudentAnswer> answers = List.of(new StudentAnswer());
        when(studentAnswerRepository.findAll()).thenReturn(answers);

        // Act
        List<StudentAnswer> result = studentAnswerService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}