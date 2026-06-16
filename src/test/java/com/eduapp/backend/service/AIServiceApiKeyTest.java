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
}
