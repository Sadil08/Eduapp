package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.LeaderboardEntry;
import com.eduapp.backend.repository.LeaderboardEntryRepository;

@ExtendWith(MockitoExtension.class)
public class LeaderboardEntryServiceTest {

    @Mock
    private LeaderboardEntryRepository leaderboardEntryRepository;

    @InjectMocks
    private LeaderboardEntryService leaderboardEntryService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        LeaderboardEntry entry = new LeaderboardEntry();
        when(leaderboardEntryRepository.findAll()).thenReturn(List.of(entry));

        // Act
        List<LeaderboardEntry> result = leaderboardEntryService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}