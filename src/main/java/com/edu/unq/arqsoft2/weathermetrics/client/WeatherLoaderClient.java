package com.edu.unq.arqsoft2.weathermetrics.client;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Component
public class WeatherLoaderClient {
    @Autowired
    private RestTemplate restTemplate;

    public WeatherData getCurrentWeather() {
        String url = "http://localhost:8080/api/weather-loader/search/current";

        return restTemplate.getForObject(url, WeatherData.class);

    }

    public WeatherData getAverageByDayWeather() {
        String url = "http://localhost:8080/api/weather-loader/search/avg/day";

        return restTemplate.getForObject(url, WeatherData.class);

    }

    public WeatherData getAverageByWeekWeather() {
        String url = "http://localhost:8080/api/weather-loader/search/avg/week";

        return restTemplate.getForObject(url, WeatherData.class);

    }

}
