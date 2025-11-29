package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
public class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SubjectService subjectService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        Subject subject = new Subject("Math");
        when(subjectRepository.findAll()).thenReturn(List.of(subject));

        // Act
        List<Subject> result = subjectService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Math");
    }

    @Test
    void save_Success() {
        // Arrange
        Subject subject = new Subject("Science");
        Subject saved = new Subject("Science");
        saved.setId(1L);
        when(subjectRepository.save(any(Subject.class))).thenReturn(saved);

        // Act
        Subject result = subjectService.save(subject);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void save_Null_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> subjectService.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Subject cannot be null");
    }
}