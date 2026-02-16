package com.eduapp.backend.controller;

import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.dto.StudentAnswerDto;
import com.eduapp.backend.mapper.StudentAnswerMapper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.service.StudentAnswerService;
import com.eduapp.backend.service.QuestionService;
import com.eduapp.backend.service.AIService;
import com.eduapp.backend.service.AIAnalysisService;
import com.eduapp.backend.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing StudentAnswer entities.
 */
@RestController
@RequestMapping("/api/student-answers")
public class StudentAnswerController {

    private static final Logger logger = LoggerFactory.getLogger(StudentAnswerController.class);

    private final AIService aiService;
    private final AIAnalysisService aiAnalysisService;
    private final StudentAnswerService studentAnswerService;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionService questionService;
    private final FileStorageService fileStorageService;
    private final com.eduapp.backend.service.ExtractionTrackingService extractionTrackingService;
    private final StudentPaperAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public StudentAnswerController(
            StudentAnswerService studentAnswerService,
            StudentAnswerMapper studentAnswerMapper,
            AIService aiService,
            QuestionService questionService,
            FileStorageService fileStorageService,
            com.eduapp.backend.service.ExtractionTrackingService extractionTrackingService,
            StudentPaperAttemptRepository attemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            AIAnalysisService aiAnalysisService) {
        this.studentAnswerService = studentAnswerService;
        this.studentAnswerMapper = studentAnswerMapper;
        this.aiService = aiService;
        this.questionService = questionService;
        this.fileStorageService = fileStorageService;
        this.extractionTrackingService = extractionTrackingService;
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/extract-from-image")
    public ResponseEntity<Map<String, Object>> extractFromImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("attemptId") Long attemptId,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "answerId", required = false) Long answerId,
            @RequestParam(value = "subject", required = false) String subjectName) {
        try {
            logger.info("Received extraction request for attempt: {}, question: {}", attemptId, questionId);

            // CHECK EXTRACTION LIMIT FIRST
            if (!extractionTrackingService.canExtract(attemptId, questionId)) {
                int used = extractionTrackingService.getExtractionCount(attemptId, questionId);
                int max = 2; // Default max extractions
                logger.warn("Extraction limit exceeded for attempt {} question {}: {}/{}",
                        attemptId, questionId, used, max);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Extraction limit reached",
                                "message",
                                String.format("You have used all %d extraction attempts for this question", max),
                                "extractionsUsed", used,
                                "extractionsMax", max));
            }

            String lessonName = null;

            // Validate if question allows image answers and fetch context
            if (questionId != null) {
                Optional<Question> question = questionService.findById(questionId);
                if (question.isPresent()) {
                    if (Boolean.FALSE.equals(question.get().getAllowImageAnswer())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Image upload not allowed for this question"));
                    }

                    // Fetch context to get lesson name and subject (if missing)
                    Map<String, String> context = questionService.getQuestionContext(questionId);
                    if (subjectName == null) {
                        subjectName = context.get("subject");
                    }
                    lessonName = context.get("lesson");
                }
            }

            // Store the file
            String imageUrl = fileStorageService.storeFile(file, "student-answers");

            // Extract text with subject, lesson context, and mark as handwritten
            String extractedText = aiService.extractTextFromImage(file, subjectName, lessonName, "handwritten");

            logger.info("Extraction successful for attempt {} question {} - extracted {} characters",
                    attemptId, questionId, extractedText != null ? extractedText.length() : 0);

            // RECORD SUCCESSFUL EXTRACTION
            extractionTrackingService.recordExtraction(attemptId, questionId);
            int remaining = extractionTrackingService.getRemainingExtractions(attemptId, questionId);
            int used = extractionTrackingService.getExtractionCount(attemptId, questionId);

            return ResponseEntity.ok(Map.of(
                    "extractedText", extractedText,
                    "imageUrl", imageUrl,
                    "extractionsRemaining", remaining,
                    "extractionsUsed", used,
                    "extractionsMax", 2));
        } catch (com.eduapp.backend.exception.ExtractionLimitExceededException e) {
            logger.error("Extraction limit exceeded: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "Extraction limit exceeded",
                            "message", e.getMessage(),
                            "extractionsUsed", e.getExtractionsUsed(),
                            "extractionsMax", e.getExtractionsMax()));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to process image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process image: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<StudentAnswerDto>> getAllStudentAnswers() {
        logger.info("Received request to get all student answers");
        List<StudentAnswer> answers = studentAnswerService.findAll();
        List<StudentAnswerDto> dtos = studentAnswerMapper.toDtoList(answers);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentAnswerDto> getStudentAnswerById(@PathVariable Long id) {
        logger.info("Received request to get student answer with ID: {}", id);
        Optional<StudentAnswer> answer = studentAnswerService.findById(id);
        if (answer.isPresent()) {
            StudentAnswerDto dto = studentAnswerMapper.toDto(answer.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<StudentAnswerDto> createStudentAnswer(@RequestBody StudentAnswerDto dto) {
        logger.info("Received request to create student answer");

        // Validate image upload permission if imageUrl is provided
        if (dto.getImageUrl() != null && !dto.getImageUrl().isEmpty()) {
            Optional<Question> question = questionService.findById(dto.getQuestionId());
            if (question.isPresent() && Boolean.FALSE.equals(question.get().getAllowImageAnswer())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }

        StudentAnswer answer = studentAnswerMapper.toEntity(dto);
        StudentAnswer savedAnswer = studentAnswerService.save(answer);

        // Trigger AI marking if answer has content
        // LEGACY: Disabled as we are moving to full-attempt analysis via AIAnalysisService
        /* 
        if (savedAnswer.getAnswerText() != null || savedAnswer.getExtractedText() != null) {
            try {
                Map<String, Object> analysis = aiService.analyzeAnswer(savedAnswer);
                savedAnswer.setAiFeedback((String) analysis.get("feedback"));
                savedAnswer.setMarksAwarded((Integer) analysis.get("marks"));
                savedAnswer = studentAnswerService.save(savedAnswer);
            } catch (Exception e) {
                logger.error("AI marking failed: {}", e.getMessage());
                // Continue without marking
            }
        }
        */

        StudentAnswerDto savedDto = studentAnswerMapper.toDto(savedAnswer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentAnswer(@PathVariable Long id) {
        logger.info("Received request to delete student answer with ID: {}", id);
        if (studentAnswerService.existsById(id)) {
            studentAnswerService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Save draft answers for an attempt (autosave).
     * Replaces existing drafts.
     */
    @PostMapping("/attempts/{attemptId}/save-draft")
    public ResponseEntity<?> saveDraftAnswers(
            @PathVariable Long attemptId,
            @RequestBody List<StudentAnswerDto> draftDtos) {
        try {
            logger.info("Saving {} draft answers for attempt {}", draftDtos.size(), attemptId);
            
            List<StudentAnswer> draftAnswers = studentAnswerMapper.toEntityList(draftDtos);
            studentAnswerService.saveDraftAnswers(attemptId, draftAnswers);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "saved", draftAnswers.size(),
                "message", "Draft answers saved successfully"
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Error saving draft answers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error saving draft answers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to save draft answers"));
        }
    }

    /**
     * Get draft answers for an attempt.
     */
    @GetMapping("/attempts/{attemptId}/draft-answers")
    public ResponseEntity<List<StudentAnswerDto>> getDraftAnswers(@PathVariable Long attemptId) {
        logger.info("Retrieving draft answers for attempt {}", attemptId);
        List<StudentAnswer> drafts = studentAnswerService.getDraftAnswers(attemptId);
        List<StudentAnswerDto> dtos = studentAnswerMapper.toDtoList(drafts);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retry AI analysis for a failed attempt.
     * Allows up to 3 submission attempts.
     */
    @PostMapping("/attempts/{attemptId}/retry-analysis")
    public ResponseEntity<?> retryAnalysis(@PathVariable Long attemptId) {
        try {
            logger.info("Retry analysis requested for attempt {}", attemptId);
            
            com.eduapp.backend.model.StudentPaperAttempt attempt = 
                attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
            
            // Check if retry is allowed (max 3 attempts)
            if (attempt.getSubmissionCount() >= 3) {
                logger.warn("Maximum retry attempts reached for attempt {}", attemptId);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Maximum retry attempts reached",
                    "maxRetries", 3,
                    "currentCount", attempt.getSubmissionCount()
                ));
            }
            
            // Reset error state
            attempt.setAnalysisError(null);
            attempt.setAnalysisAttempted(false);
            attempt.setAnalysisCompleted(false);
            attempt.setSubmissionCount(attempt.getSubmissionCount() + 1);
            attempt.setLastSubmissionTime(java.time.LocalDateTime.now());
            attemptRepository.save(attempt);
            
            logger.info("Attempt {} error state cleared, triggering reanalysis (attempt {}/3)", 
                        attemptId, attempt.getSubmissionCount());
            
            // Get finalized answers (not drafts)
            List<StudentAnswer> answers = studentAnswerRepository
                .findByAttemptIdAndIsDraft(attemptId, false);
            
            if (answers.isEmpty()) {
                logger.warn("No finalized answers found for attempt {}", attemptId);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No answers to analyze",
                    "message", "Please submit your answers first"
                ));
            }
            
            // Trigger AI analysis asynchronously
            try {
                aiAnalysisService.analyzeAttempt(attempt);
                logger.info("AI analysis triggered successfully for attempt {}", attemptId);
            } catch (Exception e) {
                logger.error("Failed to trigger AI analysis for attempt {}: {}", 
                            attemptId, e.getMessage());
                // Update attempt with new error
                attempt.setAnalysisError("Failed to start analysis: " + e.getMessage());
                attempt.setAnalysisAttempted(true);
                attempt.setAnalysisCompleted(false);
                attemptRepository.save(attempt);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Analysis resubmitted successfully",
                "attemptCount", attempt.getSubmissionCount(),
                "remainingAttempts", 3 - attempt.getSubmissionCount()
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid attempt ID: {}", attemptId);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrying analysis for attempt {}: {}", attemptId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retry analysis", "details", e.getMessage()));
        }
    }
}