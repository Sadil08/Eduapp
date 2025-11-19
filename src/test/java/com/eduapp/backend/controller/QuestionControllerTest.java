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

import com.eduapp.backend.dto.QuestionDto;
import com.eduapp.backend.mapper.QuestionMapper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuestionController.class)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private QuestionMapper questionMapper;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAllQuestions_ReturnsOk() throws Exception {
        List<Question> questions = List.of(new Question());
        List<QuestionDto> dtos = List.of(new QuestionDto());
        when(questionService.findAll()).thenReturn(questions);
        when(questionMapper.toDtoList(questions)).thenReturn(dtos);

        mockMvc.perform(get("/api/questions")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getQuestionById_ReturnsOk() throws Exception {
        Question question = new Question();
        QuestionDto dto = new QuestionDto();
        when(questionService.findById(1L)).thenReturn(Optional.of(question));
        when(questionMapper.toDto(question)).thenReturn(dto);

        mockMvc.perform(get("/api/questions/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getQuestionById_ReturnsNotFound() throws Exception {
        when(questionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/questions/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuestion_ReturnsCreated() throws Exception {
        QuestionDto dto = new QuestionDto();
        Question question = new Question();
        Question savedQuestion = new Question();
        QuestionDto savedDto = new QuestionDto();
        when(questionMapper.toEntity(dto)).thenReturn(question);
        when(questionService.save(question)).thenReturn(savedQuestion);
        when(questionMapper.toDto(savedQuestion)).thenReturn(savedDto);

        mockMvc.perform(post("/api/questions")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void updateQuestion_ReturnsOk() throws Exception {
        Question existingQuestion = new Question();
        QuestionDto dto = new QuestionDto();
        Question updatedQuestion = new Question();
        QuestionDto updatedDto = new QuestionDto();
        when(questionService.findById(1L)).thenReturn(Optional.of(existingQuestion));
        when(questionService.save(any(Question.class))).thenReturn(updatedQuestion);
        when(questionMapper.toDto(updatedQuestion)).thenReturn(updatedDto);

        mockMvc.perform(put("/api/questions/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuestion_ReturnsNotFound() throws Exception {
        when(questionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/questions/1")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteQuestion_ReturnsNoContent() throws Exception {
        when(questionService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/questions/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteQuestion_ReturnsNotFound() throws Exception {
        when(questionService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/questions/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}