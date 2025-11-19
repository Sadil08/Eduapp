package com.eduapp.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.eduapp.backend.model.LeaderboardEntry;
import com.eduapp.backend.service.LeaderboardEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LeaderboardEntryController.class)
public class LeaderboardEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardEntryService leaderboardEntryService;

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
        // Arrange
        List<LeaderboardEntry> entries = List.of(new LeaderboardEntry());
        when(leaderboardEntryService.findAll()).thenReturn(entries);

        // Act & Assert
        mockMvc.perform(get("/api/leaderboard-entries")
                .with(user("user").roles("STUDENT")))
                .andExpect(status().isOk());
    }
}