package com.eduapp.backend.service;

import com.eduapp.backend.dto.ImprovementDto;
import com.eduapp.backend.dto.ImprovementSubmissionDto;
import com.eduapp.backend.model.Improvement;
import com.eduapp.backend.model.ImprovementStatus;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.ImprovementRepository;
import com.eduapp.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImprovementService {

    private static final Logger logger = LoggerFactory.getLogger(ImprovementService.class);

    private final ImprovementRepository improvementRepository;
    private final UserRepository userRepository;

    public ImprovementService(ImprovementRepository improvementRepository, UserRepository userRepository) {
        this.improvementRepository = improvementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ImprovementDto submitImprovement(Long userId, ImprovementSubmissionDto dto) {
        logger.info("User {} submitting improvement suggestion", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Improvement improvement = new Improvement(
                user,
                dto.getImprovementText(),
                dto.getIssueDescription());

        improvement = improvementRepository.save(improvement);
        logger.info("Improvement submitted successfully: ID {}", improvement.getId());

        return mapToDto(improvement);
    }

    public List<ImprovementDto> getUserImprovements(Long userId) {
        logger.info("Fetching improvements for user {}", userId);
        List<Improvement> improvements = improvementRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return improvements.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ImprovementDto> getAllImprovementsForAdmin() {
        logger.info("Fetching all improvements for admin");
        List<Improvement> improvements = improvementRepository.findAllByOrderByCreatedAtDesc();
        return improvements.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ImprovementDto> getImprovementsByStatus(ImprovementStatus status) {
        logger.info("Fetching improvements with status: {}", status);
        List<Improvement> improvements = improvementRepository.findByStatusOrderByCreatedAtDesc(status);
        return improvements.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public ImprovementDto updateImprovementStatus(Long improvementId, ImprovementStatus status,
            Long adminId, String adminNotes) {
        logger.info("Admin {} updating improvement {} to status {}", adminId, improvementId, status);

        Improvement improvement = improvementRepository.findById(improvementId)
                .orElseThrow(() -> new IllegalArgumentException("Improvement not found"));

        improvement.setStatus(status);
        improvement.setReviewedBy(adminId);
        improvement.setAdminNotes(adminNotes);

        improvement = improvementRepository.save(improvement);
        logger.info("Improvement {} updated successfully", improvementId);

        return mapToDto(improvement);
    }

    @Transactional
    public void deleteImprovement(Long improvementId, Long adminId) {
        logger.info("Admin {} deleting improvement {}", adminId, improvementId);

        Improvement improvement = improvementRepository.findById(improvementId)
                .orElseThrow(() -> new IllegalArgumentException("Improvement not found"));

        improvementRepository.delete(improvement);
        logger.info("Improvement {} deleted successfully", improvementId);
    }

    private ImprovementDto mapToDto(Improvement improvement) {
        return new ImprovementDto(
                improvement.getId(),
                improvement.getUser().getId(),
                improvement.getUser().getName(),
                improvement.getUser().getEmail(),
                improvement.getImprovementText(),
                improvement.getIssueDescription(),
                improvement.getStatus(),
                improvement.getAdminNotes(),
                improvement.getCreatedAt(),
                improvement.getUpdatedAt());
    }
}
