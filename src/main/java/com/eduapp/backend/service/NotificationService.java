package com.eduapp.backend.service;

import com.eduapp.backend.model.Notification;
import com.eduapp.backend.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // N-SC2-A: scoped to a single user (was an unbounded all-users findAll()).
    public List<Notification> findByUserId(Long userId) {
        logger.info("Fetching notifications for user {}", userId);
        return notificationRepository.findByUserId(userId);
    }

    public Optional<Notification> findById(Long id) {
        logger.info("Fetching notification with ID: {}", id);
        return notificationRepository.findById(id);
    }

    public Notification save(Notification notification) {
        logger.info("Saving notification");
        return notificationRepository.save(notification);
    }

    public void deleteById(Long id) {
        logger.info("Deleting notification with ID: {}", id);
        notificationRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return notificationRepository.existsById(id);
    }
}