package com.eduapp.backend.service;

import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.repository.QuestionOptionRepository;
import com.eduapp.backend.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing QuestionOption entities.
 * Provides business logic for CRUD operations on question options, including validation of associated questions.
 * Follows Single Responsibility Principle by handling only question option-related operations.
 */
@Service
@SuppressWarnings("null")
public class QuestionOptionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionOptionService.class);

    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionRepository questionRepository;

    /**
     * Constructor for dependency injection of repositories.
     * @param questionOptionRepository the repository for QuestionOption entities
     * @param questionRepository the repository for Question entities
     */
    public QuestionOptionService(QuestionOptionRepository questionOptionRepository, QuestionRepository questionRepository) {
        this.questionOptionRepository = questionOptionRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Retrieves all question options from the database.
     * @return a list of all QuestionOption entities
     */
    public List<QuestionOption> findAll() {
        logger.info("Fetching all question options");
        List<QuestionOption> options = questionOptionRepository.findAll();
        logger.info("Found {} question options", options.size());
        return options;
    }

    /**
     * Retrieves a question option by its ID.
     * @param id the ID of the question option to retrieve
     * @return an Optional containing the QuestionOption if found, or empty if not
     */
    public Optional<QuestionOption> findById(Long id) {
        logger.info("Fetching question option with ID: {}", id);
        Optional<QuestionOption> option = questionOptionRepository.findById(id);
        if (option.isPresent()) {
            logger.info("Question option found: {}", option.get().getText().substring(0, Math.min(50, option.get().getText().length())));
        } else {
            logger.warn("Question option with ID {} not found", id);
        }
        return option;
    }

    /**
     * Saves a new or updated question option to the database.
     * Validates that the associated question exists.
     * @param option the QuestionOption entity to save
     * @return the saved QuestionOption entity
     * @throws IllegalArgumentException if the question does not exist
     */
    public QuestionOption save(QuestionOption option) {
        logger.info("Saving question option for question ID: {}", option.getQuestion() != null ? option.getQuestion().getId() : null);
        if (option.getQuestion() != null && option.getQuestion().getId() != null) {
            Optional<Question> question = questionRepository.findById(option.getQuestion().getId());
            if (question.isEmpty()) {
                logger.error("Question with ID {} does not exist", option.getQuestion().getId());
                throw new IllegalArgumentException("Question does not exist");
            }
        }
        QuestionOption savedOption = questionOptionRepository.save(option);
        logger.info("Question option saved with ID: {}", savedOption.getId());
        return savedOption;
    }

    /**
     * Deletes a question option by its ID.
     * @param id the ID of the question option to delete
     */
    public void deleteById(Long id) {
        logger.info("Deleting question option with ID: {}", id);
        if (questionOptionRepository.existsById(id)) {
            questionOptionRepository.deleteById(id);
            logger.info("Question option deleted successfully");
        } else {
            logger.warn("Question option with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a question option exists by its ID.
     * @param id the ID to check
     * @return true if the question option exists, false otherwise
     */
    public boolean existsById(Long id) {
        boolean exists = questionOptionRepository.existsById(id);
        logger.debug("Question option existence check for ID {}: {}", id, exists);
        return exists;
    }
}