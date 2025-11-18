package com.eduapp.backend.service;

import com.eduapp.backend.model.AIAnalysis;
import com.eduapp.backend.repository.AIAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class AIAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AIAnalysisService.class);

    private final AIAnalysisRepository aiAnalysisRepository;

    public AIAnalysisService(AIAnalysisRepository aiAnalysisRepository) {
        this.aiAnalysisRepository = aiAnalysisRepository;
    }

    public List<AIAnalysis> findAll() {
        logger.info("Fetching all AI analyses");
        return aiAnalysisRepository.findAll();
    }

    public Optional<AIAnalysis> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching AI analysis with ID: {}", id);
        return aiAnalysisRepository.findById(id);
    }

    public AIAnalysis save(AIAnalysis aiAnalysis) {
        if (aiAnalysis == null) {
            throw new IllegalArgumentException("AIAnalysis cannot be null");
        }
        logger.info("Saving AI analysis");
        return aiAnalysisRepository.save(aiAnalysis);
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting AI analysis with ID: {}", id);
        aiAnalysisRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return aiAnalysisRepository.existsById(id);
    }
}