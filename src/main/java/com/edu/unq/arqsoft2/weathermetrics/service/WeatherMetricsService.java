package com.edu.unq.arqsoft2.weathermetrics.service;

import com.edu.unq.arqsoft2.weathermetrics.client.WeatherLoaderClient;
import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherMetricsService {
    private final WeatherLoaderClient loader;
    private final List<Double> recentTemps = new ArrayList<>();

    public WeatherMetricsService(WeatherLoaderClient loader) {
        this.loader = loader;
    }

    @CircuitBreaker(name = "WeatherMetricsService", fallbackMethod = "fallbackWeather")
    @Bulkhead(name = "WeatherMetricsService")
    @TimeLimiter(name = "WeatherMetricsService")
    public CompletableFuture<WeatherData> getCurrentWeather() {
        WeatherData data = loader.loadWeather();
        synchronized (recentTemps) {
            recentTemps.add(data.getTemperature());
            if (recentTemps.size() > 168) recentTemps.remove(0);
        }
        return CompletableFuture.completedFuture(data);
    }

    public double getAverageTemperature(int days) {
        int limit = Math.min(days * 24, recentTemps.size());
        return recentTemps.stream().skip(recentTemps.size() - limit).mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private CompletableFuture<WeatherData> fallbackWeather(Throwable t) {
        return CompletableFuture.completedFuture(new WeatherData("Fallback", 0.0));
    }
}
