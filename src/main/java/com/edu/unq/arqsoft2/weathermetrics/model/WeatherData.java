package com.edu.unq.arqsoft2.weathermetrics.model;

public class WeatherData {
    private String city;
    private Double temperature;

    public WeatherData() {}

    public WeatherData(String city, Double temperature) {
        this.city = city;
        this.temperature = temperature;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
