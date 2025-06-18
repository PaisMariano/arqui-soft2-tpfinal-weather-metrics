package com.edu.unq.arqsoft2.weathermetrics.config;


import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    @Bean
    public CircuitBreakerConfigCustomizer weatherServiceCustomizer() {
        return CircuitBreakerConfigCustomizer.of("weatherService",
                builder -> builder.slidingWindowSize(10).failureRateThreshold(50));
    }
}
