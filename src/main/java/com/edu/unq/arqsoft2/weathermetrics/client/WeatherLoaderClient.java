package com.edu.unq.arqsoft2.weathermetrics.client;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class WeatherLoaderClient {

    public WeatherData loadWeather() {
        double temp = 15 + new Random().nextDouble() * 10;
        return new WeatherData("Sample City", temp);
    }

}
