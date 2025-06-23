package com.edu.unq.arqsoft2.weathermetrics.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Setter
@Getter
@Document(collection = "weather_cache")
public class WeatherCache {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String cacheKey;
    private WeatherData data;
    private LocalDateTime lastUpdated;
    private LocalDateTime expiryTime;

    public WeatherCache() {}

    public WeatherCache(String cacheKey, WeatherData data, long ttlMinutes) {
        this.cacheKey = cacheKey;
        this.data = data;
        this.lastUpdated = LocalDateTime.now();
        this.expiryTime = this.lastUpdated.plusMinutes(ttlMinutes);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
