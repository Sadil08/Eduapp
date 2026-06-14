package com.eduapp.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Paper;

public interface PaperRepository extends JpaRepository<Paper, Long> {

    // SCALE-3: server-side search + pagination (e.g. custom-bundle paper picker) so the
    // client never loads the whole papers table as it grows.
    Page<Paper> findByNameContainingIgnoreCase(String name, Pageable pageable);
}