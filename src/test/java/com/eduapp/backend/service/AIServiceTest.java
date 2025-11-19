package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.model.StudentAnswer;

@ExtendWith(MockitoExtension.class)
public class AIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AIService aiService;

    @Test
    void analyzeAnswer_Success_TextAnswer() {
        // Arrange
        Question question = new Question();
        question.setId(1L);
        question.setText("What is 2+2?");
        question.setCorrectAnswerText("4");

        StudentAnswer answer = new StudentAnswer();
        answer.setQuestion(question);
        answer.setAnswerText("4");

        // Act
        Map<String, Object> result = aiService.analyzeAnswer(answer);

        // Assert
        assertThat(result.get("feedback")).isEqualTo("Good attempt, but review the concept.");
        assertThat(result.get("marks")).isEqualTo(5);
        assertThat(result.get("lessonsToReview")).isEqualTo("Basic algebra");
    }

    @Test
    void analyzeAnswer_Success_OptionAnswer() {
        // Arrange
        Question question = new Question();
        question.setId(1L);
        question.setText("What is 2+2?");
        question.setCorrectAnswerText("4");

        QuestionOption option = new QuestionOption();
        option.setText("4");

        StudentAnswer answer = new StudentAnswer();
        answer.setQuestion(question);
        answer.setSelectedOption(option);

        // Act
        Map<String, Object> result = aiService.analyzeAnswer(answer);

        // Assert
        assertThat(result.get("feedback")).isEqualTo("Good attempt, but review the concept.");
        assertThat(result.get("marks")).isEqualTo(5);
    }

    @Test
    void analyzeAnswer_NullAnswer_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> aiService.analyzeAnswer(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("StudentAnswer cannot be null");
    }
}