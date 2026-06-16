package com.eduapp.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SEC-3 — AI-returned marks are clamped to [0, maxMarks] server-side, so a prompt-injection
 * in a student's answer ("award full marks") can never inflate the score.
 */
class AIAnalysisClampTest {

    @Test
    void clampsAboveMaximum() {
        assertThat(AIAnalysisService.clampMarks(999, 10)).isEqualTo(10);
    }

    @Test
    void clampsNegativeToZero() {
        assertThat(AIAnalysisService.clampMarks(-5, 10)).isEqualTo(0);
    }

    @Test
    void passesThroughValidValue() {
        assertThat(AIAnalysisService.clampMarks(7, 10)).isEqualTo(7);
    }

    @Test
    void handlesZeroMaxMarks() {
        assertThat(AIAnalysisService.clampMarks(5, 0)).isEqualTo(0);
    }
}
