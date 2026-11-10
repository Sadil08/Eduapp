package com.eduapp.backend.service;

import com.eduapp.backend.dto.McqOptionInput;
import com.eduapp.backend.dto.SchoolQuestionDto;
import com.eduapp.backend.dto.StudentQuestionDto;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.QuestionOptionRepository;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.SchoolClassRepository;
import com.eduapp.backend.repository.SchoolPaperRepository;
import com.eduapp.backend.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * School-tier paper lifecycle, all scoped to the current tenant. Questions are stored as
 * shared {@link Question} rows tagged with {@code school_paper_id}, so the existing AI
 * extraction/marking pipeline applies to school papers without modification.
 */
@Service
public class SchoolPaperService {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 10;

    private final SchoolPaperRepository paperRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final SchoolClassRepository classRepository;
    private final FileStorageService fileStorageService;
    private final AIService aiService;

    public SchoolPaperService(SchoolPaperRepository paperRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            SchoolClassRepository classRepository,
            FileStorageService fileStorageService,
            AIService aiService) {
        this.paperRepository = paperRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.classRepository = classRepository;
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

    private SchoolPaper requireOwnedPaper(Long paperId) {
        return paperRepository.findByIdAndSchoolId(paperId, requireTenant())
                .orElseThrow(() -> new IllegalArgumentException("Paper not found in this school"));
    }

    /**
     * Loads a question and proves it belongs to the given tenant-owned paper. {@link Question}
     * is a shared (non-tenant-scoped) table, so we MUST verify {@code schoolPaperId} matches the
     * caller's paper — otherwise a teacher could edit another school's (or a platform) question by id.
     */
    private Question requireOwnedQuestion(SchoolPaper paper, Long questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (q.getSchoolPaperId() == null || !q.getSchoolPaperId().equals(paper.getId())) {
            throw new IllegalArgumentException("Question does not belong to this paper");
        }
        return q;
    }

    private void requireDraft(SchoolPaper paper, String action) {
        if (paper.getStatus() != SchoolPaperStatus.DRAFT) {
            throw new IllegalStateException("Can only " + action + " while the paper is in DRAFT");
        }
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

    /** Teacher view of a paper's questions (with MCQ options + the answer key). */
    @Transactional(readOnly = true)
    public List<SchoolQuestionDto> listQuestionDtos(Long paperId) {
        requireOwnedPaper(paperId);
        List<Question> qs = questionRepository.findBySchoolPaperId(paperId);
        Map<Long, List<QuestionOption>> byQ = optionsByQuestion(qs);
        return qs.stream().map(q -> SchoolQuestionDto.from(q, byQ.getOrDefault(q.getId(), List.of()))).toList();
    }

    /** Student-safe view of an ASSIGNED paper's questions (MCQ options WITHOUT the answer key). */
    @Transactional(readOnly = true)
    public List<StudentQuestionDto> listStudentQuestionDtos(Long paperId) {
        requireOwnedPaper(paperId);
        List<Question> qs = questionRepository.findBySchoolPaperId(paperId);
        Map<Long, List<QuestionOption>> byQ = optionsByQuestion(qs);
        return qs.stream().map(q -> StudentQuestionDto.from(q, byQ.getOrDefault(q.getId(), List.of()))).toList();
    }

    /**
     * Adds a (reviewed/approved-by-teacher) question to a DRAFT paper. The same shape is
     * produced by the AI extraction pipeline; this is the "teacher reviews & approves
     * extracted questions" step (PRODUCT_BUSINESS_PLAN.md §5.2).
     */
    @Transactional
    public SchoolQuestionDto addQuestion(Long paperId, String text, QuestionType type, Integer marks,
            String correctAnswerText, String imageUrl, String modelAnswerImageUrl, List<McqOptionInput> options) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        requireDraft(paper, "add questions");
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();
        if (!hasText && !hasImage) {
            throw new IllegalArgumentException("A question needs either text or an image");
        }
        Question q = new Question();
        q.setSchoolPaperId(paper.getId());
        q.setText(text);
        q.setType(type != null ? type : QuestionType.ESSAY);
        q.setMarks(marks);
        q.setCorrectAnswerText(correctAnswerText);
        q.setImageUrl(imageUrl);
        q.setModelAnswerImageUrl(modelAnswerImageUrl);
        q.setRequiresImageDisplay(imageUrl != null && !imageUrl.isBlank());
        applyOptions(q, q.getType(), options);
        Question saved = questionRepository.save(q);
        return SchoolQuestionDto.from(saved, new ArrayList<>(saved.getOptions()));
    }

    /** Edit a DRAFT paper's metadata (name/description). Locked once APPROVED/ASSIGNED. */
    @Transactional
    public SchoolPaper updatePaper(Long paperId, String name, String description) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        requireDraft(paper, "edit the paper");
        if (name != null && !name.isBlank()) {
            paper.setName(name);
        }
        paper.setDescription(description);
        return paperRepository.save(paper);
    }

