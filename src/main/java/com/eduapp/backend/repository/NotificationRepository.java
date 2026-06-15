package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // N-SC2-A: scope notifications to their owner.
    List<Notification> findByUserId(Long userId);
}