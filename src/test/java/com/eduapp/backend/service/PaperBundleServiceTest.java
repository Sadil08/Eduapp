package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.mapper.PaperBundleMapper;
import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.LessonRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.SubjectRepository;

@ExtendWith(MockitoExtension.class)
public class PaperBundleServiceTest {

    @Mock
    private PaperBundleRepository paperBundleRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private PaperBundleMapper paperBundleMapper;

    @InjectMocks
    private PaperBundleService paperBundleService;

    @Test
    void createBundle_Success() {
        // Arrange
        PaperBundleDto dto = new PaperBundleDto();
        dto.setName("Test Bundle");
        dto.setSubjectId(1L);
        dto.setLessonId(2L);

        Subject subject = new Subject("Math");
        subject.setId(1L);
        Lesson lesson = new Lesson();
        lesson.setId(2L);

        PaperBundle entity = new PaperBundle();
        PaperBundle saved = new PaperBundle();
        saved.setId(10L);
        PaperBundleDto resultDto = new PaperBundleDto();
        resultDto.setId(10L);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(lesson));
        when(paperBundleMapper.toEntity(dto)).thenReturn(entity);
        when(paperBundleRepository.save(entity)).thenReturn(saved);
        when(paperBundleMapper.toDto(saved)).thenReturn(resultDto);

        // Act
        PaperBundleDto result = paperBundleService.createBundle(dto);

        // Assert
        assertThat(result.getId()).isEqualTo(10L);
        verify(paperBundleRepository).save(any(PaperBundle.class));
    }

    @Test
    void createBundle_SubjectNotFound_ThrowsException() {
        // Arrange
        PaperBundleDto dto = new PaperBundleDto();
        dto.setName("Test Bundle");
        dto.setSubjectId(1L);

        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paperBundleService.createBundle(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Subject not found");
    }
}