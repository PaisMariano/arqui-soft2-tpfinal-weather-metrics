package com.edu.unq.arqsoft2.weathermetrics.model;

public class WeatherData {
    private Double temperature;

    public WeatherData() {}

    public WeatherData(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
