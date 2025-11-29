package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionType;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private PaperRepository paperRepository;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void save_Success() {
        // Arrange
        Paper paper = new Paper();
        paper.setId(1L);
        Question question = new Question();
        question.setText("Test Question");
        question.setType(QuestionType.MCQ);
        question.setPaper(paper);
        paper.setTotalMarks(100);

        Question savedQuestion = new Question();
        savedQuestion.setId(10L);

        when(paperRepository.findById(1L)).thenReturn(Optional.of(paper));
        when(questionRepository.save(question)).thenReturn(savedQuestion);

        // Act
        Question result = questionService.save(question);

        // Assert
        assertThat(result.getId()).isEqualTo(10L);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void save_PaperNotFound_ThrowsException() {
        // Arrange
        Paper paper = new Paper();
        paper.setId(1L);
        Question question = new Question();
        question.setText("Test Question");
        question.setType(QuestionType.MCQ);
        question.setPaper(paper);

        when(paperRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.save(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Paper does not exist");
    }
}