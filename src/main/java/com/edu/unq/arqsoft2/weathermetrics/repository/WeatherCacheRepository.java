package com.edu.unq.arqsoft2.weathermetrics.repository;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherCache;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WeatherCacheRepository extends MongoRepository<WeatherCache, String> {
    Optional<WeatherCache> findByCacheKey(String cacheKey);
    void deleteByCacheKey(String cacheKey);
}
