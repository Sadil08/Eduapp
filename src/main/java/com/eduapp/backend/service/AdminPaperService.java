package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.SubjectRepository;
import com.eduapp.backend.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for admin paper and question management.
 * Provides CRUD operations for papers and their questions.
 */
@Service
public class AdminPaperService {

    private static final Logger logger = LoggerFactory.getLogger(AdminPaperService.class);

    private final PaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final StudentPaperAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final PaperBundleRepository paperBundleRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public AdminPaperService(PaperRepository paperRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            StudentPaperAttemptRepository attemptRepository,
            UserRepository userRepository,
            PaperBundleRepository paperBundleRepository,
            SubjectRepository subjectRepository,
            LessonRepository lessonRepository) {
        this.paperRepository = paperRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.paperBundleRepository = paperBundleRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Get all papers with statistics
     */
    public List<AdminPaperDto> getAllPapers() {
        logger.info("Fetching all papers with statistics");

        List<Paper> papers = paperRepository.findAll();

        return papers.stream().map(this::mapToAdminDto).collect(Collectors.toList());
    }

    /**
     * Get paper details with questions
     */
    public AdminPaperDto getPaperDetails(Long id) {
        logger.info("Fetching paper details for ID: {}", id);

        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        return mapToAdminDto(paper);
    }

    /**
     * Create a new paper
     */
    @Transactional
    public Paper createPaper(PaperDto dto, Long adminId) {
        logger.info("Creating new paper: {} by admin ID: {}", dto.getName(), adminId);

        Paper paper = new Paper();
        paper.setName(dto.getName());
        paper.setDescription(dto.getDescription());
        paper.setType(dto.getType());
        paper.setMaxFreeAttempts(dto.getMaxFreeAttempts());
        paper.setTotalMarks(dto.getTotalMarks());
        
        // Fetch and set bundles if bundleIds are provided
        if (dto.getBundleIds() != null && !dto.getBundleIds().isEmpty()) {
            List<PaperBundle> bundles = paperBundleRepository.findAllById(dto.getBundleIds());
            paper.setBundles(bundles);
        }
        
        // Set subject if provided
        if (dto.getSubjectId() != null) {
            subjectRepository.findById(dto.getSubjectId()).ifPresent(paper::setSubject);
        }
        // Set createdBy only if adminId is provided
        if (adminId != null) {
            userRepository.findById(adminId).ifPresent(paper::setCreatedBy);
        }

        Paper saved = paperRepository.save(paper);
        logger.info("Paper created with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update an existing paper
     */
    @Transactional
    public Paper updatePaper(Long id, PaperDto dto) {
        logger.info("Updating paper ID: {}", id);

        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        paper.setName(dto.getName());
        paper.setDescription(dto.getDescription());
        paper.setType(dto.getType());
        paper.setMaxFreeAttempts(dto.getMaxFreeAttempts());
        paper.setTotalMarks(dto.getTotalMarks());
        
        // Update bundles if provided
        if (dto.getBundleIds() != null) {
            List<PaperBundle> bundles = paperBundleRepository.findAllById(dto.getBundleIds());
            paper.setBundles(bundles);
        }
        
        // Update subject if provided
        if (dto.getSubjectId() != null) {
            subjectRepository.findById(dto.getSubjectId()).ifPresent(paper::setSubject);
        } else {
            paper.setSubject(null);
        }

        Paper updated = paperRepository.save(paper);
        logger.info("Paper updated: {}", updated.getName());

        return updated;
    }

    /**
     * Delete a paper (cascade deletes questions)
     */
    @Transactional
    public void deletePaper(Long id) {
        logger.info("Deleting paper ID: {}", id);

        if (!paperRepository.existsById(id)) {
            throw new IllegalArgumentException("Paper not found");
        }

        paperRepository.deleteById(id);
        logger.info("Paper deleted: {}", id);
    }

    /**
     * Add a question to a paper
     */
    @Transactional
    public Question addQuestion(Long paperId, QuestionCreateDto dto) {
        logger.info("Adding question to paper ID: {}", paperId);

        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        Question question = new Question();
        question.setPaper(paper);
        question.setText(dto.getText());
        question.setType(dto.getType());
        question.setCorrectAnswerText(dto.getCorrectAnswerText());
        question.setMarks(dto.getMarks());
        question.setImageUrl(dto.getImageUrl());
        question.setModelAnswerImageUrl(dto.getModelAnswerImageUrl());
        question.setExtractedText(dto.getExtractedText());
        question.setRequiresImageDisplay(dto.getRequiresImageDisplay());
        question.setHideQuestionText(dto.getHideQuestionText());
        question.setAllowImageAnswer(dto.getAllowImageAnswer());
        question.setAnswerTypeHint(dto.getAnswerTypeHint());
        // Set lesson if provided
        if (dto.getLessonId() != null) {
            lessonRepository.findById(dto.getLessonId()).ifPresent(question::setLesson);
        }

        Question savedQuestion = questionRepository.save(question);

        // Add options if provided
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            for (QuestionOptionDto optionDto : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestion(savedQuestion);
                option.setText(optionDto.getText());
                option.setIsCorrect(optionDto.getIsCorrect());
                optionRepository.save(option);
            }
        }

        logger.info("Question added to paper: {}", paper.getName());
        return savedQuestion;
    }

    /**
     * Update a question
     */
    @Transactional
    public Question updateQuestion(Long paperId, Long questionId, QuestionCreateDto dto) {
        logger.info("Updating question ID: {} in paper ID: {}", questionId, paperId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (!question.getPaper().getId().equals(paperId)) {
            throw new IllegalArgumentException("Question does not belong to this paper");
        }

        question.setText(dto.getText());
        question.setType(dto.getType());
        question.setCorrectAnswerText(dto.getCorrectAnswerText());
        question.setMarks(dto.getMarks());
        question.setImageUrl(dto.getImageUrl());
        question.setModelAnswerImageUrl(dto.getModelAnswerImageUrl());
        question.setExtractedText(dto.getExtractedText());
        question.setRequiresImageDisplay(dto.getRequiresImageDisplay());
        question.setHideQuestionText(dto.getHideQuestionText());
        question.setAllowImageAnswer(dto.getAllowImageAnswer());
        question.setAnswerTypeHint(dto.getAnswerTypeHint());
        // Update lesson if provided
        if (dto.getLessonId() != null) {
            lessonRepository.findById(dto.getLessonId()).ifPresent(question::setLesson);
        } else {
            question.setLesson(null);
        }

        // Update options logic
        if (dto.getOptions() != null && dto.getType() == com.eduapp.backend.model.QuestionType.MCQ) {
            List<QuestionOption> currentOptions = question.getOptions();
            List<QuestionOptionDto> newOptionsDtos = dto.getOptions();

            // 1. Update existing options and identify new ones
            // We need to keep track of which existing options were updated so we can delete
            // the rest
            java.util.List<Long> updatedOptionIds = new java.util.ArrayList<>();

            for (QuestionOptionDto optionDto : newOptionsDtos) {
                if (optionDto.getId() != null) {
                    // Update existing
                    currentOptions.stream()
                            .filter(opt -> opt.getId().equals(optionDto.getId()))
                            .findFirst()
                            .ifPresent(opt -> {
                                opt.setText(optionDto.getText());
                                opt.setIsCorrect(optionDto.getIsCorrect());
                                updatedOptionIds.add(opt.getId());
                            });
                } else {
                    // Add new option
                    QuestionOption newOption = new QuestionOption();
                    newOption.setQuestion(question);
                    newOption.setText(optionDto.getText());
                    newOption.setIsCorrect(optionDto.getIsCorrect());
                    currentOptions.add(newOption);
                }
            }

            // 2. Remove options that are no longer in the list
            // Note: This might still fail if you try to delete an option that has student
            // answers.
            // But at least you can now UPDATE the correct answer for existing options
            // without error.
            currentOptions.removeIf(opt -> opt.getId() != null && !updatedOptionIds.contains(opt.getId()));
        }

        Question updated = questionRepository.save(question);
        logger.info("Question updated: {}", updated.getId());

        return updated;
    }

    /**
     * Delete a question
     */
    @Transactional
    public void deleteQuestion(Long paperId, Long questionId) {
        logger.info("Deleting question ID: {} from paper ID: {}", questionId, paperId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (!question.getPaper().getId().equals(paperId)) {
            throw new IllegalArgumentException("Question does not belong to this paper");
        }

        questionRepository.deleteById(questionId);
        logger.info("Question deleted: {}", questionId);
    }

    /**
     * Map Paper to AdminPaperDto with statistics
     */
    private AdminPaperDto mapToAdminDto(Paper paper) {
        AdminPaperDto dto = new AdminPaperDto();

        // Basic fields
        dto.setId(paper.getId());
        dto.setName(paper.getName());
        dto.setDescription(paper.getDescription());
        dto.setType(paper.getType());
        dto.setBundleIds(paper.getBundles().stream().map(PaperBundle::getId).collect(java.util.stream.Collectors.toList()));
        dto.setMaxFreeAttempts(paper.getMaxFreeAttempts());
        dto.setTotalMarks(paper.getTotalMarks());

        // Admin fields
        dto.setCreatedAt(paper.getCreatedAt());
        dto.setUpdatedAt(paper.getUpdatedAt());
        dto.setCreatedBy(paper.getCreatedBy() != null ? paper.getCreatedBy().getId() : null);
        dto.setSubjectId(paper.getSubject() != null ? paper.getSubject().getId() : null);

        // Statistics
        int totalAttempts = (int) attemptRepository.countByPaperId(paper.getId());
        dto.setTotalAttempts(totalAttempts);

        // Calculate average score
        Double avgScore = attemptRepository.findAverageScoreByPaperId(paper.getId());
        dto.setAverageScore(avgScore != null ? avgScore : 0.0);

        // Add questions if present
        if (paper.getQuestions() != null) {
            List<QuestionDto> questions = paper.getQuestions().stream()
                    .map(this::mapQuestionToDto)
                    .collect(Collectors.toList());
            dto.setQuestions(questions);
        }

        return dto;
    }

    /**
     * Map Question to QuestionDto
     */
    private QuestionDto mapQuestionToDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setPaperId(question.getPaper().getId());
        dto.setText(question.getText());
        dto.setType(question.getType());
        dto.setCorrectAnswerText(question.getCorrectAnswerText());
        dto.setMarks(question.getMarks());
        dto.setImageUrl(question.getImageUrl());
        dto.setModelAnswerImageUrl(question.getModelAnswerImageUrl());
        dto.setExtractedText(question.getExtractedText());
        dto.setRequiresImageDisplay(question.getRequiresImageDisplay());
        dto.setHideQuestionText(question.getHideQuestionText());
        dto.setAllowImageAnswer(question.getAllowImageAnswer());
        dto.setAnswerTypeHint(question.getAnswerTypeHint());

        // Set lesson context
        if (question.getLesson() != null) {
            dto.setLessonId(question.getLesson().getId());
            dto.setLessonName(question.getLesson().getName());
        }

        if (question.getOptions() != null) {
            List<QuestionOptionDto> options = question.getOptions().stream()
                    .map(opt -> {
                        QuestionOptionDto optDto = new QuestionOptionDto();
                        optDto.setId(opt.getId());
                        optDto.setText(opt.getText());
                        optDto.setIsCorrect(opt.getIsCorrect());
                        return optDto;
                    })
                    .collect(Collectors.toList());
            dto.setOptions(options);
        }

        return dto;
    }
}
