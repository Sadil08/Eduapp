package com.eduapp.backend.service;

import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Subject entities.
 * Provides business logic for CRUD operations on subjects.
 * Follows Single Responsibility Principle by handling only subject-related
 * operations.
 */
@Service
@SuppressWarnings("null")
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);

    private final SubjectRepository subjectRepository;
    private final com.eduapp.backend.repository.LessonRepository lessonRepository;
    private final com.eduapp.backend.repository.PaperBundleRepository paperBundleRepository;

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param subjectRepository     the repository for Subject entities
     * @param lessonRepository      the repository for Lesson entities
     * @param paperBundleRepository the repository for PaperBundle entities
     */
    public SubjectService(SubjectRepository subjectRepository,
            com.eduapp.backend.repository.LessonRepository lessonRepository,
            com.eduapp.backend.repository.PaperBundleRepository paperBundleRepository) {
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.paperBundleRepository = paperBundleRepository;
    }

    /**
     * Retrieves all subjects from the database.
     * 
     * @return a list of all Subject entities
     */
    public List<Subject> findAll() {
        logger.info("Fetching all subjects");
        List<Subject> subjects = subjectRepository.findAll();
        logger.info("Found {} subjects", subjects.size());
        return subjects;
    }

    /**
     * Retrieves a subject by its ID.
     * 
     * @param id the ID of the subject to retrieve
     * @return an Optional containing the Subject if found, or empty if not
     */
    public Optional<Subject> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching subject with ID: {}", id);
        Optional<Subject> subject = subjectRepository.findById(id);
        if (subject.isPresent()) {
            logger.info("Subject found: {}", subject.get().getName());
        } else {
            logger.warn("Subject with ID {} not found", id);
        }
        return subject;
    }

    /**
     * Saves a new or updated subject to the database.
     * 
     * @param subject the Subject entity to save
     * @return the saved Subject entity
     */
    public Subject save(Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        logger.info("Saving subject: {}", subject.getName());
        Subject savedSubject = subjectRepository.save(subject);
        logger.info("Subject saved with ID: {}", savedSubject.getId());
        return savedSubject;
    }

    /**
     * Deletes a subject by its ID.
     * 
     * @param id the ID of the subject to delete
     * @throws IllegalStateException if the subject is used by any lessons or
     *                               bundles
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting subject with ID: {}", id);

        // Integrity checks
        long lessonCount = lessonRepository.countBySubjectId(id);
        if (lessonCount > 0) {
            logger.error("Cannot delete subject {}: It has {} associated lessons", id, lessonCount);
            throw new IllegalStateException("Cannot delete subject: It has " + lessonCount + " associated lessons.");
        }

        long bundleCount = paperBundleRepository.countBySubjectId(id);
        if (bundleCount > 0) {
            logger.error("Cannot delete subject {}: It is used by {} bundles", id, bundleCount);
            throw new IllegalStateException("Cannot delete subject: It is used by " + bundleCount + " bundles.");
        }

        if (subjectRepository.existsById(id)) {
            subjectRepository.deleteById(id);
            logger.info("Subject deleted successfully");
        } else {
            logger.warn("Subject with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a subject exists by its ID.
     * 
     * @param id the ID to check
     * @return true if the subject exists, false otherwise
     */
    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        boolean exists = subjectRepository.existsById(id);
        logger.debug("Subject existence check for ID {}: {}", id, exists);
        return exists;
    }
}