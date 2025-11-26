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

        List<StudentPaperAttempt> attempts = attemptRepository
                .findByPaperIdAndOptedInTrueOrderByTimeTakenMinutesAsc(paperId);

        // We also need to sort by marks. Since we don't have total marks in Attempt
        // entity directly (it's in OverallAnalysis),
        // we might need to fetch it or calculate it.
        // Or better, add totalMarks to StudentPaperAttempt for easier querying.
        // For now, let's calculate from answers or fetch from analysis.
        // Calculating from answers is easier if marksAwarded is populated.

        List<Map<String, Object>> leaderboard = attempts.stream()
                .map(attempt -> {
                    int totalMarks = attempt.getAnswers().stream()
                            .mapToInt(a -> a.getMarksAwarded() != null ? a.getMarksAwarded() : 0)
                            .sum();

                    Map<String, Object> entry = new java.util.HashMap<>();
                    entry.put("studentName", attempt.getStudent().getUsername());
                    entry.put("marks", totalMarks);
                    entry.put("timeTaken", attempt.getTimeTakenMinutes() != null ? attempt.getTimeTakenMinutes() : 0);
                    return entry;
                })
                .sorted((a, b) -> Integer.compare((int) b.get("marks"), (int) a.get("marks"))) // Sort by marks desc
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }
}
