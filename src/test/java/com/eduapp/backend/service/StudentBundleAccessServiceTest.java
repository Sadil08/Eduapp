package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.repository.StudentBundleAccessRepository;

@ExtendWith(MockitoExtension.class)
public class StudentBundleAccessServiceTest {

    @Mock
    private StudentBundleAccessRepository studentBundleAccessRepository;

    @InjectMocks
    private StudentBundleAccessService studentBundleAccessService;

    @Test
    void findAll_ReturnsList() {
        // Arrange
        List<StudentBundleAccess> accesses = List.of(new StudentBundleAccess());
        when(studentBundleAccessRepository.findAll()).thenReturn(accesses);

        // Act
        List<StudentBundleAccess> result = studentBundleAccessService.findAll();

        // Assert
        assertThat(result).hasSize(1);
    }
}