package com.eduapp.backend.service;

import com.eduapp.backend.model.Progress;
import com.eduapp.backend.repository.ProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class ProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    private final ProgressRepository progressRepository;

    public ProgressService(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public List<Progress> findAll() {
        logger.info("Fetching all progress");
        return progressRepository.findAll();
    }

    public Optional<Progress> findById(Long id) {
        logger.info("Fetching progress with ID: {}", id);
        return progressRepository.findById(id);
    }

    public Progress save(Progress progress) {
        logger.info("Saving progress");
        return progressRepository.save(progress);
    }

    public void deleteById(Long id) {
        logger.info("Deleting progress with ID: {}", id);
        progressRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return progressRepository.existsById(id);
    }
}