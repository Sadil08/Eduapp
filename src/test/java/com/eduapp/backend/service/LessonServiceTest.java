package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.LessonRepository;
import com.eduapp.backend.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void save_Success() {
        // Arrange
        Subject subject = new Subject("Math");
        subject.setId(1L);
        Lesson lesson = new Lesson();
        lesson.setName("Algebra");
        lesson.setSubject(subject);
        Lesson savedLesson = new Lesson();
        savedLesson.setId(2L);
        savedLesson.setName("Algebra");
        savedLesson.setSubject(subject);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(lessonRepository.save(lesson)).thenReturn(savedLesson);

        // Act
        Lesson result = lessonService.save(lesson);

        // Assert
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getSubject().getName()).isEqualTo("Math");
    }

    @Test
    void save_SubjectNotFound_ThrowsException() {
        // Arrange
        Subject subject = new Subject("Math");
        subject.setId(1L);
        Lesson lesson = new Lesson();
        lesson.setName("Algebra");
        lesson.setSubject(subject);
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> lessonService.save(lesson))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Subject does not exist");
    }
}