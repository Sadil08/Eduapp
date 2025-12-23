package com.eduapp.backend.controller;

import com.eduapp.backend.dto.QuestionDto;
import com.eduapp.backend.mapper.QuestionMapper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.service.QuestionService;
import com.eduapp.backend.service.AIService;
import com.eduapp.backend.service.FileStorageService;
import com.eduapp.backend.service.QuestionModelAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Question entities.
 * Provides RESTful endpoints for CRUD operations on questions.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final QuestionMapper questionMapper;
    private final AIService aiService;
    private final FileStorageService fileStorageService;
    private final QuestionModelAnswerService modelAnswerService;

    public QuestionController(
        QuestionService questionService, 
        QuestionMapper questionMapper, 
        AIService aiService,
        FileStorageService fileStorageService,
        QuestionModelAnswerService modelAnswerService
    ) {
        this.questionService = questionService;
        this.questionMapper = questionMapper;
        this.aiService = aiService;
        this.fileStorageService = fileStorageService;
        this.modelAnswerService = modelAnswerService;
    }

    @PostMapping("/extract-from-image")
    public ResponseEntity<java.util.Map<String, String>> extractFromImage(
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
        @RequestParam(value = "subject", required = false) String subjectName,
        @RequestParam(value = "paperId", required = false) Long paperId
    ) {
        try {
            logger.info("Received request to extract text from image (subject: {}, paperId: {})", subjectName, paperId);
            
            String lessonName = null;

            // If subject not provided but paperId is, try to fetch from DB
            if (paperId != null) {
                java.util.Map<String, String> context = questionService.getPaperContext(paperId);
                if (subjectName == null) {
                    subjectName = context.get("subject");
                }
                lessonName = context.get("lesson");
            }
            
            // Store the file
            String imageUrl = fileStorageService.storeFile(file, "questions");
            
            // Extract text with subject and lesson context
            String extractedText = aiService.extractTextFromImage(file, subjectName, lessonName);
            
            return ResponseEntity.ok(java.util.Map.of(
                "extractedText", extractedText,
                "imageUrl", imageUrl
            ));
        } catch (Exception e) {
            logger.error("Failed to process image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions() {
        logger.info("Received request to get all questions");
        List<Question> questions = questionService.findAll();
        List<QuestionDto> dtos = questionMapper.toDtoList(questions);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
        logger.info("Received request to get question with ID: {}", id);
        Optional<Question> question = questionService.findById(id);
        if (question.isPresent()) {
            QuestionDto dto = questionMapper.toDto(question.get());
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(@RequestBody QuestionDto dto) {
        logger.info("Received request to create question");
        Question question = questionMapper.toEntity(dto);
        Question savedQuestion = questionService.save(question);
        QuestionDto savedDto = questionMapper.toDto(savedQuestion);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Long id, @RequestBody QuestionDto dto) {
        logger.info("Received request to update question with ID: {}", id);
        Optional<Question> existingQuestion = questionService.findById(id);
        if (existingQuestion.isPresent()) {
            Question question = existingQuestion.get();
            question.setText(dto.getText());
            question.setCorrectAnswerText(dto.getCorrectAnswerText());
            question.setImageUrl(dto.getImageUrl());
            question.setModelAnswerImageUrl(dto.getModelAnswerImageUrl());
            question.setExtractedText(dto.getExtractedText());
            question.setRequiresImageDisplay(dto.getRequiresImageDisplay());
            question.setHideQuestionText(dto.getHideQuestionText());
            question.setAllowImageAnswer(dto.getAllowImageAnswer());
            question.setAnswerTypeHint(dto.getAnswerTypeHint());
            question.setMarks(dto.getMarks());
            Question updatedQuestion = questionService.save(question);
            QuestionDto updatedDto = questionMapper.toDto(updatedQuestion);
            return ResponseEntity.ok(updatedDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        logger.info("Received request to delete question with ID: {}", id);
        if (questionService.existsById(id)) {
            questionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}