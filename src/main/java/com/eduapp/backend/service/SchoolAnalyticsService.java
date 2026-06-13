package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperAnalyticsSummaryDto;
import com.eduapp.backend.dto.QuestionAnalyticsDto;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cohort analytics for the school tier (WP-8). All queries are tenant-scoped and bounded
 * by a single paper's cohort (a class) — never an unbounded global scan. The expensive
 * summary is cached; the cache key includes the tenant id so a cache hit can never serve
 * another school's data.
 */
@Service
public class SchoolAnalyticsService {

    private final SchoolPaperRepository paperRepository;
    private final SchoolPaperAttemptRepository attemptRepository;
    private final SchoolStudentAnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public SchoolAnalyticsService(SchoolPaperRepository paperRepository,
            SchoolPaperAttemptRepository attemptRepository,
            SchoolStudentAnswerRepository answerRepository,
            QuestionRepository questionRepository) {
        this.paperRepository = paperRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Cached, tenant-keyed. Called directly from the controller (not self-invoked) so the
     * {@code @Cacheable} proxy applies. On a cache miss it verifies the paper belongs to the
     * tenant; a different tenant produces a different key, so cross-tenant cache hits are
     * impossible.
     */
    @Cacheable(value = "schoolPaperSummary", key = "#schoolId + ':' + #paperId")
    @Transactional(readOnly = true)
    public PaperAnalyticsSummaryDto paperSummary(Long schoolId, Long paperId) {
        requireOwnedPaper(schoolId, paperId);
        List<SchoolPaperAttempt> attempts = attemptRepository.findBySchoolPaperIdAndSchoolId(paperId, schoolId);

        List<Integer> marks = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED || a.getStatus() == AttemptStatus.GRADED)
                .map(SchoolPaperAttempt::getEffectiveMark)
                .filter(Objects::nonNull)
                .toList();

        Double avg = marks.isEmpty() ? null : marks.stream().mapToInt(Integer::intValue).average().orElse(0);
        Integer high = marks.stream().max(Integer::compareTo).orElse(null);
        Integer low = marks.stream().min(Integer::compareTo).orElse(null);
        Map<Integer, Long> distribution = marks.stream()
                .collect(Collectors.groupingBy(m -> m, TreeMap::new, Collectors.counting()));

        return new PaperAnalyticsSummaryDto(paperId, attempts.size(), marks.size(), avg, high, low, distribution);
    }

    @Transactional(readOnly = true)
    public List<QuestionAnalyticsDto> questionAnalysis(Long schoolId, Long paperId) {
        requireOwnedPaper(schoolId, paperId);

        List<Long> attemptIds = attemptRepository.findBySchoolPaperIdAndSchoolId(paperId, schoolId).stream()
                .map(SchoolPaperAttempt::getId).toList();
        Map<Long, List<SchoolStudentAnswer>> byQuestion = attemptIds.isEmpty()
                ? Map.of()
                : answerRepository.findByAttemptIdIn(attemptIds).stream()
                        .collect(Collectors.groupingBy(SchoolStudentAnswer::getQuestionId));

        List<QuestionAnalyticsDto> result = new ArrayList<>();
        for (Question q : questionRepository.findBySchoolPaperId(paperId)) {
            List<SchoolStudentAnswer> answers = byQuestion.getOrDefault(q.getId(), List.of());
            List<Integer> awarded = answers.stream()
                    .map(SchoolStudentAnswer::getMarksAwarded).filter(Objects::nonNull).toList();
            Double avg = awarded.isEmpty() ? null : awarded.stream().mapToInt(Integer::intValue).average().orElse(0);
            Double correctRate = null;
            if (q.getMarks() != null && !awarded.isEmpty()) {
                long full = awarded.stream().filter(m -> m.equals(q.getMarks())).count();
                correctRate = (double) full / awarded.size();
            }
            result.add(new QuestionAnalyticsDto(q.getId(), q.getText(), answers.size(), avg, q.getMarks(), correctRate));
        }
        return result;
    }

    private SchoolPaper requireOwnedPaper(Long schoolId, Long paperId) {
        if (schoolId == null) {
            throw new IllegalStateException("No tenant in context — school-tier user required");
        }
        return paperRepository.findByIdAndSchoolId(paperId, schoolId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found in this school"));
    }
}
