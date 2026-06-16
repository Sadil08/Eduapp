package com.eduapp.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.MediaType;

/**
 * SEC-1 — the backend must authenticate to the Python extraction service with X-API-Key on
 * every call, so the service can reject anything that didn't come through this backend.
 */
class AIServiceApiKeyTest {

    private MockRestServiceServer bindMockServer(AIService service) {
        RestTemplate rt = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        return MockRestServiceServer.createServer(rt);
    }

    @Test
    void sendsApiKeyHeader_whenConfigured() {
        AIService service = new AIService(null);
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://ai:8000");
        ReflectionTestUtils.setField(service, "aiServiceApiKey", "secret-123");
        MockRestServiceServer server = bindMockServer(service);

        server.expect(requestTo("http://ai:8000/extract"))
                .andExpect(header("X-API-Key", "secret-123"))
                .andRespond(withSuccess("{\"extracted_text\":\"hi\"}", MediaType.APPLICATION_JSON));

        String out = service.extractTextFromImage(
                new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3}));

        assertThat(out).isEqualTo("hi");
        server.verify();
    }

    @Test
    void omitsApiKeyHeader_whenNotConfigured() {
        AIService service = new AIService(null);
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://ai:8000");
        ReflectionTestUtils.setField(service, "aiServiceApiKey", "");
        MockRestServiceServer server = bindMockServer(service);

        server.expect(requestTo("http://ai:8000/extract"))
                .andExpect(headerDoesNotExist("X-API-Key"))
                .andRespond(withSuccess("{\"extracted_text\":\"hi\"}", MediaType.APPLICATION_JSON));

        service.extractTextFromImage(
                new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3}));
        server.verify();
    }

    // SCALE-1: when the bulkhead is exhausted, the call fast-fails WITHOUT hitting the network.
    @Test
    void bulkheadFull_fastFails_withoutCallingAiService() throws Exception {
        AIService service = new AIService(null);
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://ai:8000");
        ReflectionTestUtils.setField(service, "maxConcurrentAiCalls", 1);
        ReflectionTestUtils.setField(service, "acquireTimeoutMs", 50L); // fast fail
        service.initBulkhead();
        // Drain the only permit so the next call cannot acquire a slot.
        java.util.concurrent.Semaphore slots =
                (java.util.concurrent.Semaphore) ReflectionTestUtils.getField(service, "aiSlots");
        slots.acquire();

        MockRestServiceServer server = bindMockServer(service);
        // No request is expected — the bulkhead rejects before any HTTP call.

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                service.extractTextFromImage(
                        new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3})));
        server.verify(); // zero requests
    }

    // SCALE-3: after enough consecutive failures the circuit opens and the NEXT call fast-fails
    // without touching the network.
    @Test
    void circuitOpens_afterRepeatedFailures_thenFastFails() {
        AIService service = new AIService(null);
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://ai:8000");
        ReflectionTestUtils.setField(service, "maxConcurrentAiCalls", 4);
        ReflectionTestUtils.setField(service, "acquireTimeoutMs", 500L);
        ReflectionTestUtils.setField(service, "cbFailureThreshold", 2);
        ReflectionTestUtils.setField(service, "cbOpenMs", 30000L);
        service.initBulkhead();

        RestTemplate rt = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.createServer(rt);
        // Exactly 2 requests will hit the server (both 500); the 3rd is blocked by the open circuit.
        server.expect(org.springframework.test.web.client.ExpectedCount.times(2),
                        requestTo("http://ai:8000/extract"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withServerError());

        MockMultipartFile f = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        for (int i = 0; i < 2; i++) {
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> service.extractTextFromImage(f));
        }
        // Circuit now open → fast-fail, no 3rd HTTP call.
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> service.extractTextFromImage(f));
        server.verify(); // only the 2 expected requests happened
    }
}
