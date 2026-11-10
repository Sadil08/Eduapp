package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.service.MarkingPersistenceService;
import com.eduapp.backend.service.MarkingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NFR-IDEM-1 + NFR-PARTIAL-1 at the DB level. Marking persistence must be idempotent (repeated
 * dispatch → exactly one analysis row, never a NonUniqueResult), set the completion flag, compute
 * the weighted score, and REJECT a partial model response (rolling back) rather than silently
 * under-counting.
 */
class MarkingIdempotencyIT extends AbstractIntegrationTest {

    @Autowired MarkingPersistenceService persistence;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired StudentAnswerRepository studentAnswerRepository;
    @Autowired OverallPaperAnalysisRepository analysisRepository;

    private record Fixture(Long attemptId, Long q1, Long q2) {
    }

    /** Paper total 100, two questions worth 10 each (allocated 20). */
    private Fixture fixture() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("mark-" + u + "@m.test", "x", "mark-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);

        Paper paper = new Paper();
        paper.setName("Mark " + u);
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(100);
        paper = paperRepository.save(paper);

        Question q1 = new Question(paper, "Q1", QuestionType.ESSAY, "scheme1");
        q1.setMarks(10);
        q1 = questionRepository.save(q1);
        Question q2 = new Question(paper, "Q2", QuestionType.ESSAY, "scheme2");
        q2.setMarks(10);
        q2 = questionRepository.save(q2);

        StudentPaperAttempt attempt = new StudentPaperAttempt(user, paper, 1, LocalDateTime.now());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt = attemptRepository.save(attempt);

        studentAnswerRepository.save(new StudentAnswer(attempt, q1, "a1", null));
        studentAnswerRepository.save(new StudentAnswer(attempt, q2, "a2", null));

        return new Fixture(attempt.getId(), q1.getId(), q2.getId());
    }

    private MarkingResult fullResult(Fixture f, int m1, int m2) {
        Map<Long, MarkingResult.QuestionMark> map = new LinkedHashMap<>();
        map.put(f.q1(), new MarkingResult.QuestionMark(f.q1(), m1, "fb1"));
        map.put(f.q2(), new MarkingResult.QuestionMark(f.q2(), m2, "fb2"));
        return new MarkingResult(map, "overall feedback", m1 + m2);
    }

    @Test
    void persistMarking_isIdempotent_andComputesWeightedScore() {
        Fixture f = fixture();

        // Run marking TWICE for the same attempt (simulates live-async + sweeper/retry racing).
        persistence.persistMarking(f.attemptId(), fullResult(f, 8, 6));
        persistence.persistMarking(f.attemptId(), fullResult(f, 8, 6));

        // Exactly one analysis row — findByAttemptId throws if a duplicate exists.
        OverallPaperAnalysis analysis = analysisRepository.findByAttemptId(f.attemptId()).orElseThrow();
        // weighted = round((8+6)/20 * 100) = 70
        assertThat(analysis.getTotalMarks()).isEqualTo(70);

        StudentPaperAttempt attempt = attemptRepository.findById(f.attemptId()).orElseThrow();
        assertThat(attempt.getAnalysisCompleted()).isTrue();
        assertThat(attempt.getAnalysisError()).isNull();
    }

    @Test
    void persistMarking_rejectsPartialCoverage_andRollsBack() {
        Fixture f = fixture();

        // Result covers only q1 → incomplete coverage must throw (retryable), not under-count.
        Map<Long, MarkingResult.QuestionMark> partial = new LinkedHashMap<>();
        partial.put(f.q1(), new MarkingResult.QuestionMark(f.q1(), 9, "fb"));
        MarkingResult result = new MarkingResult(partial, "overall", 9);

        assertThatThrownBy(() -> persistence.persistMarking(f.attemptId(), result))
                .isInstanceOf(IllegalStateException.class);

        // No analysis row and the answer marks were rolled back (not silently persisted).
        assertThat(analysisRepository.findByAttemptId(f.attemptId())).isEmpty();
        assertThat(studentAnswerRepository.findByAttemptId(f.attemptId()))
                .allSatisfy(a -> assertThat(a.getMarksAwarded()).isNull());
    }
}
