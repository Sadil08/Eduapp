package com.eduapp.backend.controller;

import com.eduapp.backend.model.StudentPaperAttempt;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import com.eduapp.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);

    private final StudentPaperAttemptRepository attemptRepository;
    private final JwtUtil jwtUtil;

    public LeaderboardController(StudentPaperAttemptRepository attemptRepository, JwtUtil jwtUtil) {
        this.attemptRepository = attemptRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/opt-in")
    public ResponseEntity<Void> optIn(@RequestBody Map<String, Long> payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        Long attemptId = payload.get("attemptId");
        logger.info("Received request to opt-in for leaderboard for attempt ID: {}", attemptId);

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<StudentPaperAttempt> attemptOpt = attemptRepository.findById(attemptId);
        if (attemptOpt.isPresent()) {
            StudentPaperAttempt attempt = attemptOpt.get();
            if (!attempt.getStudent().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            attempt.setOptedIn(true);
            attemptRepository.save(attempt);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paper/{paperId}")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@PathVariable Long paperId) {
        logger.info("Fetching leaderboard for paper ID: {}", paperId);

        // Fetch all opted-in attempts for the paper
        List<StudentPaperAttempt> attempts = attemptRepository
                .findByPaperIdAndOptedInTrueOrderByTimeTakenMinutesAsc(paperId);

        // Map attempts to a simpler structure with calculated marks
        List<Map<String, Object>> allEntries = attempts.stream()
                .map(attempt -> {
                    int totalMarks = attempt.getAnswers().stream()
                            .mapToInt(a -> a.getMarksAwarded() != null ? a.getMarksAwarded() : 0)
                            .sum();

                    Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("studentId", attempt.getStudent().getId());
                    entry.put("studentName", attempt.getStudent().getUsername());
                    entry.put("marks", totalMarks);
                    entry.put("timeTaken", attempt.getTimeTakenMinutes() != null ? attempt.getTimeTakenMinutes() : 0);
                    return entry;
                })
                .collect(Collectors.toList());

        // Group by student ID and find the best attempt for each student
        // Best attempt = Highest Marks. If marks are equal, Lowest Time Taken.
        Map<Long, Map<String, Object>> bestAttempts = allEntries.stream()
                .collect(Collectors.toMap(
                        entry -> (Long) entry.get("studentId"),
                        entry -> entry,
                        (existing, replacement) -> {
                            int existingMarks = (int) existing.get("marks");
                            int replacementMarks = (int) replacement.get("marks");
                            if (replacementMarks > existingMarks) {
                                return replacement;
                            } else if (replacementMarks == existingMarks) {
                                int existingTime = (int) existing.get("timeTaken");
                                int replacementTime = (int) replacement.get("timeTaken");
                                return replacementTime < existingTime ? replacement : existing;
                            }
                            return existing;
                        }));

        // Convert back to list and sort by Marks (desc) then Time (asc)
        List<Map<String, Object>> leaderboard = bestAttempts.values().stream()
                .sorted((a, b) -> {
                    int marksCompare = Integer.compare((int) b.get("marks"), (int) a.get("marks"));
                    if (marksCompare != 0) {
                        return marksCompare;
                    }
                    return Integer.compare((int) a.get("timeTaken"), (int) b.get("timeTaken"));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }
}
