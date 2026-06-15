package com.eduapp.backend.it;

import com.eduapp.backend.model.*;
import com.eduapp.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-A1 durable marking: the sweeper query selects ONLY genuinely-stuck, unanalysed attempts. */
class MarkingSweeperIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired PaperRepository paperRepository;
    @Autowired StudentPaperAttemptRepository attemptRepository;
    @Autowired OverallPaperAnalysisRepository overallRepository;

    private Paper paper;

    private User student() {
        String u = UUID.randomUUID().toString().substring(0, 8);
        User user = new User("sweep-" + u + "@m.test", "x", "sweep-" + u);
        user.setRole(Role.STUDENT);
        user.setReferralCode("R" + u);
        return userRepository.save(user);
    }

    private StudentPaperAttempt attempt(LocalDateTime completedAt, AttemptStatus status,
            int submissionCount, LocalDateTime lastSubmission) {
        if (paper == null) {
            Paper p = new Paper();
            p.setName("Sweep " + UUID.randomUUID().toString().substring(0, 5));
            p.setType(PaperType.ESSAY);
            p.setTotalMarks(100);
            paper = paperRepository.save(p);
        }
        StudentPaperAttempt a = new StudentPaperAttempt(student(), paper, 1, LocalDateTime.now().minusHours(1));
        a.setStatus(status);
        a.setCompletedAt(completedAt);
        a.setSubmissionCount(submissionCount);
        a.setLastSubmissionTime(lastSubmission);
        return attemptRepository.save(a);
    }

    @Test
    void selectsOnlyStuckUnanalysedAttempts() {
        LocalDateTime now = LocalDateTime.now();

        StudentPaperAttempt stuck = attempt(now.minusMinutes(30), AttemptStatus.SUBMITTED, 0, null);     // ✓ match
        StudentPaperAttempt recent = attempt(now.minusMinutes(2), AttemptStatus.SUBMITTED, 0, null);     // too recent
        StudentPaperAttempt maxed = attempt(now.minusMinutes(30), AttemptStatus.SUBMITTED, 5, null);     // retry cap
        StudentPaperAttempt throttled = attempt(now.minusMinutes(30), AttemptStatus.SUBMITTED, 1, now.minusMinutes(2)); // recently retried
        StudentPaperAttempt ancient = attempt(now.minusDays(2), AttemptStatus.SUBMITTED, 0, null);       // older than 24h
        StudentPaperAttempt inProgress = attempt(now.minusMinutes(30), AttemptStatus.IN_PROGRESS, 0, null); // not submitted

        // analysed: stale + submitted but already has an OverallPaperAnalysis
        StudentPaperAttempt analysed = attempt(now.minusMinutes(30), AttemptStatus.SUBMITTED, 0, null);
        overallRepository.save(new OverallPaperAnalysis(analysed, 80, null, null));

        List<StudentPaperAttempt> found = attemptRepository.findStuckAnalysis(
                now.minusMinutes(10), now.minusHours(24), now.minusMinutes(10), 5, PageRequest.of(0, 50));

        List<Long> ids = found.stream().map(StudentPaperAttempt::getId).toList();
        assertThat(ids).contains(stuck.getId());
        assertThat(ids).doesNotContain(recent.getId(), maxed.getId(), throttled.getId(),
                ancient.getId(), inProgress.getId(), analysed.getId());
    }
}
