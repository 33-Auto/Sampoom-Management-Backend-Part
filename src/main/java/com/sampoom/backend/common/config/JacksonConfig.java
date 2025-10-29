package com.sampoom.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Hibernate Lazy 프록시 무시
        mapper.registerModule(new Hibernate6Module());
        // Java 8 시간 API 지원 (LocalDateTime 등)
        mapper.registerModule(new JavaTimeModule());
        // 비어 있는 Bean 직렬화 오류 방지
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // LocalDateTime을 타임스탬프가 아닌 문자열로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
