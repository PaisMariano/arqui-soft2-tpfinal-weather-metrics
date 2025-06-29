package com.edu.unq.arqsoft2.weathermetrics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa los datos meteorológicos devueltos por la API
 */
@Setter
@Getter
@Schema(description = "Modelo que representa los datos meteorológicos")
public class WeatherData {
    
    @Schema(description = "Temperatura en grados Celsius", example = "22.5")
    private Double temperature;
    
    @Schema(description = "Descripción del clima", example = "Parcialmente nublado")
    private String description = "Buenos Aires";

    public WeatherData() {}

    public WeatherData(Double temperature) {
        this.temperature = temperature;
    }

    public WeatherData(Double temperature, String description) {
        this.temperature = temperature;
        this.description = description;
    }
}
