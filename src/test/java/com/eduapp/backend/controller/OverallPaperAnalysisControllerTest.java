package com.eduapp.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.service.OverallPaperAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OverallPaperAnalysisController.class)
public class OverallPaperAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OverallPaperAnalysisService overallPaperAnalysisService;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAll_ReturnsOk() throws Exception {
        List<OverallPaperAnalysis> analyses = List.of(new OverallPaperAnalysis());
        when(overallPaperAnalysisService.findAll()).thenReturn(analyses);

        mockMvc.perform(get("/api/overall-paper-analyses")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ReturnsOk() throws Exception {
        OverallPaperAnalysis analysis = new OverallPaperAnalysis();
        when(overallPaperAnalysisService.findById(1L)).thenReturn(Optional.of(analysis));

        mockMvc.perform(get("/api/overall-paper-analyses/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getById_ReturnsNotFound() throws Exception {
        when(overallPaperAnalysisService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/overall-paper-analyses/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_ReturnsOk() throws Exception {
        OverallPaperAnalysis analysis = new OverallPaperAnalysis();
        when(overallPaperAnalysisService.save(any(OverallPaperAnalysis.class))).thenReturn(analysis);

        mockMvc.perform(post("/api/overall-paper-analyses")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void update_ReturnsOk() throws Exception {
        OverallPaperAnalysis analysis = new OverallPaperAnalysis();
        when(overallPaperAnalysisService.existsById(1L)).thenReturn(true);
        when(overallPaperAnalysisService.save(any(OverallPaperAnalysis.class))).thenReturn(analysis);

        mockMvc.perform(put("/api/overall-paper-analyses/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void update_ReturnsNotFound() throws Exception {
        when(overallPaperAnalysisService.existsById(1L)).thenReturn(false);

        mockMvc.perform(put("/api/overall-paper-analyses/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        when(overallPaperAnalysisService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/overall-paper-analyses/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_ReturnsNotFound() throws Exception {
        when(overallPaperAnalysisService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/overall-paper-analyses/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}