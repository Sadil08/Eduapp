package com.eduapp.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.eduapp.backend.dto.LessonDto;
import com.eduapp.backend.mapper.LessonMapper;
import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.repository.UserRepository;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.LessonService;

import org.springframework.security.crypto.password.PasswordEncoder;

@WebMvcTest(LessonController.class)
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private LessonMapper lessonMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void getAllLessons_ReturnsOk() throws Exception {
        // Arrange
        Lesson lesson = new Lesson();
        LessonDto dto = new LessonDto();
        when(lessonService.findAll()).thenReturn(List.of(lesson));
        when(lessonMapper.toDtoList(List.of(lesson))).thenReturn(List.of(dto));

        // Act & Assert
        mockMvc.perform(get("/api/lessons"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }
}