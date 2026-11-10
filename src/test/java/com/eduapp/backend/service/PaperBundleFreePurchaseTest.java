package com.eduapp.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.eduapp.backend.exception.PaymentRequiredException;
import com.eduapp.backend.model.PaperBundle;
import com.eduapp.backend.model.StudentBundleAccess;
import com.eduapp.backend.model.User;
import com.eduapp.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** SEC-01: the no-payment purchase path must only grant free bundles. */
class PaperBundleFreePurchaseTest {

    private PaperBundleRepository bundles;
    private StudentBundleAccessRepository access;
    private UserRepository users;
    private PaperBundleService service;

    @BeforeEach
    void setUp() {
        bundles = mock(PaperBundleRepository.class);
        access = mock(StudentBundleAccessRepository.class);
        users = mock(UserRepository.class);
        service = new PaperBundleService(bundles, mock(SubjectRepository.class), mock(LessonRepository.class),
                access, users, mock(com.eduapp.backend.mapper.PaperBundleMapper.class),
                mock(ExamTypeRepository.class));
    }

    @Test
    void paidBundle_isRejectedWithoutGrantingAccess() {
        PaperBundle paid = new PaperBundle();
        paid.setPrice(new BigDecimal("9.99"));
        when(bundles.findById(1L)).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> service.purchaseBundle(1L, 7L)).isInstanceOf(PaymentRequiredException.class);
        verify(access, never()).save(any());
    }

    @Test
    void freeBundle_grantsAccess() {
        PaperBundle free = new PaperBundle();
        free.setPrice(BigDecimal.ZERO);
        when(bundles.findById(2L)).thenReturn(Optional.of(free));
        when(access.existsByStudentIdAndBundleId(7L, 2L)).thenReturn(false);
        when(users.findById(7L)).thenReturn(Optional.of(new User()));

        service.purchaseBundle(2L, 7L);

        verify(access).save(any(StudentBundleAccess.class));
    }
}
