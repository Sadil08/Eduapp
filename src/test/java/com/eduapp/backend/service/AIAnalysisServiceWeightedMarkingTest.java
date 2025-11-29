package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.AIAnalysisRepository;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AIAnalysisServiceWeightedMarkingTest {

    @Mock
    private OverallPaperAnalysisRepository analysisRepository;
    @Mock
    private AIAnalysisRepository aiAnalysisRepository;
    @Mock
    private StudentPaperAttemptRepository studentPaperAttemptRepository;
    @Mock
    private StudentAnswerRepository studentAnswerRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AIAnalysisService aiAnalysisService;

    @Test
    void analyzeAttempt_CalculatesWeightedScoreCorrectly() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(aiAnalysisService, "apiKey", "test-key");
        ReflectionTestUtils.setField(aiAnalysisService, "apiUrl", "http://test-url");
        ReflectionTestUtils.setField(aiAnalysisService, "model", "test-model");
        ReflectionTestUtils.setField(aiAnalysisService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(aiAnalysisService, "restTemplate", restTemplate);

        Paper paper = new Paper();
        paper.setName("Test Paper");
        paper.setTotalMarks(100); // Paper total marks = 100

        Question q1 = new Question();
        q1.setId(1L);
        q1.setMarks(3);
        q1.setText("Q1");

        Question q2 = new Question();
        q2.setId(2L);
        q2.setMarks(2);
        q2.setText("Q2");

        StudentPaperAttempt attempt = new StudentPaperAttempt();
        attempt.setId(1L);
        attempt.setPaper(paper);

        StudentAnswer a1 = new StudentAnswer();
        a1.setQuestion(q1);
        a1.setAnswerText("A1");

        StudentAnswer a2 = new StudentAnswer();
        a2.setQuestion(q2);
        a2.setAnswerText("A2");

        attempt.setAnswers(List.of(a1, a2));

        // Mock Gemini Response
        // Q1: 3/3 marks, Q2: 1/2 marks. Total Obtained = 4. Total Allocated = 5.
        // Expected Weighted Score: (4/5) * 100 = 80.
        String jsonResponse = "{"
                + "\"questions\": ["
                + "  {\"questionId\": 1, \"marksAwarded\": 3, \"feedback\": \"Good\"},"
                + "  {\"questionId\": 2, \"marksAwarded\": 1, \"feedback\": \"Partial\"}"
                + "],"
                + "\"overallFeedback\": \"Good effort\","
                + "\"totalMarks\": 4" // AI might return raw sum
                + "}";

        // Mock RestTemplate response structure for Gemini
        String fullApiResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": " +
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.textNode(jsonResponse).toString() +
                "}]}}]}";

        when(restTemplate.postForEntity(any(String.class), any(), any(Class.class)))
                .thenReturn(ResponseEntity.ok(fullApiResponse));

        // Act
        aiAnalysisService.analyzeAttempt(attempt);

        // Assert
        ArgumentCaptor<OverallPaperAnalysis> captor = ArgumentCaptor.forClass(OverallPaperAnalysis.class);
        verify(analysisRepository).save(captor.capture());

        OverallPaperAnalysis savedAnalysis = captor.getValue();
        assertThat(savedAnalysis.getTotalMarks()).isEqualTo(80);
    }
}
