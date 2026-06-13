package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.model.PasswordResetToken;
import com.eduapp.backend.model.Role;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.*;
import com.eduapp.backend.security.JwtUtil;
import com.eduapp.backend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final EmailService emailService;

        private final PaperBundleMapper paperBundleMapper;
        private final StudentPaperAttemptMapper studentPaperAttemptMapper;
        private final ProgressMapper progressMapper;
        private final LeaderboardEntryMapper leaderboardEntryMapper;
        private final AIAnalysisMapper aiAnalysisMapper;
        private final GeoLocationService geoLocationService;

        public UserService(UserRepository userRepository,
                        StudentBundleAccessRepository studentBundleAccessRepository,
                        StudentPaperAttemptRepository studentPaperAttemptRepository,
                        ProgressRepository progressRepository,
                        LeaderboardEntryRepository leaderboardEntryRepository,
                        AIAnalysisRepository aiAnalysisRepository,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        EmailService emailService,
                        PaperBundleMapper paperBundleMapper,
                        StudentPaperAttemptMapper studentPaperAttemptMapper,
                        ProgressMapper progressMapper,
                        LeaderboardEntryMapper leaderboardEntryMapper,
                        AIAnalysisMapper aiAnalysisMapper,
                        GeoLocationService geoLocationService) {
                this.userRepository = userRepository;
                this.studentBundleAccessRepository = studentBundleAccessRepository;
                this.studentPaperAttemptRepository = studentPaperAttemptRepository;
                this.progressRepository = progressRepository;
                this.leaderboardEntryRepository = leaderboardEntryRepository;
                this.aiAnalysisRepository = aiAnalysisRepository;
                this.passwordResetTokenRepository = passwordResetTokenRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtUtil = jwtUtil;
                this.emailService = emailService;
                this.paperBundleMapper = paperBundleMapper;
                this.studentPaperAttemptMapper = studentPaperAttemptMapper;
                this.progressMapper = progressMapper;
                this.leaderboardEntryMapper = leaderboardEntryMapper;
                this.aiAnalysisMapper = aiAnalysisMapper;
                this.geoLocationService = geoLocationService;
        }

        // --- Register normal student ---
        @Transactional
        public User register(RegisterRequest req, String ipAddress) {
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

                // SECURITY: public self-registration must NEVER honour a client-supplied role.
                // Privileged roles (ADMIN, SCHOOL_ADMIN, TEACHER) are created only via
                // authenticated, role-gated admin/invite flows. Any role in the request body
                // is ignored here — a public registrant is always a STUDENT.
                user.setRole(Role.STUDENT);

                // Handle Lifetime Attribution at Signup
                String referralCode = req.getReferralCode();
                if (referralCode != null && !referralCode.trim().isEmpty()) {
                        userRepository.findByReferralCode(referralCode.trim().toUpperCase())
                                        .ifPresentOrElse(referrer -> {
                                                logger.info("Binding new user {} to referrer {}", req.getEmail(),
                                                                referrer.getId());
                                                user.setReferredBy(referrer);
                                        }, () -> logger.warn("Invalid referral code used during signup: {}",
                                                        referralCode));
                }

                // Location Tracking
                user.setRegistrationIp(ipAddress);
                String country = geoLocationService.getCountryFromIp(ipAddress);
                user.setCountry(country);

                // Generate unique referral code for the new user
                user.setReferralCode(generateUniqueReferralCode());

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
        public String login(String email, String rawPassword, String ipAddress) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        throw new RuntimeException("Invalid password");
                }

                // Update login stats
                user.setLastLoginIp(ipAddress);
                user.setLastLoginTime(java.time.LocalDateTime.now());
                userRepository.save(user);

                return jwtUtil.generateToken(email, user.getRole(), user.getId(), user.getReferralCode());
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

        // --- Forgot Password ---
        @Transactional
        public void forgotPassword(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Check for existing token and rate limit
                Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);

                String otp = generateOTP();

                if (existingToken.isPresent()) {
                        PasswordResetToken token = existingToken.get();

                        // Check 1 minute cooldown
                        if (token.getLastRequestTime().plusMinutes(1).isAfter(LocalDateTime.now())) {
                                throw new RuntimeException("Please wait 1 minute before requesting a new OTP");
                        }

                        token.updateToken(otp);
                        passwordResetTokenRepository.save(token);
                } else {
                        PasswordResetToken newToken = new PasswordResetToken(otp, user);
                        passwordResetTokenRepository.save(newToken);
                }

                // Send email
                emailService.sendPasswordResetEmail(user.getEmail(), otp);
        }

        @Transactional
        public void resetPassword(String email, String otp, String newPassword) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // Check 1-day password change limit
                if (user.getLastPasswordChangeDate() != null &&
                                user.getLastPasswordChangeDate().plusHours(24).isAfter(LocalDateTime.now())) {
                        throw new RuntimeException("You can only change your password once every 24 hours");
                }

                PasswordResetToken token = passwordResetTokenRepository.findByUser(user)
                                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

                // Validate OTP
                if (!token.getToken().equals(otp)) {
                        throw new RuntimeException("Invalid OTP");
                }

                // Validate Expiry
                if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                        throw new RuntimeException("OTP has expired");
                }

                // Reset Password
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setLastPasswordChangeDate(LocalDateTime.now());
                userRepository.save(user);

                // Delete used token
                passwordResetTokenRepository.delete(token);
        }

        private String generateOTP() {
                SecureRandom random = new SecureRandom();
                int otp = 100000 + random.nextInt(900000);
                return String.valueOf(otp);
        }

        private String generateUniqueReferralCode() {
                String code;
                do {
                        code = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                } while (userRepository.findByReferralCode(code).isPresent());
                return code;
        }
}
