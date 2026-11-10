package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.service.AIAnalysisService;
import com.eduapp.backend.service.PaperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * Scalability / correctness under load: when many requests for the SAME (student, paper, bundle)
 * race to start an attempt, the per-paper attempt cap must still hold — concurrent starts must not
 * mint extra IN_PROGRESS attempts beyond {@code maxFreeAttempts}.
 *
 * <p>Mirrors the proven {@link ExtractionConcurrencyIT} pattern (drive the service directly from a
 * thread pool). {@code PaperService.getPaperAttempt} is {@code @Transactional} with no DB-level
 * guard against duplicate IN_PROGRESS rows, so this is the natural place a race would surface. The
 * assertion encodes the invariant; if it fails it documents a real concurrency defect to fix
 * (e.g. a unique partial index on the active attempt, or an atomic upsert).
 */
class PaperAttemptConcurrencyIT extends AbstractIntegrationTest {

    @Autowired PaperService paperService;
    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired PaperBundleRepository bundleRepository;
    @Autowired StudentBundleAccessRepository accessRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;

    @MockBean AIAnalysisService aiAnalysisService;

    private Long userId;
    private Long paperId;
    private Long bundleId;
    private static final int MAX_FREE_ATTEMPTS = 2;

    @BeforeEach
    void setUp() {
        doNothing().when(aiAnalysisService).analyzeAttempt(any());

        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("conc-" + u + "@p.test", "x", "conc-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        user = userRepository.save(user);
        userId = user.getId();

        PaperBundle bundle = bundleRepository.save(new PaperBundle("Conc " + u, "desc",
                new BigDecimal("100.00"), PaperType.ESSAY, null, null, null, false));
        bundleId = bundle.getId();

        Paper paper = new Paper();
        paper.setName("Conc paper " + u);
        paper.setType(PaperType.ESSAY);
        paper.setTotalMarks(10);
        paper.setMaxFreeAttempts(MAX_FREE_ATTEMPTS);
        paper.setBundles(List.of(bundle));
        paper = paperRepository.save(paper);
        paperId = paper.getId();

        Question q = new Question(paper, "Q?", QuestionType.ESSAY, "a");
        q.setMarks(10);
        questionRepository.save(q);

        accessRepository.save(new StudentBundleAccess(user, bundle, "pay-" + u));
    }

    @Test
    void concurrentStarts_doNotExceedAttemptCap() throws Exception {
        int threads = 12;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    // forceNew=false → should resume the active attempt rather than mint new ones
                    paperService.getPaperAttempt(paperId, userId, bundleId, null, false);
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        int created = attemptRepository.countByStudentIdAndPaperIdAndOriginBundleId(userId, paperId, bundleId);
        assertThat(created)
                .as("concurrent starts must not exceed the per-paper attempt cap (no duplicate IN_PROGRESS minting)")
                .isLessThanOrEqualTo(MAX_FREE_ATTEMPTS);
    }
}
