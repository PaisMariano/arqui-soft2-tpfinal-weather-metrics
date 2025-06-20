package com.edu.unq.arqsoft2.weathermetrics.service;

import com.edu.unq.arqsoft2.weathermetrics.client.WeatherLoaderClient;
import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherMetricsService {
    private final WeatherLoaderClient loader;
    private final List<Double> recentTemps = new ArrayList<>();
    private final MeterRegistry meterRegistry;

    public WeatherMetricsService(WeatherLoaderClient loader, MeterRegistry meterRegistry) {
        this.loader = loader;
        this.meterRegistry = meterRegistry;
    }

    @CircuitBreaker(name = "WeatherMetricsService", fallbackMethod = "fallbackWeather")
    @Bulkhead(name = "WeatherMetricsService")
    @TimeLimiter(name = "WeatherMetricsService")
    public CompletableFuture<WeatherData> getCurrentWeather() {
        WeatherData data = loader.getCurrentWeather();
        synchronized (recentTemps) {
            recentTemps.add(data.getTemperature());
            meterRegistry.gauge("weather_temperature_current", data.getTemperature());
            if (recentTemps.size() > 168) recentTemps.remove(0);
        }

        return CompletableFuture.completedFuture(data);
    }

    public CompletableFuture<WeatherData> getAverageTemperatureByDay() {
        WeatherData data = loader.getAverageByDayWeather();
        synchronized (recentTemps) {
            recentTemps.add(data.getTemperature());
            meterRegistry.gauge("weather_temperature_avg_day", data.getTemperature());
            if (recentTemps.size() > 168) recentTemps.remove(0);
        }

        return CompletableFuture.completedFuture(data);
    }

    public CompletableFuture<WeatherData> getAverageTemperatureByWeek() {
        WeatherData data = loader.getAverageByDayWeather();
        synchronized (recentTemps) {
            recentTemps.add(data.getTemperature());
            meterRegistry.gauge("weather_temperature_avg_week", data.getTemperature());
            if (recentTemps.size() > 168) recentTemps.remove(0);
        }

        return CompletableFuture.completedFuture(data);
    }

    private CompletableFuture<WeatherData> fallbackWeather(Throwable t) {
        return CompletableFuture.completedFuture(new WeatherData(0.0));
    }
}
