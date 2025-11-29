package com.eduapp.backend.service;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing StudentAnswer entities.
 * Provides business logic for CRUD operations on student answers, including validation of associated attempts and questions.
 * Follows Single Responsibility Principle by handling only student answer-related operations.
 */
@Service
@SuppressWarnings("null")
public class StudentAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(StudentAnswerService.class);

    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    /**
     * Constructor for dependency injection of repositories.
     * @param studentAnswerRepository the repository for StudentAnswer entities
     * @param attemptRepository the repository for StudentPaperAttempt entities
     * @param questionRepository the repository for Question entities
     */
    public StudentAnswerService(StudentAnswerRepository studentAnswerRepository,
                                StudentPaperAttemptRepository attemptRepository,
                                QuestionRepository questionRepository) {
        this.studentAnswerRepository = studentAnswerRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Retrieves all student answers from the database.
     * @return a list of all StudentAnswer entities
     */
    public List<StudentAnswer> findAll() {
        logger.info("Fetching all student answers");
        List<StudentAnswer> answers = studentAnswerRepository.findAll();
        logger.info("Found {} student answers", answers.size());
        return answers;
    }

    /**
     * Retrieves a student answer by its ID.
     * @param id the ID of the student answer to retrieve
     * @return an Optional containing the StudentAnswer if found, or empty if not
     */
    public Optional<StudentAnswer> findById(Long id) {
        logger.info("Fetching student answer with ID: {}", id);
        Optional<StudentAnswer> answer = studentAnswerRepository.findById(id);
        if (answer.isPresent()) {
            logger.info("Student answer found for question ID: {}", answer.get().getQuestion().getId());
        } else {
            logger.warn("Student answer with ID {} not found", id);
        }
        return answer;
    }

    /**
     * Saves a new or updated student answer to the database.
     * Validates that the associated attempt and question exist.
     * @param answer the StudentAnswer entity to save
     * @return the saved StudentAnswer entity
     * @throws IllegalArgumentException if the attempt or question does not exist
     */
    public StudentAnswer save(StudentAnswer answer) {
        logger.info("Saving student answer for attempt ID: {} and question ID: {}",
                    answer.getAttempt() != null ? answer.getAttempt().getId() : null,
                    answer.getQuestion() != null ? answer.getQuestion().getId() : null);
        if (answer.getAttempt() != null && answer.getAttempt().getId() != null) {
            Optional<StudentPaperAttempt> attempt = attemptRepository.findById(answer.getAttempt().getId());
            if (attempt.isEmpty()) {
                logger.error("StudentPaperAttempt with ID {} does not exist", answer.getAttempt().getId());
                throw new IllegalArgumentException("StudentPaperAttempt does not exist");
            }
        }
        if (answer.getQuestion() != null && answer.getQuestion().getId() != null) {
            Optional<Question> question = questionRepository.findById(answer.getQuestion().getId());
            if (question.isEmpty()) {
                logger.error("Question with ID {} does not exist", answer.getQuestion().getId());
                throw new IllegalArgumentException("Question does not exist");
            }
        }
        StudentAnswer savedAnswer = studentAnswerRepository.save(answer);
        logger.info("Student answer saved with ID: {}", savedAnswer.getId());
        return savedAnswer;
    }

    /**
     * Deletes a student answer by its ID.
     * @param id the ID of the student answer to delete
     */
    public void deleteById(Long id) {
        logger.info("Deleting student answer with ID: {}", id);
        if (studentAnswerRepository.existsById(id)) {
            studentAnswerRepository.deleteById(id);
            logger.info("Student answer deleted successfully");
        } else {
            logger.warn("Student answer with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a student answer exists by its ID.
     * @param id the ID to check
     * @return true if the student answer exists, false otherwise
     */
    public boolean existsById(Long id) {
        boolean exists = studentAnswerRepository.existsById(id);
        logger.debug("Student answer existence check for ID {}: {}", id, exists);
        return exists;
    }
}