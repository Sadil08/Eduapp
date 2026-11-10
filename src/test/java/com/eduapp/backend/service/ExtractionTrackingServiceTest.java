package com.eduapp.backend.service;

import com.eduapp.backend.exception.ExtractionLimitExceededException;
import com.eduapp.backend.model.QuestionExtractionTracking;
import com.eduapp.backend.repository.QuestionExtractionTrackingRepository;
import com.eduapp.backend.repository.QuestionRepository;
import com.eduapp.backend.repository.StudentPaperAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NFR-RACE-1: proves the extraction limiter is now race-safe via an atomic conditional increment.
 * The old code did {@code canExtract()} then a separate {@code save(incrementedCount)} — two
 * concurrent requests could both pass the check and exceed the per-question cap (extra AI cost).
 * The service must now translate the atomic UPDATE's row-count into allow/deny, and release a slot
 * if the AI call fails.
 */
@ExtendWith(MockitoExtension.class)
class ExtractionTrackingServiceTest {

    @Mock QuestionExtractionTrackingRepository trackingRepository;
    @Mock StudentPaperAttemptRepository attemptRepository;
    @Mock QuestionRepository questionRepository;

    @InjectMocks ExtractionTrackingService service;

    private QuestionExtractionTracking tracking(long id, int count, int max) {
        QuestionExtractionTracking t = new QuestionExtractionTracking();
        t.setId(id);
        t.setExtractionCount(count);
        t.setMaxExtractions(max);
        return t;
    }

    @Test
    void reserve_succeedsWhenAtomicIncrementGrantsASlot() {
        when(trackingRepository.findByAttemptIdAndQuestionId(1L, 2L))
                .thenReturn(Optional.of(tracking(5L, 1, 2)));
        when(trackingRepository.tryIncrementCount(5L)).thenReturn(1); // slot granted

        assertThatCode(() -> service.reserveExtraction(1L, 2L)).doesNotThrowAnyException();
        verify(trackingRepository).tryIncrementCount(5L);
    }

    @Test
    void reserve_throwsWhenAtLimit_andDoesNotConsumeASlot() {
        when(trackingRepository.findByAttemptIdAndQuestionId(1L, 2L))
                .thenReturn(Optional.of(tracking(5L, 2, 2)));
        when(trackingRepository.tryIncrementCount(5L)).thenReturn(0); // limit reached → no row updated

        assertThatThrownBy(() -> service.reserveExtraction(1L, 2L))
                .isInstanceOf(ExtractionLimitExceededException.class);
    }

    @Test
    void release_decrementsAReservedSlot() {
        when(trackingRepository.findByAttemptIdAndQuestionId(1L, 2L))
                .thenReturn(Optional.of(tracking(5L, 2, 2)));
        when(trackingRepository.decrementCount(5L)).thenReturn(1);

        service.releaseExtraction(1L, 2L);
        verify(trackingRepository).decrementCount(5L);
    }

    @Test
    void release_noopWhenNoTrackingRow() {
        when(trackingRepository.findByAttemptIdAndQuestionId(9L, 9L)).thenReturn(Optional.empty());

        service.releaseExtraction(9L, 9L);
        verify(trackingRepository, never()).decrementCount(org.mockito.ArgumentMatchers.anyLong());
    }
}
