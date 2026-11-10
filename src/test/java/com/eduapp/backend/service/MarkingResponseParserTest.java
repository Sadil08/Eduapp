package com.eduapp.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NFR-PARTIAL-1: proves the marking-response parser is robust. The old code did
 * {@code qNode.get("marksAwarded").asInt()} which NPE'd on a missing field (failing the whole
 * attempt) and silently kept 0 for questions the model omitted (under-counting the score). The
 * parser must: skip malformed entries (so coverage detection can retry), never NPE, and reject
 * non-JSON.
 */
class MarkingResponseParserTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parsesValidFullResponse() throws IOException {
        String json = """
            {"questions":[
               {"questionId":1,"marksAwarded":3,"feedback":"good"},
               {"questionId":2,"marksAwarded":0,"feedback":"see notes"}
            ],"overallFeedback":"solid","totalMarks":3}
            """;
        MarkingResult r = AIAnalysisService.parseMarkingResult(json, mapper);

        assertThat(r.returnedQuestionIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(r.byQuestionId().get(1L).rawMarks()).isEqualTo(3);
        assertThat(r.byQuestionId().get(1L).feedback()).isEqualTo("good");
        assertThat(r.overallFeedback()).isEqualTo("solid");
        assertThat(r.aiTotalMarks()).isEqualTo(3);
    }

    @Test
    void skipsEntryMissingMarks_soCoverageDetectsPartial() throws IOException {
        String json = """
            {"questions":[
               {"questionId":1,"marksAwarded":3,"feedback":"ok"},
               {"questionId":2,"feedback":"no marks field"}
            ]}
            """;
        MarkingResult r = AIAnalysisService.parseMarkingResult(json, mapper);

        // Q2 is dropped (no numeric marksAwarded) → coverage check downstream will trigger a retry
        // rather than silently scoring Q2 as 0.
        assertThat(r.returnedQuestionIds()).containsExactly(1L);
    }

    @Test
    void missingFeedback_defaultsToEmpty_noNpe() throws IOException {
        String json = "{\"questions\":[{\"questionId\":5,\"marksAwarded\":2}]}";
        MarkingResult r = AIAnalysisService.parseMarkingResult(json, mapper);
        assertThat(r.byQuestionId().get(5L).feedback()).isEmpty();
    }

    @Test
    void noQuestionsArray_yieldsEmptyResult_noNpe() throws IOException {
        MarkingResult r = AIAnalysisService.parseMarkingResult("{\"overallFeedback\":\"x\"}", mapper);
        assertThat(r.returnedQuestionIds()).isEmpty();
        assertThat(r.overallFeedback()).isEqualTo("x");
    }

    @Test
    void nonJson_throws() {
        assertThatThrownBy(() -> AIAnalysisService.parseMarkingResult("not json at all", mapper))
                .isInstanceOf(IOException.class);
    }
}
