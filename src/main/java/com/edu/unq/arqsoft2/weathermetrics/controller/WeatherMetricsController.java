package com.edu.unq.arqsoft2.weathermetrics.controller;

import com.edu.unq.arqsoft2.weathermetrics.model.WeatherData;
import com.edu.unq.arqsoft2.weathermetrics.service.WeatherMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "Weather Metrics", description = "API para obtener métricas meteorológicas")
public class WeatherMetricsController {
    private final WeatherMetricsService weatherService;

    public WeatherMetricsController(WeatherMetricsService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(summary = "Obtener el clima actual", description = "Devuelve los datos meteorológicos actuales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos meteorológicos obtenidos exitosamente",
            content = @Content(schema = @Schema(implementation = WeatherData.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/current")
    public WeatherData getCurrent() {
        return weatherService.getCurrentWeather().join();
    }

    @Operation(summary = "Obtener el promedio diario de temperatura", 
               description = "Devuelve el promedio de temperatura de las últimas 24 horas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Promedio diario obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = WeatherData.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/avg/day")
    public WeatherData getAvgDay() {
        return weatherService.getAverageTemperatureByDay().join();
    }

    @Operation(summary = "Obtener el promedio semanal de temperatura",
               description = "Devuelve el promedio de temperatura de los últimos 7 días")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Promedio semanal obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = WeatherData.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/avg/week")
    public WeatherData getAvgWeek() {
        return weatherService.getAverageTemperatureByWeek().join();
    }
}
