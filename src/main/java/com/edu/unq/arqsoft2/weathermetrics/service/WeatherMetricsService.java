package com.edu.unq.arqsoft2.weathermetrics.service;

import com.edu.unq.arqsoft2.weathermetrics.client.WeatherLoaderClient;
import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Service
public class WeatherMetricsService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherMetricsService.class);
    private static final long CACHE_TTL_MINUTES = 1;
    
    private final WeatherLoaderClient loader;
    private final WeatherCacheService cacheService;
    private final List<Double> recentTemps = new ArrayList<>();
    private final MeterRegistry meterRegistry;

    @Autowired
    public WeatherMetricsService(
            WeatherLoaderClient loader, 
            MeterRegistry meterRegistry,
            WeatherCacheService cacheService) {
        this.loader = loader;
        this.meterRegistry = meterRegistry;
        this.cacheService = cacheService;
    }

    @CircuitBreaker(name = "weatherService", fallbackMethod = "fallbackWeather")
    @Bulkhead(name = "weatherService")
    @TimeLimiter(name = "weatherService")
    @Retry(name = "weatherService")
    public CompletableFuture<WeatherData> getCurrentWeather() {
        log.info("Fetching current weather data");
        String cacheKey = "current_weather";
        
        Supplier<WeatherData> dataSupplier = () -> {
            logger.info("Fetching fresh current weather data");
            WeatherData data = loader.getCurrentWeather();
            updateMetrics(data, "weather_temperature_current");
            return data;
        };
        
        WeatherData data = cacheService.getCachedOrFetch(cacheKey, dataSupplier, CACHE_TTL_MINUTES);
        return CompletableFuture.completedFuture(data);
    }

    @CircuitBreaker(name = "weatherService", fallbackMethod = "fallbackWeather")
    @Bulkhead(name = "weatherService")
    @TimeLimiter(name = "weatherService")
    @Retry(name = "weatherService")
    public CompletableFuture<WeatherData> getAverageTemperatureByDay() {
        String cacheKey = "avg_day_weather";
        
        Supplier<WeatherData> dataSupplier = () -> {
            logger.info("Fetching fresh average day weather data");
            WeatherData data = loader.getAverageByDayWeather();
            updateMetrics(data, "weather_temperature_avg_day");
            return data;
        };
        
        WeatherData data = cacheService.getCachedOrFetch(cacheKey, dataSupplier, CACHE_TTL_MINUTES);
        return CompletableFuture.completedFuture(data);
    }

    @CircuitBreaker(name = "weatherService", fallbackMethod = "fallbackWeather")
    @Retry(name = "weatherService")
    public CompletableFuture<WeatherData> getAverageTemperatureByWeek() {
        String cacheKey = "avg_week_weather";

        Supplier<WeatherData> dataSupplier = () -> {
            logger.info("Fetching fresh average week weather data");
            WeatherData data = loader.getAverageByWeekWeather();
            updateMetrics(data, "weather_temperature_avg_week");
            return data;
        };

        WeatherData data = cacheService.getCachedOrFetch(cacheKey, dataSupplier, CACHE_TTL_MINUTES);
        return CompletableFuture.completedFuture(data);
    }

    private void updateMetrics(WeatherData data, String metricName) {
        if (data != null && data.getTemperature() != null) {
            synchronized (recentTemps) {
                recentTemps.add(data.getTemperature());
                meterRegistry.gauge(metricName, data.getTemperature());
                if (recentTemps.size() > 168) recentTemps.remove(0);
            }
        }
        else {throw new RuntimeException("No se pudo obtener datos de temperatura");}
    }

    private CompletableFuture<WeatherData> fallbackWeather(Throwable t) {
        logger.warn("Fallback method called due to: {}", t.getMessage());
        // Devolver datos por defecto o lanzar una excepción personalizada
        // dependiendo de los requisitos de tu aplicación
        WeatherData fallbackData = new WeatherData(
            null,
            "Datos de respaldo - Servicio temporalmente no disponible"
        );
        return CompletableFuture.completedFuture(fallbackData);
    }
}
