package com.eduapp.backend.it;

import com.eduapp.backend.exception.ExtractionLimitExceededException;
import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.service.ExtractionTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NFR-RACE-1: proves the per-question extraction cap holds under concurrency. The old check-then-act
 * limiter let simultaneous requests exceed the cap (extra paid AI calls). The atomic conditional
 * UPDATE must let through EXACTLY {@code maxExtractions} reservations no matter how many threads race.
 */
class ExtractionConcurrencyIT extends AbstractIntegrationTest {

    @Autowired ExtractionTrackingService service;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired QuestionExtractionTrackingRepository trackingRepository;

    private record Fixture(Long attemptId, Long questionId) {
    }

    private Fixture fixture(int maxExtractions) {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("ext-" + u + "@r.test", "x", "ext-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);

        Paper paper = new Paper();
        paper.setName("Ext " + u);
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(10);
        paper = paperRepository.save(paper);

        Question q = questionRepository.save(new Question(paper, "Q?", QuestionType.ESSAY, "a"));

        StudentPaperAttempt attempt = attemptRepository.save(
                new StudentPaperAttempt(user, paper, 1, LocalDateTime.now()));

        // Pre-create the tracking row (as the real flow does via initializeTrackingForAttempt),
        // then set the cap for the test.
        QuestionExtractionTracking t = new QuestionExtractionTracking(attempt, q);
        t.setMaxExtractions(maxExtractions);
        trackingRepository.save(t);

        return new Fixture(attempt.getId(), q.getId());
    }

    @Test
    void sequential_allowsExactlyMaxThenThrows() {
        Fixture f = fixture(2);
        service.reserveExtraction(f.attemptId(), f.questionId());
        service.reserveExtraction(f.attemptId(), f.questionId());
        assertThatThrownBy(() -> service.reserveExtraction(f.attemptId(), f.questionId()))
                .isInstanceOf(ExtractionLimitExceededException.class);
    }

    @Test
    void release_freesASlot() {
        Fixture f = fixture(1);
        service.reserveExtraction(f.attemptId(), f.questionId());            // at limit
        service.releaseExtraction(f.attemptId(), f.questionId());            // freed (AI failed)
        // A slot is available again.
        service.reserveExtraction(f.attemptId(), f.questionId());
        assertThat(trackingRepository.findByAttemptIdAndQuestionId(f.attemptId(), f.questionId())
                .orElseThrow().getExtractionCount()).isEqualTo(1);
    }

    @Test
    void concurrent_neverExceedsTheCap() throws Exception {
        int cap = 2;
        int threads = 10;
        Fixture f = fixture(cap);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger granted = new AtomicInteger();
        AtomicInteger denied = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    service.reserveExtraction(f.attemptId(), f.questionId());
                    granted.incrementAndGet();
                } catch (ExtractionLimitExceededException e) {
                    denied.incrementAndGet();
                } catch (Exception ignored) {
                    // a lazy concurrent-create fallback may surface a DB error; counts as not-granted
                }
            });
        }
        start.countDown(); // release all threads at once
        pool.shutdown();
        assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();

        assertThat(granted.get())
                .as("atomic reservation must never exceed the cap under concurrency")
                .isEqualTo(cap);
        assertThat(trackingRepository.findByAttemptIdAndQuestionId(f.attemptId(), f.questionId())
                .orElseThrow().getExtractionCount()).isEqualTo(cap);
    }
}
