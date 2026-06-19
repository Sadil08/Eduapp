package com.eduapp.backend.service;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * School exam sitting + marking + teacher override (WP-7). Marking reuses the platform
 * approach: MCQ is rule-based (deterministic) here; extended answers are scored by the AI
 * pipeline (same {@code aiMark}/{@code aiFeedback} fields). A teacher can override any AI
 * mark with a mandatory justification note — the override supersedes the AI mark in the
 * gradebook (PRODUCT_BUSINESS_PLAN.md §5.3).
 */
@Service
public class SchoolAttemptService {

    private final SchoolPaperRepository paperRepository;
    private final SchoolPaperAttemptRepository attemptRepository;
    private final SchoolStudentAnswerRepository answerRepository;
    private final SchoolEnrolmentRepository enrolmentRepository;
    private final QuestionRepository questionRepository;
    private final FileStorageService fileStorageService;
    private final AIService aiService;

    public SchoolAttemptService(SchoolPaperRepository paperRepository,
            SchoolPaperAttemptRepository attemptRepository,
            SchoolStudentAnswerRepository answerRepository,
            SchoolEnrolmentRepository enrolmentRepository,
            QuestionRepository questionRepository,
            FileStorageService fileStorageService,
            AIService aiService) {
        this.paperRepository = paperRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.questionRepository = questionRepository;
        this.fileStorageService = fileStorageService;
        this.aiService = aiService;
    }

