package com.eduapp.backend.service;

import com.eduapp.backend.dto.PaperBundleDto;
import com.eduapp.backend.model.Paper;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.PaperType;
import com.eduapp.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit coverage for the admin bundle-management service — the "admin creates bundles and adds
 * papers to bundles" flow. Pure Mockito (no Spring context / DB).
 *
 * <p>The composition methods ({@code addPaperToBundle}/{@code removePaperFromBundle}) are the
 * many-to-many wiring that gates which papers a purchaser can attempt, so their not-found and
 * idempotency behaviour is verified explicitly.
 */
@ExtendWith(MockitoExtension.class)
class AdminBundleServiceTest {

    @Mock PaperBundleRepository bundleRepository;
    @Mock PaperRepository paperRepository;
    @Mock StudentBundleAccessRepository accessRepository;
    @Mock StudentPaperAttemptRepository attemptRepository;
    @Mock QuestionRepository questionRepository;
    @Mock UserRepository userRepository;
    @Mock ExamTypeRepository examTypeRepository;
    @Mock CustomBundleRepository customBundleRepository;

    @InjectMocks AdminBundleService service;

    private PaperBundleDto bundleDto(String name) {
        PaperBundleDto dto = new PaperBundleDto();
        dto.setName(name);
        dto.setDescription("desc");
        dto.setPrice(new BigDecimal("499.00"));
        dto.setType(PaperType.ESSAY);
        dto.setIsPastPaper(false);
        return dto;
    }

    @Test
    void createBundle_persistsMappedFields() {
        when(bundleRepository.save(any(PaperBundle.class))).thenAnswer(i -> i.getArgument(0));

        service.createBundle(bundleDto("O/L Maths 2024"), null);

        ArgumentCaptor<PaperBundle> captor = ArgumentCaptor.forClass(PaperBundle.class);
        verify(bundleRepository).save(captor.capture());
        PaperBundle saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("O/L Maths 2024");
        assertThat(saved.getPrice()).isEqualByComparingTo("499.00");
        assertThat(saved.getType()).isEqualTo(PaperType.ESSAY);
    }

    @Test
    void updateBundle_whenMissing_throws() {
        when(bundleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBundle(99L, bundleDto("x")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(bundleRepository, never()).save(any());
    }

    @Test
    void deleteBundle_whenMissing_throws() {
        when(bundleRepository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteBundle(5L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(bundleRepository, never()).deleteById(any());
    }

    @Test
    void addPaperToBundle_linksPaperAndSaves() {
        PaperBundle bundle = new PaperBundle();
        bundle.setId(10L);
        bundle.setName("bundle");
        Paper paper = new Paper();
        paper.setName("paper");
        paper.setBundles(new ArrayList<>());

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(bundle));
        when(paperRepository.findById(20L)).thenReturn(Optional.of(paper));

        service.addPaperToBundle(10L, 20L);

        assertThat(paper.getBundles()).contains(bundle);
        verify(paperRepository).save(paper);
    }

    @Test
    void addPaperToBundle_whenBundleMissing_throws() {
        when(bundleRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addPaperToBundle(10L, 20L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(paperRepository, never()).save(any());
    }

    @Test
    void addPaperToBundle_isIdempotent_whenAlreadyLinked() {
        PaperBundle bundle = new PaperBundle();
        bundle.setId(10L);
        bundle.setName("bundle");
        Paper paper = new Paper();
        paper.setName("paper");
        ArrayList<PaperBundle> bundles = new ArrayList<>();
        bundles.add(bundle);
        paper.setBundles(bundles);

        when(bundleRepository.findById(10L)).thenReturn(Optional.of(bundle));
        when(paperRepository.findById(20L)).thenReturn(Optional.of(paper));

        service.addPaperToBundle(10L, 20L);

        // already present → no duplicate insert, no save
        assertThat(paper.getBundles()).containsExactly(bundle);
        verify(paperRepository, never()).save(any());
    }

    @Test
    void removePaperFromBundle_whenNotInBundle_throws() {
        Paper paper = new Paper();
        paper.setName("paper");
        paper.setBundles(new ArrayList<>());
        when(paperRepository.findById(20L)).thenReturn(Optional.of(paper));

        assertThatThrownBy(() -> service.removePaperFromBundle(10L, 20L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
