package com.eduapp.backend.repository;

import com.eduapp.backend.model.ExtraAttemptPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraAttemptPurchaseRepository extends JpaRepository<ExtraAttemptPurchase, Long> {

    /**
     * Sum all extra attempts granted to a user for a specific paper
     */
    @Query("SELECT COALESCE(SUM(e.attemptsGranted), 0) FROM ExtraAttemptPurchase e WHERE e.user.id = :userId AND e.paper.id = :paperId")
    Integer sumExtraAttemptsByUserAndPaper(@Param("userId") Long userId, @Param("paperId") Long paperId);
}
