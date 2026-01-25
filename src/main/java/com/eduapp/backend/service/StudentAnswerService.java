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

        // Check for existing answer to avoid duplicate key violation
        Long attemptIdToCheck = (answer.getAttempt() != null) ? answer.getAttempt().getId() : null;
        Long questionIdToCheck = (answer.getQuestion() != null) ? answer.getQuestion().getId() : null;
        
        logger.info("UPSERT CHECK: answerId={}, attemptId={}, questionId={}", 
            answer.getId(), attemptIdToCheck, questionIdToCheck);
        
        if (answer.getId() == null && attemptIdToCheck != null && questionIdToCheck != null) {
            
            logger.info("Checking for existing answer with attemptId={} and questionId={}",
                attemptIdToCheck, questionIdToCheck);
            
            Optional<StudentAnswer> existing = studentAnswerRepository.findByAttemptIdAndQuestionId(
                attemptIdToCheck, questionIdToCheck);
            
            logger.info("Existing answer found: {}", existing.isPresent());
                
            if (existing.isPresent()) {
                logger.info("Found existing answer ID {} for attempt {} question {}. Merging data.",
                    existing.get().getId(), attemptIdToCheck, questionIdToCheck);
                
                // Merge data from incoming answer to existing managed entity
                StudentAnswer existingAnswer = existing.get();
                existingAnswer.setAnswerText(answer.getAnswerText());
                existingAnswer.setImageUrl(answer.getImageUrl());
                existingAnswer.setExtractedText(answer.getExtractedText());
                existingAnswer.setExtractionConfidence(answer.getExtractionConfidence());
                existingAnswer.setSelectedOption(answer.getSelectedOption());
                existingAnswer.setSubmittedAt(answer.getSubmittedAt());
                existingAnswer.setMarksAwarded(answer.getMarksAwarded());
                existingAnswer.setAiFeedback(answer.getAiFeedback());
                existingAnswer.setUploadCount(answer.getUploadCount());
                existingAnswer.setIsDraft(answer.getIsDraft());
                
                // Save the existing (managed) entity
                StudentAnswer savedAnswer = studentAnswerRepository.save(existingAnswer);
                logger.info("Student answer updated with ID: {}", savedAnswer.getId());
                return savedAnswer;
            }
        }

        // No existing answer found - save as new
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

    /**
     * Save draft answers for an attempt (autosave functionality).
     * Replaces existing drafts with new ones.
     * 
     * @param attemptId the attempt ID
     * @param draftAnswers list of draft answers to save
     */
    @org.springframework.transaction.annotation.Transactional
    public void saveDraftAnswers(Long attemptId, List<StudentAnswer> draftAnswers) {
        logger.info("Saving {} draft answers for attempt {}", draftAnswers.size(), attemptId);
        
        // Delete existing drafts
        studentAnswerRepository.deleteByAttemptIdAndIsDraft(attemptId, true);
        logger.debug("Deleted existing draft answers for attempt {}", attemptId);
        
        // Verify attempt exists
        StudentPaperAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        
        // Save new drafts
        for (StudentAnswer answer : draftAnswers) {
            answer.setAttempt(attempt);
            answer.setIsDraft(true);
            answer.setSubmittedAt(java.time.LocalDateTime.now());
            
            // Validate question exists
            if (answer.getQuestion() != null && answer.getQuestion().getId() != null) {
                questionRepository.findById(answer.getQuestion().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found: " + answer.getQuestion().getId()));
            }
            
            studentAnswerRepository.save(answer);
        }
        
        logger.info("Successfully saved {} draft answers for attempt {}", draftAnswers.size(), attemptId);
    }

    /**
     * Retrieve draft answers for an attempt.
     * 
     * @param attemptId the attempt ID
     * @return list of draft answers
     */
    public List<StudentAnswer> getDraftAnswers(Long attemptId) {
        logger.info("Retrieving draft answers for attempt {}", attemptId);
        List<StudentAnswer> drafts = studentAnswerRepository.findByAttemptIdAndIsDraft(attemptId, true);
        logger.info("Found {} draft answers for attempt {}", drafts.size(), attemptId);
        return drafts;
    }

    /**
     * Mark all draft answers as final submission.
     * Called when student submits the paper.
     * 
     * @param attemptId the attempt ID
     */
    @org.springframework.transaction.annotation.Transactional
    public void finalizeDraftAnswers(Long attemptId) {
        logger.info("Finalizing draft answers for attempt {}", attemptId);
        List<StudentAnswer> drafts = studentAnswerRepository.findByAttemptIdAndIsDraft(attemptId, true);
        
        for (StudentAnswer draft : drafts) {
            draft.setIsDraft(false);
            draft.setSubmittedAt(java.time.LocalDateTime.now());
            studentAnswerRepository.save(draft);
        }
        
        logger.info("Finalized {} draft answers for attempt {}", drafts.size(), attemptId);
    }
}