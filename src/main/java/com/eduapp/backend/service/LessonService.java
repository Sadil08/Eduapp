package com.eduapp.backend.service;

import com.eduapp.backend.model.Lesson;
import com.eduapp.backend.model.Subject;
import com.eduapp.backend.repository.LessonRepository;
import com.eduapp.backend.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Lesson entities.
 * Provides business logic for CRUD operations on lessons, including validation
 * of associated subjects.
 * Follows Single Responsibility Principle by handling only lesson-related
 * operations.
 */
@Service
@SuppressWarnings("null")
public class LessonService {

    private static final Logger logger = LoggerFactory.getLogger(LessonService.class);

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final com.eduapp.backend.repository.PaperBundleRepository paperBundleRepository;

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param lessonRepository      the repository for Lesson entities
     * @param subjectRepository     the repository for Subject entities
     * @param paperBundleRepository the repository for PaperBundle entities
     */
    public LessonService(LessonRepository lessonRepository,
            SubjectRepository subjectRepository,
            com.eduapp.backend.repository.PaperBundleRepository paperBundleRepository) {
        this.lessonRepository = lessonRepository;
        this.subjectRepository = subjectRepository;
        this.paperBundleRepository = paperBundleRepository;
    }

    /**
     * Retrieves all lessons from the database.
     * 
     * @return a list of all Lesson entities
     */
    public List<Lesson> findAll() {
        logger.info("Fetching all lessons");
        List<Lesson> lessons = lessonRepository.findAll();
        logger.info("Found {} lessons", lessons.size());
        return lessons;
    }

    /**
     * Retrieves a lesson by its ID.
     * 
     * @param id the ID of the lesson to retrieve
     * @return an Optional containing the Lesson if found, or empty if not
     */
    public Optional<Lesson> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching lesson with ID: {}", id);
        Optional<Lesson> lesson = lessonRepository.findById(id);
        if (lesson.isPresent()) {
            logger.info("Lesson found: {}", lesson.get().getName());
        } else {
            logger.warn("Lesson with ID {} not found", id);
        }
        return lesson;
    }

    /**
     * Saves a new or updated lesson to the database.
     * Validates that the associated subject exists.
     * 
     * @param lesson the Lesson entity to save
     * @return the saved Lesson entity
     * @throws IllegalArgumentException if the subject does not exist
     */
    public Lesson save(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("Lesson cannot be null");
        }
        logger.info("Saving lesson: {}", lesson.getName());
        if (lesson.getSubject() != null && lesson.getSubject().getId() != null) {
            Optional<Subject> subject = subjectRepository.findById(lesson.getSubject().getId());
            if (subject.isEmpty()) {
                logger.error("Subject with ID {} does not exist", lesson.getSubject().getId());
                throw new IllegalArgumentException("Subject does not exist");
            }
        }
        Lesson savedLesson = lessonRepository.save(lesson);
        logger.info("Lesson saved with ID: {}", savedLesson.getId());
        return savedLesson;
    }

    /**
     * Deletes a lesson by its ID.
     * 
     * @param id the ID of the lesson to delete
     * @throws IllegalStateException if the lesson is used by any bundles
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting lesson with ID: {}", id);

        // Integrity check
        long bundleCount = paperBundleRepository.countByLessonId(id);
        if (bundleCount > 0) {
            logger.error("Cannot delete lesson {}: It is used by {} bundles", id, bundleCount);
            throw new IllegalStateException("Cannot delete lesson: It is used by " + bundleCount + " bundles.");
        }

        if (lessonRepository.existsById(id)) {
            lessonRepository.deleteById(id);
            logger.info("Lesson deleted successfully");
        } else {
            logger.warn("Lesson with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a lesson exists by its ID.
     * 
     * @param id the ID to check
     * @return true if the lesson exists, false otherwise
     */
    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        boolean exists = lessonRepository.existsById(id);
        logger.debug("Lesson existence check for ID {}: {}", id, exists);
        return exists;
    }
}