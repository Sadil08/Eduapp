package com.eduapp.backend.service;

import java.util.Map;
import java.util.Set;

/**
 * NFR-PARTIAL-1: parsed, validated result of an AI marking response. Produced by
 * {@link AIAnalysisService#parseMarkingResult} from the (sanitized) model JSON, then applied by
 * {@code MarkingPersistenceService} inside a short write transaction.
 *
 * <p>Only entries with a valid numeric {@code questionId} + {@code marksAwarded} are retained;
 * malformed entries are dropped during parsing so the persistence step can detect incomplete
 * coverage (and retry) rather than silently under-counting a score.
 */
public final class MarkingResult {

    /** One question's mark + feedback as returned by the model (raw — server clamps before use). */
    public record QuestionMark(long questionId, int rawMarks, String feedback) {
    }

    private final Map<Long, QuestionMark> byQuestionId;
    private final String overallFeedback;
    private final Integer aiTotalMarks;

    public MarkingResult(Map<Long, QuestionMark> byQuestionId, String overallFeedback, Integer aiTotalMarks) {
        this.byQuestionId = byQuestionId;
        this.overallFeedback = overallFeedback;
        this.aiTotalMarks = aiTotalMarks;
    }

    public Map<Long, QuestionMark> byQuestionId() {
        return byQuestionId;
    }

    public Set<Long> returnedQuestionIds() {
        return byQuestionId.keySet();
    }

    public String overallFeedback() {
        return overallFeedback;
    }

    public Integer aiTotalMarks() {
        return aiTotalMarks;
    }
}
