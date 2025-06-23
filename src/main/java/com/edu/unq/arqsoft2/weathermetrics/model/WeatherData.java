package com.edu.unq.arqsoft2.weathermetrics.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WeatherData {
    private Double temperature;
    private String description;

    public WeatherData() {}

    public WeatherData(Double temperature) {
        this.temperature = temperature;
    }

    public WeatherData(Double temperature, String description) {
        this.temperature = temperature;
        this.description = description;
    }

}