    private Long requireTenant() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context — school-tier user required");
        }
        return schoolId;
    }

    private SchoolPaperAttempt requireOwnAttempt(Long attemptId, Long studentId) {
        SchoolPaperAttempt attempt = attemptRepository.findByIdAndSchoolId(attemptId, requireTenant())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        if (!attempt.getStudentId().equals(studentId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your attempt");
        }
        return attempt;
    }

    @Transactional
    public SchoolPaperAttempt start(Long studentId, Long paperId) {
        Long schoolId = requireTenant();
        SchoolPaper paper = paperRepository.findByIdAndSchoolId(paperId, schoolId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found"));

        if (!paper.isOpenForAttempts(LocalDateTime.now())) {
            // Covers: not ASSIGNED, before the window opens, or after it closes.
            throw new IllegalStateException("This paper is not open for attempts right now");
        }
        // BT-9.5: attempts require an ACTIVE (teacher-approved) enrolment.
        if (paper.getClassId() == null
                || !enrolmentRepository.existsByClassIdAndStudentIdAndStatus(
                        paper.getClassId(), studentId, com.eduapp.backend.model.SchoolEnrolmentStatus.ACTIVE)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Your enrolment in this class is not active yet");
        }

        return attemptRepository.findBySchoolPaperIdAndStudentId(paperId, studentId)
                .map(existing -> {
                    if (existing.getStatus() != AttemptStatus.IN_PROGRESS) {
                        throw new IllegalStateException("You have already submitted this paper");
                    }
                    return existing;
                })
                .orElseGet(() -> attemptRepository.save(
                        new SchoolPaperAttempt(schoolId, studentId, paperId, paper.getClassId())));
    }

    @Transactional
    public SchoolStudentAnswer answer(Long studentId, Long attemptId, Long questionId, String answerText) {
        SchoolPaperAttempt attempt = requireOwnAttempt(attemptId, studentId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attempt is no longer in progress");
        }
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!attempt.getSchoolPaperId().equals(q.getSchoolPaperId())) {
            throw new IllegalArgumentException("Question does not belong to this paper");
        }
        SchoolStudentAnswer ans = answerRepository.findByAttemptIdAndQuestionId(attemptId, questionId)
                .orElseGet(() -> new SchoolStudentAnswer(attemptId, questionId, null));
        ans.setAnswerText(answerText);
        return answerRepository.save(ans);
    }

    /**
     * Image-answer for a handwritten response: store the image, AI-extract its text, and use the
     * extracted text as the answer (the student can still edit it before submitting). Returns the
     * saved answer (incl. imageUrl + extractedText).
     */
    @Transactional
    public SchoolStudentAnswer answerWithImage(Long studentId, Long attemptId, Long questionId,
            org.springframework.web.multipart.MultipartFile file) {
        SchoolPaperAttempt attempt = requireOwnAttempt(attemptId, studentId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attempt is no longer in progress");
        }
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!attempt.getSchoolPaperId().equals(q.getSchoolPaperId())) {
            throw new IllegalArgumentException("Question does not belong to this paper");
        }
        String imageUrl;
        String extracted;
        try {
            imageUrl = fileStorageService.storeFile(file, "school-answers");
            extracted = aiService.extractTextFromImage(file, null, null, "answer");
        } catch (Exception e) {
            throw new IllegalStateException("Could not process the image: " + e.getMessage(), e);
        }
        SchoolStudentAnswer ans = answerRepository.findByAttemptIdAndQuestionId(attemptId, questionId)
                .orElseGet(() -> new SchoolStudentAnswer(attemptId, questionId, null));
        ans.setImageUrl(imageUrl);
        ans.setExtractedText(extracted);
        ans.setAnswerText(extracted); // marked text = extracted; student may overwrite before submit
        return answerRepository.save(ans);
    }

    @Transactional
    public SchoolPaperAttempt submit(Long studentId, Long attemptId) {
        SchoolPaperAttempt attempt = requireOwnAttempt(attemptId, studentId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Attempt already submitted");
        }
        int aiTotal = 0;
        for (SchoolStudentAnswer ans : answerRepository.findByAttemptId(attemptId)) {
            Question q = questionRepository.findById(ans.getQuestionId()).orElse(null);
            Integer awarded = markAnswer(q, ans);
            ans.setMarksAwarded(awarded);
            answerRepository.save(ans);
            if (awarded != null) {
                aiTotal += awarded;
            }
        }
        attempt.setAiMark(aiTotal);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTimeTakenMinutes((int) Duration.between(attempt.getStartedAt(), LocalDateTime.now()).toMinutes());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        return attemptRepository.save(attempt);
    }

    /** Rule-based MCQ marking (deterministic). Extended answers are left for the AI pipeline. */
    private Integer markAnswer(Question q, SchoolStudentAnswer ans) {
        if (q == null) {
            return null;
        }
        if (q.getType() == QuestionType.MCQ) {
            int max = q.getMarks() != null ? q.getMarks() : 1;
            boolean correct = q.getCorrectAnswerText() != null && ans.getAnswerText() != null
                    && q.getCorrectAnswerText().trim().equalsIgnoreCase(ans.getAnswerText().trim());
            return correct ? max : 0;
        }
        return null; // essay/extended: scored by AI pipeline / teacher
    }

    // ---------- Teacher review ----------

    @Transactional(readOnly = true)
    public List<SchoolPaperAttempt> listForPaper(Long paperId) {
        return attemptRepository.findBySchoolPaperIdAndSchoolId(paperId, requireTenant());
    }

    /** Per-question answers + AI mark/feedback + marking scheme for a teacher to review one attempt. */
    @Transactional(readOnly = true)
    public List<com.eduapp.backend.dto.AttemptAnswerReviewDto> listAnswersForReview(Long attemptId) {
        // Tenant guard: the attempt must belong to the caller's school.
        attemptRepository.findByIdAndSchoolId(attemptId, requireTenant())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found in this school"));
        return answerRepository.findByAttemptId(attemptId).stream().map(ans -> {
            Question q = questionRepository.findById(ans.getQuestionId()).orElse(null);
            return new com.eduapp.backend.dto.AttemptAnswerReviewDto(
                    ans.getQuestionId(),
                    q != null ? q.getText() : null,
                    q != null ? q.getMarks() : null,
                    q != null ? q.getCorrectAnswerText() : null,
                    ans.getAnswerText(),
                    ans.getMarksAwarded(),
                    ans.getAiFeedback());
        }).toList();
    }

    @Transactional
    public SchoolPaperAttempt teacherOverride(Long attemptId, Integer overrideMark, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("A justification note is required to override a mark");
        }
        SchoolPaperAttempt attempt = attemptRepository.findByIdAndSchoolId(attemptId, requireTenant())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found in this school"));
        attempt.setTeacherOverrideMark(overrideMark);
        attempt.setTeacherOverrideNote(note);
        attempt.setTeacherReviewedAt(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.GRADED);
        return attemptRepository.save(attempt);
    }

    // ---------- Student result (gated by results_released) ----------

    @Transactional(readOnly = true)
    public SchoolPaperAttempt getOwnAttempt(Long studentId, Long attemptId) {
        return requireOwnAttempt(attemptId, studentId);
    }

    @Transactional(readOnly = true)
    public boolean areResultsReleased(Long schoolPaperId) {
        return paperRepository.findByIdAndSchoolId(schoolPaperId, requireTenant())
                .map(SchoolPaper::isResultsReleased)
                .orElse(false);
    }
}
