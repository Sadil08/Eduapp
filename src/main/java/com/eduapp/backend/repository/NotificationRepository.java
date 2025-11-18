package com.eduapp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduapp.backend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {}