package com.eduapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoLocationService {

    private static final Logger logger = LoggerFactory.getLogger(GeoLocationService.class);
    private static final String GEO_API_URL = "http://ip-api.com/json/";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getCountryFromIp(String ip) {
        if (ip == null || ip.isEmpty() || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "Localhost";
        }

        try {
            // Using ip-api.com (free tier, no key required for basic usage)
            // Response is JSON: {"status":"success","country":"United States",...}
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(GEO_API_URL + ip, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            logger.error("Failed to resolve country for IP {}: {}", ip, e.getMessage());
        }

        return "Unknown";
    }
}
