package com.eduapp.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration to handle Hibernate lazy-loading proxies properly.
 * Prevents "ByteBuddyInterceptor" serialization errors when returning
 * entities that have uninitialized lazy associations.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        // Don't serialize uninitialized lazy-loaded properties
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
        // Replace lazy proxies with null instead of failing
        module.configure(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS, true);
        return module;
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.registerModule(hibernate6Module());
        return objectMapper;
    }
}
