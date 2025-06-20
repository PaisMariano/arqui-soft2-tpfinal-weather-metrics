package com.edu.unq.arqsoft2.weathermetrics.controller;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import com.edu.unq.arqsoft2.weathermetrics.service.WeatherMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherMetricsController {
    private final WeatherMetricsService weatherService;

    public WeatherMetricsController(WeatherMetricsService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public WeatherData getCurrent() {
        return weatherService.getCurrentWeather().join();
    }

    @GetMapping("/avg/day")
    public WeatherData getAvgDay() {
        return weatherService.getAverageTemperatureByDay().join();
    }

    @GetMapping("/avg/week")
    public WeatherData getAvgWeek() {
        return weatherService.getAverageTemperatureByWeek().join();
    }

}
