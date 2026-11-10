package com.eduapp.backend.security;

import com.eduapp.backend.model.Role;
import com.eduapp.backend.repository.StudentAnswerRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sprint-0 (SEC-IDOR): central object-level authorization guard for consumer-tier attempts and
 * answers.
 *
 * <p>The legacy consumer controllers ({@code StudentPaperAttemptController},
 * {@code StudentAnswerController}) fetched objects by id with no check that the object belongs to
 * the caller — any authenticated user could read/modify/delete another student's attempt or
 * answer, or trigger paid AI re-marking on arbitrary attempts. This guard enforces that the
 * acting principal owns the object (or is an {@code ADMIN}).
 *
 * <p>Denials throw {@code 404 NOT_FOUND} (not 403) so a probing caller cannot distinguish
 * "exists but not yours" from "does not exist" — i.e. no id-enumeration oracle.
 */
@Component("attemptAccessGuard")
public class AttemptAccessGuard {

    private static final Logger logger = LoggerFactory.getLogger(AttemptAccessGuard.class);

    private final StudentPaperAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public AttemptAccessGuard(StudentPaperAttemptRepository attemptRepository,
            StudentAnswerRepository studentAnswerRepository) {
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    private static boolean isAdmin(AuthenticatedUser principal) {
        return principal != null && principal.getRole() == Role.ADMIN;
    }

    /** @return true if the principal owns the attempt, or is an ADMIN. */
    public boolean ownsAttempt(Long attemptId, AuthenticatedUser principal) {
        if (attemptId == null || principal == null) {
            return false;
        }
        if (isAdmin(principal)) {
            return true;
        }
        return attemptRepository.findByIdAndStudentId(attemptId, principal.getUserId()).isPresent();
    }

    /** @return true if the principal owns the attempt that this answer belongs to, or is an ADMIN. */
    public boolean ownsAnswer(Long answerId, AuthenticatedUser principal) {
        if (answerId == null || principal == null) {
            return false;
        }
        if (isAdmin(principal)) {
            return true;
        }
        return studentAnswerRepository
                .findByIdAndAttempt_Student_Id(answerId, principal.getUserId())
                .isPresent();
    }

    /** Throws {@code 404} if the principal does not own the attempt (and is not ADMIN). */
    public void assertOwnsAttempt(Long attemptId, AuthenticatedUser principal) {
        if (!ownsAttempt(attemptId, principal)) {
            logger.warn("Denied attempt access: user={} attemptId={}",
                    principal != null ? principal.getUserId() : null, attemptId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found");
        }
    }

    /** Throws {@code 404} if the principal does not own the answer (and is not ADMIN). */
    public void assertOwnsAnswer(Long answerId, AuthenticatedUser principal) {
        if (!ownsAnswer(answerId, principal)) {
            logger.warn("Denied answer access: user={} answerId={}",
                    principal != null ? principal.getUserId() : null, answerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }
    }
}
