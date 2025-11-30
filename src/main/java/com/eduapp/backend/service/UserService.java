package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.eduapp.backend.mapper.*;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class UserService implements UserDetailsService {

        private static final Logger logger = LoggerFactory.getLogger(UserService.class);

        private final UserRepository userRepository;
        private final StudentBundleAccessRepository studentBundleAccessRepository;
        private final StudentPaperAttemptRepository studentPaperAttemptRepository;
        private final ProgressRepository progressRepository;
        private final LeaderboardEntryRepository leaderboardEntryRepository;
        private final AIAnalysisRepository aiAnalysisRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;

        private final PaperBundleMapper paperBundleMapper;
        private final StudentPaperAttemptMapper studentPaperAttemptMapper;
        private final ProgressMapper progressMapper;
        private final LeaderboardEntryMapper leaderboardEntryMapper;
        private final AIAnalysisMapper aiAnalysisMapper;

        public UserService(UserRepository userRepository,
                        StudentBundleAccessRepository studentBundleAccessRepository,
                        StudentPaperAttemptRepository studentPaperAttemptRepository,
                        ProgressRepository progressRepository,
                        LeaderboardEntryRepository leaderboardEntryRepository,
                        AIAnalysisRepository aiAnalysisRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        PaperBundleMapper paperBundleMapper,
                        StudentPaperAttemptMapper studentPaperAttemptMapper,
                        ProgressMapper progressMapper,
                        LeaderboardEntryMapper leaderboardEntryMapper,
                        AIAnalysisMapper aiAnalysisMapper) {
                this.userRepository = userRepository;
                this.studentBundleAccessRepository = studentBundleAccessRepository;
                this.studentPaperAttemptRepository = studentPaperAttemptRepository;
                this.progressRepository = progressRepository;
                this.leaderboardEntryRepository = leaderboardEntryRepository;
                this.aiAnalysisRepository = aiAnalysisRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtUtil = jwtUtil;
                this.paperBundleMapper = paperBundleMapper;
                this.studentPaperAttemptMapper = studentPaperAttemptMapper;
                this.progressMapper = progressMapper;
                this.leaderboardEntryMapper = leaderboardEntryMapper;
                this.aiAnalysisMapper = aiAnalysisMapper;
        }

        // --- Register normal student ---
        @Transactional
        public User register(RegisterRequest req) {
                if (req == null) {
                        throw new IllegalArgumentException("RegisterRequest cannot be null");
                }
                userRepository.findByEmail(req.getEmail())
                                .ifPresent(u -> {
                                        throw new RuntimeException("Email already exists");
                                });

                User user = new User();
                user.setEmail(req.getEmail());
                user.setPassword(passwordEncoder.encode(req.getPassword()));
                user.setUsername(req.getName());

                if (req.getRole() != null) {
                        user.setRole(req.getRole());
                } else {
                        user.setRole(Role.STUDENT);
                }

                return userRepository.save(user);
        }

        // --- Create admin manually (restricted) ---
        @Transactional
        public User createAdmin(RegisterRequest req) {
                userRepository.findByEmail(req.getEmail())
                                .ifPresent(u -> {
                                        throw new RuntimeException("Email already exists");
                                });

                User user = new User();
                user.setEmail(req.getEmail());
                user.setPassword(passwordEncoder.encode(req.getPassword()));
                user.setUsername(req.getName());
                user.setRole(Role.ADMIN);

                return userRepository.save(user);
        }

        // --- Login ---
        public String login(String email, String rawPassword) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        throw new RuntimeException("Invalid password");
                }
                return jwtUtil.generateToken(email, user.getRole(), user.getId());
        }

        public java.util.Optional<User> findById(Long id) {
                return userRepository.findById(id);
        }

        // --- Get all users (admin) ---
        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {
                logger.info("Fetching all users for admin");
                List<User> users = userRepository.findAll();
                List<UserResponse> responses = users.stream()
                                .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                                                user.getRole()))
                                .collect(Collectors.toList());
                logger.info("Found {} users", responses.size());
                return responses;
        }

        // --- Get detailed user data (admin) ---
        @Transactional(readOnly = true)
        public UserDetailDto getUserDetails(Long userId) {
                logger.info("Fetching detailed data for user ID: {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                // Fetch accessed bundles
                List<PaperBundleDto> accessedBundles = studentBundleAccessRepository.findByStudentId(userId)
                                .stream()
                                .map(access -> paperBundleMapper.toDto(access.getBundle()))
                                .collect(Collectors.toList());

                // Attempted papers
                List<StudentPaperAttemptDto> attemptedPapers = studentPaperAttemptRepository.findByStudentId(userId)
                                .stream()
                                .map(studentPaperAttemptMapper::toDto)
                                .collect(Collectors.toList());

                // Progress
                List<ProgressDto> progress = progressRepository.findByUserId(userId)
                                .stream()
                                .map(progressMapper::toDto)
                                .collect(Collectors.toList());

                // Scores
                List<LeaderboardEntryDto> scores = leaderboardEntryRepository.findByUserId(userId)
                                .stream()
                                .map(leaderboardEntryMapper::toDto)
                                .collect(Collectors.toList());

                // AI Feedback summaries
                List<AIAnalysisDto> aiFeedbackSummaries = aiAnalysisRepository.findByAnswerAttemptStudentId(userId)
                                .stream()
                                .map(aiAnalysisMapper::toDto)
                                .collect(Collectors.toList());

                UserDetailDto detail = new UserDetailDto(user.getId(), user.getUsername(), user.getEmail(),
                                user.getRole(),
                                user.getCreatedAt(), user.getUpdatedAt(), accessedBundles, attemptedPapers, progress,
                                scores, aiFeedbackSummaries);
                logger.info("Fetched detailed data for user {}", userId);
                return detail;
        }

        // --- Spring Security support ---
        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities("ROLE_" + user.getRole().name())
                                .build();
        }
}
