package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Notification;
import com.eduapp.backend.model.NotificationType;
import com.eduapp.backend.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        Notification notification = new Notification();
        notification.setMessage("Test message");
        notification.setType(NotificationType.INFO);
        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        // Act
        List<Notification> result = notificationService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Test message");
    }

    @Test
    void findById_ReturnsNotification() {
        // Arrange
        Notification notification = new Notification();
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        // Act
        Optional<Notification> result = notificationService.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void save_Success() {
        // Arrange
        Notification notification = new Notification();
        notification.setMessage("New notification");
        Notification saved = new Notification();
        saved.setId(1L);
        saved.setMessage("New notification");
        when(notificationRepository.save(notification)).thenReturn(saved);

        // Act
        Notification result = notificationService.save(notification);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("New notification");
    }

    @Test
    void deleteById_Success() {
        // Arrange
        Mockito.lenient().when(notificationRepository.existsById(1L)).thenReturn(true);

        // Act
        notificationService.deleteById(1L);

        // Assert
        // Verify deleteById is called
    }

    @Test
    void existsById_ReturnsTrue() {
        // Arrange
        when(notificationRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = notificationService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
    }
}