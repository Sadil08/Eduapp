package com.eduapp.backend.service;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing StudentPaperAttempt entities.
 * Provides business logic for CRUD operations on student paper attempts,
 * including validation of associated users and papers.
 * Follows Single Responsibility Principle by handling only student paper
 * attempt-related operations.
 */
@Service
@SuppressWarnings("null")
public class StudentPaperAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(StudentPaperAttemptService.class);

    private final StudentPaperAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param attemptRepository the repository for StudentPaperAttempt entities
     * @param userRepository    the repository for User entities
     * @param paperRepository   the repository for Paper entities
     */
    public StudentPaperAttemptService(StudentPaperAttemptRepository attemptRepository,
            UserRepository userRepository,
            PaperRepository paperRepository) {
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.paperRepository = paperRepository;
    }

    /**
     * Retrieves all student paper attempts from the database.
     * 
     * @return a list of all StudentPaperAttempt entities
     */
    public List<StudentPaperAttempt> findAll() {
        logger.info("Fetching all student paper attempts");
        List<StudentPaperAttempt> attempts = attemptRepository.findAll();
        logger.info("Found {} student paper attempts", attempts.size());
        return attempts;
    }

    /**
     * Retrieves a student paper attempt by its ID.
     * 
     * @param id the ID of the student paper attempt to retrieve
     * @return an Optional containing the StudentPaperAttempt if found, or empty if
     *         not
     */
    public Optional<StudentPaperAttempt> findById(Long id) {
        logger.info("Fetching student paper attempt with ID: {}", id);
        Optional<StudentPaperAttempt> attempt = attemptRepository.findById(id);
        if (attempt.isPresent()) {
            logger.info("Student paper attempt found for paper: {}", attempt.get().getPaper().getName());
        } else {
            logger.warn("Student paper attempt with ID {} not found", id);
        }
        return attempt;
    }

    /**
     * Saves a new or updated student paper attempt to the database.
     * Validates that the associated user and paper exist.
     * 
     * @param attempt the StudentPaperAttempt entity to save
     * @return the saved StudentPaperAttempt entity
     * @throws IllegalArgumentException if the user or paper does not exist
     */
    public StudentPaperAttempt save(StudentPaperAttempt attempt) {
        logger.info("Saving student paper attempt for user ID: {} and paper ID: {}",
                attempt.getStudent() != null ? attempt.getStudent().getId() : null,
                attempt.getPaper() != null ? attempt.getPaper().getId() : null);
        if (attempt.getStudent() != null && attempt.getStudent().getId() != null) {
            Optional<User> user = userRepository.findById(attempt.getStudent().getId());
            if (user.isEmpty()) {
                logger.error("User with ID {} does not exist", attempt.getStudent().getId());
                throw new IllegalArgumentException("User does not exist");
            }
        }
        if (attempt.getPaper() != null && attempt.getPaper().getId() != null) {
            Optional<Paper> paper = paperRepository.findById(attempt.getPaper().getId());
            if (paper.isEmpty()) {
                logger.error("Paper with ID {} does not exist", attempt.getPaper().getId());
                throw new IllegalArgumentException("Paper does not exist");
            }
        }
        StudentPaperAttempt savedAttempt = attemptRepository.save(attempt);
        logger.info("Student paper attempt saved with ID: {}", savedAttempt.getId());
        return savedAttempt;
    }

    /**
     * Deletes a student paper attempt by its ID.
     * 
     * @param id the ID of the student paper attempt to delete
     */
    public void deleteById(Long id) {
        logger.info("Deleting student paper attempt with ID: {}", id);
        if (attemptRepository.existsById(id)) {
            attemptRepository.deleteById(id);
            logger.info("Student paper attempt deleted successfully");
        } else {
            logger.warn("Student paper attempt with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a student paper attempt exists by its ID.
     * 
     * @param id the ID to check
     * @return true if the student paper attempt exists, false otherwise
     */
    public boolean existsById(Long id) {
        boolean exists = attemptRepository.existsById(id);
        logger.debug("Student paper attempt existence check for ID {}: {}", id, exists);
        return exists;
    }

    /**
     * Retrieves all attempts by a specific student for a specific paper.
     * 
     * @param studentId the ID of the student
     * @param paperId   the ID of the paper
     * @return list of attempts ordered by start time (newest first)
     */
    public List<StudentPaperAttempt> getAttemptsByStudentAndPaper(Long studentId, Long paperId) {
        logger.info("Fetching attempts for student ID: {} and paper ID: {}", studentId, paperId);
        return attemptRepository.findByStudentIdAndPaperIdOrderByStartedAtDesc(studentId, paperId);
    }
}