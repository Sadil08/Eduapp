package com.eduapp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.PaperBundleService;
import com.eduapp.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
//test failed
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserService.class))
@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
public class PaperBundleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperBundleService paperBundleService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPaperBundle_Success() throws Exception {
        // Arrange
        PaperBundleDto dto = new PaperBundleDto();
        dto.setName("Test Bundle");
        dto.setSubjectId(1L);

        PaperBundleDto resultDto = new PaperBundleDto();
        resultDto.setId(10L);
        resultDto.setName("Test Bundle");

        when(paperBundleService.createBundle(any(PaperBundleDto.class))).thenReturn(resultDto);

        // Act & Assert
        mockMvc.perform(post("/api/paper-bundles")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Bundle"));
    }
}