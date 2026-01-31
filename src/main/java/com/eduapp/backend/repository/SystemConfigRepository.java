package com.eduapp.backend.repository;

import com.eduapp.backend.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    /**
     * Find config by key.
     * The key is the primary key, so this is essentially findById but more semantic.
     */
    Optional<SystemConfig> findByKey(String key);
}
