package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.LeaderboardEntry;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {}