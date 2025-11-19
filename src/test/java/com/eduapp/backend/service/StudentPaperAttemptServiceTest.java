package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.AttemptStatus;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class StudentPaperAttemptServiceTest {

    @Mock
    private StudentPaperAttemptRepository attemptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaperRepository paperRepository;

    @InjectMocks
    private StudentPaperAttemptService studentPaperAttemptService;

    @Test
    void save_Success() {
        // Arrange
        User student = new User();
        student.setId(1L);
        Paper paper = new Paper();
        paper.setId(1L);
        StudentPaperAttempt attempt = new StudentPaperAttempt(student, paper, 1, LocalDateTime.now());
        attempt.setStatus(AttemptStatus.IN_PROGRESS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));
        StudentPaperAttempt saved = new StudentPaperAttempt(student, paper, 1, LocalDateTime.now());
        saved.setId(1L);
        saved.setStatus(AttemptStatus.IN_PROGRESS);
        when(attemptRepository.save(any(StudentPaperAttempt.class))).thenReturn(saved);

        // Act
        StudentPaperAttempt result = studentPaperAttemptService.save(attempt);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    }

    @Test
    void save_UserNotExist_ThrowsException() {
        // Arrange
        User student = new User();
        student.setId(1L);
        Paper paper = new Paper();
        paper.setId(1L);
        StudentPaperAttempt attempt = new StudentPaperAttempt(student, paper, 1, LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentPaperAttemptService.save(attempt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User does not exist");
    }

    @Test
    void save_PaperNotExist_ThrowsException() {
        // Arrange
        User student = new User();
        student.setId(1L);
        Paper paper = new Paper();
        paper.setId(1L);
        StudentPaperAttempt attempt = new StudentPaperAttempt(student, paper, 1, LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(paperRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> studentPaperAttemptService.save(attempt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Paper does not exist");
    }
}