    /** Edit a question on a DRAFT paper. Null image URLs leave the existing image untouched. */
    @Transactional
    public SchoolQuestionDto updateQuestion(Long paperId, Long questionId, String text, QuestionType type,
            Integer marks, String correctAnswerText, String imageUrl, String modelAnswerImageUrl,
            List<McqOptionInput> options) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        requireDraft(paper, "edit questions");
        Question q = requireOwnedQuestion(paper, questionId);
        if (text != null && !text.isBlank()) {
            q.setText(text);
        }
        if (type != null) {
            q.setType(type);
        }
        q.setMarks(marks);
        q.setCorrectAnswerText(correctAnswerText);
        if (imageUrl != null) {
            q.setImageUrl(imageUrl.isBlank() ? null : imageUrl);
            q.setRequiresImageDisplay(!imageUrl.isBlank());
        }
        if (modelAnswerImageUrl != null) {
            q.setModelAnswerImageUrl(modelAnswerImageUrl.isBlank() ? null : modelAnswerImageUrl);
        }
        applyOptions(q, q.getType(), options);
        Question saved = questionRepository.save(q);
        return SchoolQuestionDto.from(saved, new ArrayList<>(saved.getOptions()));
    }

    // ---------- MCQ options ----------

    /**
     * Replaces a question's options. For MCQ: validates (2-10, exactly one correct, non-blank) and
     * rebuilds the option rows (orphanRemoval deletes the old ones). For non-MCQ: clears any options
     * so a question that was switched away from MCQ doesn't keep stale choices.
     */
    private void applyOptions(Question q, QuestionType type, List<McqOptionInput> inputs) {
        if (type != QuestionType.MCQ) {
            q.getOptions().clear();
            return;
        }
        List<McqOptionInput> opts = inputs == null ? List.of() : inputs;
        validateOptions(opts);
        q.getOptions().clear();
        for (int i = 0; i < opts.size(); i++) {
            McqOptionInput in = opts.get(i);
            QuestionOption o = new QuestionOption();
            o.setQuestion(q);
            o.setText(in.getText().trim());
            o.setIsCorrect(in.isCorrect());
            o.setOrderIndex(i);
            o.setKeyLabel(String.valueOf((char) ('A' + i)));
            q.getOptions().add(o);
        }
    }

    private void validateOptions(List<McqOptionInput> opts) {
        if (opts.size() < MIN_OPTIONS || opts.size() > MAX_OPTIONS) {
            throw new IllegalArgumentException(
                    "An MCQ question needs between " + MIN_OPTIONS + " and " + MAX_OPTIONS + " options");
        }
        if (opts.stream().anyMatch(o -> o.getText() == null || o.getText().isBlank())) {
            throw new IllegalArgumentException("Every option needs text");
        }
        long correct = opts.stream().filter(McqOptionInput::isCorrect).count();
        if (correct != 1) {
            throw new IllegalArgumentException("Exactly one option must be marked correct");
        }
    }

    private Map<Long, List<QuestionOption>> optionsByQuestion(List<Question> questions) {
        List<Long> ids = questions.stream().map(Question::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return optionRepository.findByQuestionIdInOrderByOrderIndexAsc(ids).stream()
                .collect(Collectors.groupingBy(o -> o.getQuestion().getId()));
    }

    /**
     * Stores an uploaded image and AI-extracts its text, reusing the same pipeline the global
     * admin uses. Stateless: returns {imageUrl, extractedText} for the client to place into the
     * question text (docType="question") or the marking scheme (docType="answer"). The paper must
     * be a DRAFT the caller owns — this both authorises and bounds use of the (rate-limited) AI call.
     *
     * Intentionally NOT @Transactional: the ownership read runs in its own short transaction, then
     * the slow Supabase upload + AI extraction happen with no DB connection held (pool-friendly).
     */
    public Map<String, String> extractImage(Long paperId, MultipartFile file, String docType) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        requireDraft(paper, "attach images");
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No image provided");
        }
        String hint = "answer".equalsIgnoreCase(docType) ? "answer" : "question";
        try {
            String imageUrl = fileStorageService.storeFile(file, "school-questions");
            String extracted = aiService.extractTextFromImage(file, null, null, hint);
            return Map.of("imageUrl", imageUrl, "extractedText", extracted != null ? extracted : "");
        } catch (Exception e) {
            throw new IllegalStateException("Could not process the image: " + e.getMessage(), e);
        }
    }

    /** Remove a question from a DRAFT paper. */
    @Transactional
    public void deleteQuestion(Long paperId, Long questionId) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        requireDraft(paper, "delete questions");
        Question q = requireOwnedQuestion(paper, questionId);
        questionRepository.delete(q);
    }

    @Transactional
    public SchoolPaper approve(Long paperId) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        if (paper.getStatus() != SchoolPaperStatus.DRAFT) {
            throw new IllegalStateException("Only a DRAFT paper can be approved");
        }
        List<Question> questions = questionRepository.findBySchoolPaperId(paperId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Cannot approve a paper with no questions");
        }
        // Every MCQ question must have a valid, answerable option set before the paper goes live.
        Map<Long, List<QuestionOption>> byQ = optionsByQuestion(questions);
        for (Question q : questions) {
            if (q.getType() == QuestionType.MCQ) {
                List<QuestionOption> opts = byQ.getOrDefault(q.getId(), List.of());
                long correct = opts.stream().filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).count();
                if (opts.size() < MIN_OPTIONS || opts.size() > MAX_OPTIONS || correct != 1) {
                    throw new IllegalStateException(
                            "Each MCQ question must have " + MIN_OPTIONS + "-" + MAX_OPTIONS
                                    + " options with exactly one correct answer");
                }
            }
        }
        paper.setStatus(SchoolPaperStatus.APPROVED);
        return paperRepository.save(paper);
    }

    @Transactional
    public SchoolPaper assignToClass(Long paperId, Long classId, java.time.Instant windowStart,
            java.time.Instant windowEnd, Integer timeLimitMinutes) {
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

    /** Teacher releases results so students can see their marks (WP-7). */
    @Transactional
    public SchoolPaper releaseResults(Long paperId) {
        SchoolPaper paper = requireOwnedPaper(paperId);
        paper.setResultsReleased(true);
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
