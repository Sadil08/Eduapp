package com.eduapp.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.model.User;
import com.eduapp.backend.service.PaperService;
import com.eduapp.backend.service.UserService;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserService.class))
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class PaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaperService paperService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    void getAllPapers_ReturnsOk() throws Exception {
        // Arrange
        PaperBundle bundle = new PaperBundle("Bundle", "Desc", null, PaperType.MCQ, "Exam", null, null, false);
        bundle.setId(1L);
        User user = new User("user@example.com", "pass", "user");
        user.setId(1L);
        Paper paper = new Paper("Paper1", "Desc", PaperType.MCQ, bundle, 2);
        paper.setId(1L);
        paper.setCreatedBy(user);

        when(paperService.findAll()).thenReturn(List.of(paper));

        // Act & Assert
        mockMvc.perform(get("/api/papers"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Paper1"));
    }
}