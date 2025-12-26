package com.eduapp.backend.controller;

import com.eduapp.backend.dto.StudentAnswerDto;
import com.eduapp.backend.mapper.StudentAnswerMapper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.StudentAnswer;
import com.eduapp.backend.service.StudentAnswerService;
import com.eduapp.backend.service.QuestionService;
import com.eduapp.backend.service.AIService;
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
    private final StudentAnswerService studentAnswerService;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionService questionService;
    private final FileStorageService fileStorageService;

    public StudentAnswerController(
            StudentAnswerService studentAnswerService,
            StudentAnswerMapper studentAnswerMapper,
            AIService aiService,
            QuestionService questionService,
            FileStorageService fileStorageService) {
        this.studentAnswerService = studentAnswerService;
        this.studentAnswerMapper = studentAnswerMapper;
        this.aiService = aiService;
        this.questionService = questionService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/extract-from-image")
    public ResponseEntity<Map<String, String>> extractFromImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "questionId", required = false) Long questionId,
            @RequestParam(value = "answerId", required = false) Long answerId,
            @RequestParam(value = "subject", required = false) String subjectName) {
        try {
            logger.info("Received request to extract text from student answer image");

            String lessonName = null;

            // Check upload limit if answerId is provided
            if (answerId != null) {
                Optional<StudentAnswer> existingAnswer = studentAnswerService.findById(answerId);
                if (existingAnswer.isPresent()) {
                    StudentAnswer answer = existingAnswer.get();
                    if (!answer.canUpload()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Upload limit reached. Maximum " +
                                        StudentAnswer.MAX_UPLOADS_PER_QUESTION + " uploads allowed per question."));
                    }
                }
            }

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

            // Extract text with subject and lesson context
            String extractedText = aiService.extractTextFromImage(file, subjectName, lessonName);

            logger.warn("AI_SERVICE_DEBUG: Extracted text from image: '{}' (subject: {}, lesson: {})",
                    (extractedText != null && !extractedText.isEmpty())
                            ? extractedText.substring(0, Math.min(extractedText.length(), 50)) + "..."
                            : "EMPTY",
                    subjectName, lessonName);

            // Increment upload count if answerId provided
            if (answerId != null) {
                studentAnswerService.findById(answerId).ifPresent(answer -> {
                    answer.incrementUploadCount();
                    studentAnswerService.save(answer);
                });
            }

            return ResponseEntity.ok(Map.of(
                    "extractedText", extractedText,
                    "imageUrl", imageUrl));
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
}