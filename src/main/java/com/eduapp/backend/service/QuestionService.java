package com.eduapp.backend.service;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.repository.PaperRepository;
import com.eduapp.backend.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Question entities.
 * Provides business logic for CRUD operations on questions, including
 * validation of associated papers.
 * Follows Single Responsibility Principle by handling only question-related
 * operations.
 */
@Service
@SuppressWarnings("null")
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final PaperRepository paperRepository;

    /**
     * Constructor for dependency injection of repositories.
     * 
     * @param questionRepository the repository for Question entities
     * @param paperRepository    the repository for Paper entities
     */
    public QuestionService(QuestionRepository questionRepository, PaperRepository paperRepository) {
        this.questionRepository = questionRepository;
        this.paperRepository = paperRepository;
    }

    /**
     * Retrieves all questions from the database.
     * 
     * @return a list of all Question entities
     */
    public List<Question> findAll() {
        logger.info("Fetching all questions");
        List<Question> questions = questionRepository.findAll();
        logger.info("Found {} questions", questions.size());
        return questions;
    }

    /**
     * Retrieves a question by its ID.
     * 
     * @param id the ID of the question to retrieve
     * @return an Optional containing the Question if found, or empty if not
     */
    public Optional<Question> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Fetching question with ID: {}", id);
        Optional<Question> question = questionRepository.findById(id);
        if (question.isPresent()) {
            logger.info("Question found: {}",
                    question.get().getText().substring(0, Math.min(50, question.get().getText().length())));
        } else {
            logger.warn("Question with ID {} not found", id);
        }
        return question;
    }

    /**
     * Saves a new or updated question to the database.
     * Validates that the associated paper exists.
     * 
     * @param question the Question entity to save
     * @return the saved Question entity
     * @throws IllegalArgumentException if the paper does not exist
     */
    public Question save(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        logger.info("Saving question for paper ID: {}",
                question.getPaper() != null ? question.getPaper().getId() : null);
        if (question.getPaper() != null && question.getPaper().getId() != null) {
            Optional<Paper> paper = paperRepository.findById(question.getPaper().getId());
            if (paper.isEmpty()) {
                logger.error("Paper with ID {} does not exist", question.getPaper().getId());
                throw new IllegalArgumentException("Paper does not exist");
            }
        }
        Question savedQuestion = questionRepository.save(question);
        logger.info("Question saved with ID: {}", savedQuestion.getId());
        return savedQuestion;
    }

    /**
     * Deletes a question by its ID.
     * 
     * @param id the ID of the question to delete
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        logger.info("Deleting question with ID: {}", id);
        if (questionRepository.existsById(id)) {
            questionRepository.deleteById(id);
            logger.info("Question deleted successfully");
        } else {
            logger.warn("Question with ID {} not found for deletion", id);
        }
    }

    /**
     * Checks if a question exists by its ID.
     * 
     * @param id the ID to check
     * @return true if the question exists, false otherwise
     */
    public boolean existsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        boolean exists = questionRepository.existsById(id);
        logger.debug("Question existence check for ID {}: {}", id, exists);
        return exists;
    }

    /**
     * Retrieves the subject and lesson names for a given paper ID.
     * 
     * @param paperId the ID of the paper
     * @return a Map containing "subject" and "lesson" keys, or empty map if not
     *         found
     */
    public java.util.Map<String, String> getPaperContext(Long paperId) {
        Optional<Paper> paperOpt = paperRepository.findById(paperId);
        java.util.Map<String, String> context = new java.util.HashMap<>();

        if (paperOpt.isPresent()) {
            Paper paper = paperOpt.get();
            if (paper.getBundle() != null) {
                if (paper.getBundle().getSubject() != null) {
                    context.put("subject", paper.getBundle().getSubject().getName());
                }
                if (paper.getBundle().getLesson() != null) {
                    context.put("lesson", paper.getBundle().getLesson().getName());
                }
            }
        }
        return context;
    }

    /**
     * Retrieves the subject and lesson names for a given question ID.
     * 
     * @param questionId the ID of the question
     * @return a Map containing "subject" and "lesson" keys, or empty map if not
     *         found
     */
    public java.util.Map<String, String> getQuestionContext(Long questionId) {
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        java.util.Map<String, String> context = new java.util.HashMap<>();

        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();

            // Get lesson from question directly
            if (question.getLesson() != null) {
                context.put("lesson", question.getLesson().getName());
                logger.warn("[CONTEXT DEBUG] Found lesson for question {}: {}", questionId,
                        question.getLesson().getName());
            } else {
                logger.warn("[CONTEXT DEBUG] No lesson set for question {}", questionId);
            }

            // Get subject from paper
            if (question.getPaper() != null) {
                Paper paper = question.getPaper();

                // First try paper's direct subject
                if (paper.getSubject() != null) {
                    context.put("subject", paper.getSubject().getName());
                    logger.warn("[CONTEXT DEBUG] Found subject from paper for question {}: {}", questionId,
                            paper.getSubject().getName());
                } else if (paper.getBundle() != null && paper.getBundle().getSubject() != null) {
                    // Fallback to bundle subject
                    context.put("subject", paper.getBundle().getSubject().getName());
                    logger.warn("[CONTEXT DEBUG] Found subject from bundle for question {}: {}", questionId,
                            paper.getBundle().getSubject().getName());
                } else {
                    logger.warn("[CONTEXT DEBUG] No subject found for question {}", questionId);
                }
            }
        }

        logger.info("Question context for ID {}: subject='{}', lesson='{}'",
                questionId, context.get("subject"), context.get("lesson"));

        return context;
    }
}