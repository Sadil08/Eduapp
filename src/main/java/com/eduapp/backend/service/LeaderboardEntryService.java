package com.eduapp.backend.service;

import com.eduapp.backend.model.LeaderboardEntry;
import com.eduapp.backend.repository.LeaderboardEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class LeaderboardEntryService {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardEntryService.class);

    private final LeaderboardEntryRepository leaderboardEntryRepository;

    public LeaderboardEntryService(LeaderboardEntryRepository leaderboardEntryRepository) {
        this.leaderboardEntryRepository = leaderboardEntryRepository;
    }

    public List<LeaderboardEntry> findAll() {
        logger.info("Fetching all leaderboard entries");
        return leaderboardEntryRepository.findAll();
    }

    public Optional<LeaderboardEntry> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching leaderboard entry with ID: {}", id);
        return leaderboardEntryRepository.findById(id);
    }

    public LeaderboardEntry save(LeaderboardEntry leaderboardEntry) {
        if (leaderboardEntry == null) {
            throw new IllegalArgumentException("LeaderboardEntry cannot be null");
        }
        logger.info("Saving leaderboard entry");
        return leaderboardEntryRepository.save(leaderboardEntry);
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting leaderboard entry with ID: {}", id);
        leaderboardEntryRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return leaderboardEntryRepository.existsById(id);
    }
}