package com.eduapp.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.service.AIAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AIAnalysisController.class)
public class AIAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIAnalysisService aiAnalysisService;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAll_ReturnsOk() throws Exception {
        // Arrange
        List<AIAnalysis> analyses = List.of(new AIAnalysis());
        when(aiAnalysisService.findAll()).thenReturn(analyses);

        // Act & Assert
        mockMvc.perform(get("/api/ai-analyses")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }
}