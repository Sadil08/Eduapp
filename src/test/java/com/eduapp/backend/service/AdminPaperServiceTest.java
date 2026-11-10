package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperDto;
import com.eduapp.backend.dto.QuestionCreateDto;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionType;
import com.eduapp.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit coverage for the admin paper-management service — the "admin adds papers / adds questions
 * to papers (manual)" flow. Pure Mockito (no Spring context), so it runs without a database.
 *
 * <p>Verifies the happy paths persist the right fields and the not-found paths raise
 * {@link IllegalArgumentException} (which the controllers translate to 404) instead of NPEing.
 */
@ExtendWith(MockitoExtension.class)
class AdminPaperServiceTest {

    @Mock PaperRepository paperRepository;
    @Mock QuestionRepository questionRepository;
    @Mock QuestionOptionRepository optionRepository;
    @Mock StudentPaperAttemptRepository attemptRepository;
    @Mock UserRepository userRepository;
    @Mock PaperBundleRepository paperBundleRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock LessonRepository lessonRepository;

    @InjectMocks AdminPaperService service;

    private PaperDto paperDto(String name) {
        PaperDto dto = new PaperDto();
        dto.setName(name);
        dto.setDescription("desc");
        dto.setType(PaperType.ESSAY);
        dto.setMaxFreeAttempts(2);
        dto.setTotalMarks(100);
        return dto;
    }

    @Test
    void createPaper_persistsMappedFields() {
        when(paperRepository.save(any(Paper.class))).thenAnswer(i -> i.getArgument(0));

        service.createPaper(paperDto("Algebra P1"), null);

        ArgumentCaptor<Paper> captor = ArgumentCaptor.forClass(Paper.class);
        verify(paperRepository).save(captor.capture());
        Paper saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Algebra P1");
        assertThat(saved.getType()).isEqualTo(PaperType.ESSAY);
        assertThat(saved.getTotalMarks()).isEqualTo(100);
        assertThat(saved.getMaxFreeAttempts()).isEqualTo(2);
    }

    @Test
    void updatePaper_whenMissing_throws() {
        when(paperRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePaper(99L, paperDto("x")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
        verify(paperRepository, never()).save(any());
    }

    @Test
    void updatePaper_whenPresent_appliesNewValues() {
        Paper existing = new Paper();
        existing.setName("old");
        when(paperRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paperRepository.save(any(Paper.class))).thenAnswer(i -> i.getArgument(0));

        PaperDto dto = paperDto("new name");
        dto.setTotalMarks(55);
        Paper result = service.updatePaper(1L, dto);

        assertThat(result.getName()).isEqualTo("new name");
        assertThat(result.getTotalMarks()).isEqualTo(55);
    }

    @Test
    void deletePaper_whenMissing_throws_andDoesNotDelete() {
        when(paperRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePaper(7L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(paperRepository, never()).deleteById(any());
    }

    @Test
    void deletePaper_whenPresent_deletes() {
        when(paperRepository.existsById(7L)).thenReturn(true);

        service.deletePaper(7L);

        verify(paperRepository).deleteById(7L);
    }

    @Test
    void addQuestion_whenPaperMissing_throws() {
        when(paperRepository.findById(404L)).thenReturn(Optional.empty());

        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setText("2+2?");
        dto.setType(QuestionType.ESSAY);

        assertThatThrownBy(() -> service.addQuestion(404L, dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(questionRepository, never()).save(any());
    }

    @Test
    void addQuestion_whenPaperPresent_savesQuestionBoundToPaper() {
        Paper paper = new Paper();
        paper.setName("host paper");
        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));
        when(questionRepository.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setText("What is 2+2?");
        dto.setType(QuestionType.ESSAY);
        dto.setCorrectAnswerText("4");
        dto.setMarks(10);

        Question saved = service.addQuestion(1L, dto);

        assertThat(saved.getText()).isEqualTo("What is 2+2?");
        assertThat(saved.getMarks()).isEqualTo(10);
        assertThat(saved.getPaper()).isSameAs(paper);
    }
}
