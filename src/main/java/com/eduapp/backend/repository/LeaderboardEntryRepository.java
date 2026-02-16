package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.eduapp.backend.model.LeaderboardEntry;
import java.util.List;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {
    List<LeaderboardEntry> findByUserId(Long userId);

    // Analytics queries
    @Query("SELECT le.user.id, AVG(le.score) FROM LeaderboardEntry le " +
           "GROUP BY le.user.id")
    List<Object[]> avgScoreByUser();
}