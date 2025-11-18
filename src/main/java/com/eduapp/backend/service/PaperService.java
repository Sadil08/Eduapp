package com.eduapp.backend.service;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.PaperBundleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Paper entities.
 * Provides business logic for CRUD operations on papers, including validation of associated bundles.
 * Follows Single Responsibility Principle by handling only paper-related operations.
 */
@Service
@SuppressWarnings("null")
public class PaperService {

    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);

    private final PaperRepository paperRepository;
    private final PaperBundleRepository paperBundleRepository;

    /**
     * Constructor for dependency injection of repositories.
     * @param paperRepository the repository for Paper entities
     * @param paperBundleRepository the repository for PaperBundle entities
     */
    public PaperService(PaperRepository paperRepository, PaperBundleRepository paperBundleRepository) {
        this.paperRepository = paperRepository;
        this.paperBundleRepository = paperBundleRepository;
    }

    /**
     * Retrieves all papers from the database.
     * @return a list of all Paper entities
     */
    public List<Paper> findAll() {
        logger.info("Fetching all papers");
        List<Paper> papers = paperRepository.findAll();
        logger.info("Found {} papers", papers.size());
        return papers;
    }

    /**
     * Retrieves a paper by its ID.
     * @param id the ID of the paper to retrieve
     * @return an Optional containing the Paper if found, or empty if not
     */
    public Optional<Paper> findById(Long id) {
        logger.info("Fetching paper with ID: {}", id);
        Optional<Paper> paper = paperRepository.findById(id);
        if (paper.isPresent()) {
            logger.info("Paper found: {}", paper.get().getName());
        } else {
            logger.warn("Paper with ID {} not found", id);
        }
        return paper;
    }

    /**
     * Saves a new or updated paper to the database.
     * Validates that the associated bundle exists.
     * @param paper the Paper entity to save
     * @return the saved Paper entity
     * @throws IllegalArgumentException if the bundle does not exist
     */
    public Paper save(Paper paper) {
        logger.info("Saving paper: {}", paper.getName());
        if (paper.getBundle() != null && paper.getBundle().getId() != null) {
            Optional<PaperBundle> bundle = paperBundleRepository.findById(paper.getBundle().getId());
            if (bundle.isEmpty()) {
                logger.error("PaperBundle with ID {} does not exist", paper.getBundle().getId());
                throw new IllegalArgumentException("PaperBundle does not exist");
            }
        }
        Paper savedPaper = paperRepository.save(paper);
        logger.info("Paper saved with ID: {}", savedPaper.getId());
        return savedPaper;
    }

    /**
     * Deletes a paper by its ID.
     * @param id the ID of the paper to delete
     */
    public void deleteById(Long id) {
        logger.info("Deleting paper with ID: {}", id);
        if (paperRepository.existsById(id)) {
            paperRepository.deleteById(id);
            logger.info("Paper deleted successfully");
        } else {
            logger.warn("Paper with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a paper exists by its ID.
     * @param id the ID to check
     * @return true if the paper exists, false otherwise
     */
    public boolean existsById(Long id) {
        boolean exists = paperRepository.existsById(id);
        logger.debug("Paper existence check for ID {}: {}", id, exists);
        return exists;
    }
}