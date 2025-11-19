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

import com.eduapp.backend.dto.QuestionOptionDto;
import com.eduapp.backend.mapper.QuestionOptionMapper;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.service.QuestionOptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuestionOptionController.class)
public class QuestionOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionOptionService questionOptionService;

    @MockBean
    private QuestionOptionMapper questionOptionMapper;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAllQuestionOptions_ReturnsOk() throws Exception {
        List<QuestionOption> options = List.of(new QuestionOption());
        List<QuestionOptionDto> dtos = List.of(new QuestionOptionDto());
        when(questionOptionService.findAll()).thenReturn(options);
        when(questionOptionMapper.toDtoList(options)).thenReturn(dtos);

        mockMvc.perform(get("/api/question-options")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getQuestionOptionById_ReturnsOk() throws Exception {
        QuestionOption option = new QuestionOption();
        QuestionOptionDto dto = new QuestionOptionDto();
        when(questionOptionService.findById(1L)).thenReturn(Optional.of(option));
        when(questionOptionMapper.toDto(option)).thenReturn(dto);

        mockMvc.perform(get("/api/question-options/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getQuestionOptionById_ReturnsNotFound() throws Exception {
        when(questionOptionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/question-options/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuestionOption_ReturnsCreated() throws Exception {
        QuestionOptionDto dto = new QuestionOptionDto();
        QuestionOption option = new QuestionOption();
        QuestionOption savedOption = new QuestionOption();
        QuestionOptionDto savedDto = new QuestionOptionDto();
        when(questionOptionMapper.toEntity(dto)).thenReturn(option);
        when(questionOptionService.save(option)).thenReturn(savedOption);
        when(questionOptionMapper.toDto(savedOption)).thenReturn(savedDto);

        mockMvc.perform(post("/api/question-options")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteQuestionOption_ReturnsNoContent() throws Exception {
        when(questionOptionService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/question-options/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteQuestionOption_ReturnsNotFound() throws Exception {
        when(questionOptionService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/question-options/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}