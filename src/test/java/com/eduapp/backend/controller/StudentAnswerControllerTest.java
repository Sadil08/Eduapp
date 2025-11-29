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

import com.eduapp.backend.dto.StudentAnswerDto;
import com.eduapp.backend.mapper.StudentAnswerMapper;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.service.StudentAnswerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentAnswerController.class)
public class StudentAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentAnswerService studentAnswerService;

    @MockBean
    private StudentAnswerMapper studentAnswerMapper;

    @MockBean
    private com.eduapp.backend.security.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockBean
    private com.eduapp.backend.repository.UserRepository userRepository;

    @Test
    void getAllStudentAnswers_ReturnsOk() throws Exception {
        List<StudentAnswer> answers = List.of(new StudentAnswer());
        List<StudentAnswerDto> dtos = List.of(new StudentAnswerDto());
        when(studentAnswerService.findAll()).thenReturn(answers);
        when(studentAnswerMapper.toDtoList(answers)).thenReturn(dtos);

        mockMvc.perform(get("/api/student-answers")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentAnswerById_ReturnsOk() throws Exception {
        StudentAnswer answer = new StudentAnswer();
        StudentAnswerDto dto = new StudentAnswerDto();
        when(studentAnswerService.findById(1L)).thenReturn(Optional.of(answer));
        when(studentAnswerMapper.toDto(answer)).thenReturn(dto);

        mockMvc.perform(get("/api/student-answers/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void getStudentAnswerById_ReturnsNotFound() throws Exception {
        when(studentAnswerService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/student-answers/1")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createStudentAnswer_ReturnsCreated() throws Exception {
        StudentAnswerDto dto = new StudentAnswerDto();
        StudentAnswer answer = new StudentAnswer();
        StudentAnswer savedAnswer = new StudentAnswer();
        StudentAnswerDto savedDto = new StudentAnswerDto();
        when(studentAnswerMapper.toEntity(dto)).thenReturn(answer);
        when(studentAnswerService.save(answer)).thenReturn(savedAnswer);
        when(studentAnswerMapper.toDto(savedAnswer)).thenReturn(savedDto);

        mockMvc.perform(post("/api/student-answers")
                .contentType("application/json")
                .content("{}")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void deleteStudentAnswer_ReturnsNoContent() throws Exception {
        when(studentAnswerService.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/student-answers/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudentAnswer_ReturnsNotFound() throws Exception {
        when(studentAnswerService.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/student-answers/1")
                .with(user("user").roles("STUDENT"))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}