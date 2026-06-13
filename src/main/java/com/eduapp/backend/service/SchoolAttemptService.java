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

    public SchoolAttemptService(SchoolPaperRepository paperRepository,
            SchoolPaperAttemptRepository attemptRepository,
            SchoolStudentAnswerRepository answerRepository,
            SchoolEnrolmentRepository enrolmentRepository,
            QuestionRepository questionRepository) {
        this.paperRepository = paperRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.questionRepository = questionRepository;
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
        if (paper.getClassId() == null
                || !enrolmentRepository.existsByClassIdAndStudentId(paper.getClassId(), studentId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not enrolled in this paper's class");
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
