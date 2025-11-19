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

import com.eduapp.backend.dto.StudentPaperAttemptDto;
import com.eduapp.backend.mapper.StudentPaperAttemptMapper;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.service.StudentPaperAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentPaperAttemptController.class)
public class StudentPaperAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentPaperAttemptService attemptService;

    @MockBean
    private StudentPaperAttemptMapper attemptMapper;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAllAttempts_ReturnsOk() throws Exception {
        List<StudentPaperAttempt> attempts = List.of(new StudentPaperAttempt());
        List<StudentPaperAttemptDto> dtos = List.of(new StudentPaperAttemptDto());
        when(attemptService.findAll()).thenReturn(attempts);
        when(attemptMapper.toDtoList(attempts)).thenReturn(dtos);

        mockMvc.perform(get("/api/student-paper-attempts")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getAttemptById_ReturnsOk() throws Exception {
        StudentPaperAttempt attempt = new StudentPaperAttempt();
        StudentPaperAttemptDto dto = new StudentPaperAttemptDto();
        when(attemptService.findById(1L)).thenReturn(Optional.of(attempt));
        when(attemptMapper.toDto(attempt)).thenReturn(dto);

        mockMvc.perform(get("/api/student-paper-attempts/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getAttemptById_ReturnsNotFound() throws Exception {
        when(attemptService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/student-paper-attempts/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAttempt_ReturnsCreated() throws Exception {
        StudentPaperAttemptDto dto = new StudentPaperAttemptDto();
        StudentPaperAttempt attempt = new StudentPaperAttempt();
        StudentPaperAttempt savedAttempt = new StudentPaperAttempt();
        StudentPaperAttemptDto savedDto = new StudentPaperAttemptDto();
        when(attemptMapper.toEntity(dto)).thenReturn(attempt);
        when(attemptService.save(attempt)).thenReturn(savedAttempt);
        when(attemptMapper.toDto(savedAttempt)).thenReturn(savedDto);

        mockMvc.perform(post("/api/student-paper-attempts")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteAttempt_ReturnsNoContent() throws Exception {
        when(attemptService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/student-paper-attempts/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAttempt_ReturnsNotFound() throws Exception {
        when(attemptService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/student-paper-attempts/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}