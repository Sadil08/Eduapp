package com.eduapp.backend.service;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.repository.SchoolPaperRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * School-tier paper lifecycle, all scoped to the current tenant. Questions are stored as
 * shared {@link Question} rows tagged with {@code school_paper_id}, so the existing AI
 * extraction/marking pipeline applies to school papers without modification.
 */
@Service
public class SchoolPaperService {

    private final SchoolPaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final SchoolClassRepository classRepository;

    public SchoolPaperService(SchoolPaperRepository paperRepository,
            QuestionRepository questionRepository,
            SchoolClassRepository classRepository) {
        this.paperRepository = paperRepository;
        this.questionRepository = questionRepository;
        this.classRepository = classRepository;
    }

    private Long requireTenant() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context — school-tier user required");
        }
        return schoolId;
    }

    private SchoolPaper requireOwnedPaper(Long paperId) {
        return paperRepository.findByIdAndSchoolId(paperId, requireTenant())
                .orElseThrow(() -> new IllegalArgumentException("Paper not found in this school"));
    }

    @Transactional
    public SchoolPaper createDraft(String name, String description, PaperType type, Long createdBy) {
        return paperRepository.save(new SchoolPaper(requireTenant(), name, description, type, createdBy));
    }

    @Transactional(readOnly = true)
    public List<SchoolPaper> listForTenant() {
        return paperRepository.findBySchoolId(requireTenant());
    }

    @Transactional(readOnly = true)
    public SchoolPaper getForTenant(Long paperId) {
        return requireOwnedPaper(paperId);
    }

    @Transactional(readOnly = true)
    public List<Question> listQuestions(Long paperId) {
        requireOwnedPaper(paperId); // tenant ownership check
        return questionRepository.findBySchoolPaperId(paperId);
    }

    /**
     * Adds a (reviewed/approved-by-teacher) question to a DRAFT paper. The same shape is
     * produced by the AI extraction pipeline; this is the "teacher reviews & approves
     * extracted questions" step (PRODUCT_BUSINESS_PLAN.md §5.2).
     */
    @Transactional
    public Question addQuestion(Long paperId, String text, QuestionType type, Integer marks, String correctAnswerText) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        if (paper.getStatus() != SchoolPaperStatus.DRAFT) {
            throw new IllegalStateException("Questions can only be added while the paper is in DRAFT");
        }
        Question q = new Question();
        q.setSchoolPaperId(paper.getId());
        q.setText(text);
        q.setType(type != null ? type : QuestionType.ESSAY);
        q.setMarks(marks);
        q.setCorrectAnswerText(correctAnswerText);
        return questionRepository.save(q);
    }

    @Transactional
    public SchoolPaper approve(Long paperId) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        if (paper.getStatus() != SchoolPaperStatus.DRAFT) {
            throw new IllegalStateException("Only a DRAFT paper can be approved");
        }
        if (questionRepository.countBySchoolPaperId(paperId) == 0) {
            throw new IllegalStateException("Cannot approve a paper with no questions");
        }
        paper.setStatus(SchoolPaperStatus.APPROVED);
        return paperRepository.save(paper);
    }

    @Transactional
    public SchoolPaper assignToClass(Long paperId, Long classId, LocalDateTime windowStart,
            LocalDateTime windowEnd, Integer timeLimitMinutes) {
        Long schoolId = requireTenant();
        SchoolPaper paper = requireOwnedPaper(paperId);
        if (paper.getStatus() != SchoolPaperStatus.APPROVED && paper.getStatus() != SchoolPaperStatus.ASSIGNED) {
            throw new IllegalStateException("Only an APPROVED paper can be assigned");
        }
        // Class must belong to the same tenant.
        classRepository.findByIdAndSchoolId(classId, schoolId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found in this school"));
        paper.setClassId(classId);
        paper.setExamWindowStart(windowStart);
        paper.setExamWindowEnd(windowEnd);
        paper.setTimeLimitMinutes(timeLimitMinutes);
        paper.setStatus(SchoolPaperStatus.ASSIGNED);
        return paperRepository.save(paper);
    }

    /** Papers a student in {@code classId} may see: ASSIGNED only (drafts/approved are hidden). */
    @Transactional(readOnly = true)
    public List<SchoolPaper> listAssignedForClass(Long classId) {
        return paperRepository.findByClassIdAndSchoolId(classId, requireTenant()).stream()
                .filter(p -> p.getStatus() == SchoolPaperStatus.ASSIGNED)
                .toList();
    }
}
