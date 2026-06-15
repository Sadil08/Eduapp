package com.eduapp.backend.service;

import com.eduapp.backend.dto.*;
import com.eduapp.backend.exception.BadRequestException;
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
                                        throw new BadRequestException("Email already exists");
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
                                        throw new BadRequestException("Email already exists");
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
                // N-S7: identical generic error for unknown-email and wrong-password (no user enumeration).
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

                if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        throw new BadRequestException("Invalid email or password");
                }

                // Update login stats
                user.setLastLoginIp(ipAddress);
                user.setLastLoginTime(java.time.LocalDateTime.now());
                userRepository.save(user);

                return jwtUtil.generateToken(email, user.getRole(), user.getId(),
                                user.getReferralCode(), user.getTokenVersion());
        }

        public java.util.Optional<User> findById(Long id) {
                return userRepository.findById(id);
        }

        // N-SC2: removed the unbounded getAllUsers() (no callers — the admin users list is
        // served by AdminUserService.getAllUsers(search, pageable), which is paginated).

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

                // N-SC1/N-S5: custom principal carries schoolId + tokenVersion so downstream
                // filters don't re-query the user on every request.
                return com.eduapp.backend.security.AuthenticatedUser.from(user);
        }

        // --- Forgot Password ---
        @Transactional
        public void forgotPassword(String email) {
                // N-P2: never reveal whether an email is registered. Silently no-op for unknown
                // emails; the controller always responds "OTP sent".
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                        logger.info("forgot-password requested for non-existent email (no-op)");
                        return;
                }
                User user = userOpt.get();

                // Check for existing token and rate limit
                Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);

                String otp = generateOTP();
                // N-S1: persist only the BCrypt hash of the OTP; the raw OTP goes to the user's email.
                String otpHash = passwordEncoder.encode(otp);

                if (existingToken.isPresent()) {
                        PasswordResetToken token = existingToken.get();

                        // Check 1 minute cooldown
                        if (token.getLastRequestTime().plusMinutes(1).isAfter(LocalDateTime.now())) {
                                throw new BadRequestException("Please wait 1 minute before requesting a new OTP");
                        }

                        token.updateToken(otpHash);
                        passwordResetTokenRepository.save(token);
                } else {
                        PasswordResetToken newToken = new PasswordResetToken(otpHash, user);
                        passwordResetTokenRepository.save(newToken);
                }

                // Send email
                emailService.sendPasswordResetEmail(user.getEmail(), otp);
        }

        // N-S2: the OTP attempt-count increment / lockout-delete MUST survive the thrown
        // BadRequestException — otherwise the default rollback-on-RuntimeException undoes the
        // counter and the brute-force lockout never triggers.
        @Transactional(noRollbackFor = BadRequestException.class)
        public void resetPassword(String email, String otp, String newPassword) {
                // N-P2/N-S7: don't reveal email existence — same generic error as a bad OTP.
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

                // Check 1-day password change limit
                if (user.getLastPasswordChangeDate() != null &&
                                user.getLastPasswordChangeDate().plusHours(24).isAfter(LocalDateTime.now())) {
                        throw new BadRequestException("You can only change your password once every 24 hours");
                }

                PasswordResetToken token = passwordResetTokenRepository.findByUser(user)
                                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

                // Validate Expiry first.
                if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                        passwordResetTokenRepository.delete(token);
                        throw new BadRequestException("Invalid or expired OTP");
                }

                // N-S2: brute-force lockout — a 6-digit OTP is only 1M combinations.
                if (token.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
                        passwordResetTokenRepository.delete(token);
                        throw new BadRequestException("Too many invalid attempts. Please request a new OTP.");
                }

                // N-S1: compare against the stored BCrypt hash, not a plaintext value.
                if (!passwordEncoder.matches(otp, token.getToken())) {
                        token.incrementAttempt();
                        if (token.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
                                passwordResetTokenRepository.delete(token); // lock out → force re-generation
                        } else {
                                passwordResetTokenRepository.save(token);
                        }
                        throw new BadRequestException("Invalid OTP");
                }

                // Reset Password
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setLastPasswordChangeDate(LocalDateTime.now());
                // N-S5: invalidate every JWT issued before this reset (a leaked/old token is now dead).
                user.incrementTokenVersion();
                userRepository.save(user);

                // Delete used token
                passwordResetTokenRepository.delete(token);
        }

        private static final int MAX_OTP_ATTEMPTS = 5;

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
