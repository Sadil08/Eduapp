package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.repository.PaperBundleRepository;
import com.eduapp.backend.repository.PaperRepository;

@ExtendWith(MockitoExtension.class)
public class PaperServiceTest {

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private PaperBundleRepository paperBundleRepository;

    @InjectMocks
    private PaperService paperService;

    @Test
    void save_Success() {
        // Arrange
        PaperBundle bundle = new PaperBundle();
        bundle.setId(1L);
        Paper paper = new Paper();
        paper.setName("Test Paper");
        paper.setBundle(bundle);

        Paper savedPaper = new Paper();
        savedPaper.setId(10L);

        when(paperBundleRepository.findById(1L)).thenReturn(Optional.of(bundle));
        when(paperRepository.save(paper)).thenReturn(savedPaper);

        // Act
        Paper result = paperService.save(paper);

        // Assert
        assertThat(result.getId()).isEqualTo(10L);
        verify(paperRepository).save(any(Paper.class));
    }

    @Test
    void save_BundleNotFound_ThrowsException() {
        // Arrange
        PaperBundle bundle = new PaperBundle();
        bundle.setId(1L);
        Paper paper = new Paper();
        paper.setName("Test Paper");
        paper.setBundle(bundle);

        when(paperBundleRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paperService.save(paper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("PaperBundle does not exist");
    }
}