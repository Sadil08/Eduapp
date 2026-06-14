package com.eduapp.backend.repository;

import com.eduapp.backend.model.Improvement;
import com.eduapp.backend.model.ImprovementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImprovementRepository extends JpaRepository<Improvement, Long> {

    List<Improvement> findByStatusOrderByCreatedAtDesc(ImprovementStatus status);

    // SCALE-4: paginated admin listings.
    Page<Improvement> findByStatusOrderByCreatedAtDesc(ImprovementStatus status, Pageable pageable);

    Page<Improvement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Improvement> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(ImprovementStatus status);

    List<Improvement> findAllByOrderByCreatedAtDesc();
}
