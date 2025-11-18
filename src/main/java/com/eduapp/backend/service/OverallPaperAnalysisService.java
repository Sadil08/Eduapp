package com.eduapp.backend.service;

import com.eduapp.backend.model.OverallPaperAnalysis;
import com.eduapp.backend.repository.OverallPaperAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class OverallPaperAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(OverallPaperAnalysisService.class);

    private final OverallPaperAnalysisRepository overallPaperAnalysisRepository;

    public OverallPaperAnalysisService(OverallPaperAnalysisRepository overallPaperAnalysisRepository) {
        this.overallPaperAnalysisRepository = overallPaperAnalysisRepository;
    }

    public List<OverallPaperAnalysis> findAll() {
        logger.info("Fetching all overall paper analyses");
        return overallPaperAnalysisRepository.findAll();
    }

    public Optional<OverallPaperAnalysis> findById(Long id) {
        logger.info("Fetching overall paper analysis with ID: {}", id);
        return overallPaperAnalysisRepository.findById(id);
    }

    public OverallPaperAnalysis save(OverallPaperAnalysis overallPaperAnalysis) {
        logger.info("Saving overall paper analysis");
        return overallPaperAnalysisRepository.save(overallPaperAnalysis);
    }

    public void deleteById(Long id) {
        logger.info("Deleting overall paper analysis with ID: {}", id);
        overallPaperAnalysisRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return overallPaperAnalysisRepository.existsById(id);
    }
}