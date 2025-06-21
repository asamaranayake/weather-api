package com.example.weatherapi.controller;

import com.example.weatherapi.model.WeatherData;
import com.example.weatherapi.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WeatherController {
    
    private final WeatherService weatherService;
    
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    // Simple weather API endpoint
    @GetMapping("/weather-api")
    public ResponseEntity<Map<String, Object>> getWeatherApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Weather API");
        response.put("status", "running");
        response.put("version", "1.0");
        response.put("endpoints", Map.of(
            "weather", "/weather-api/weather/{city}",
            "health", "/actuator/health",
            "info", "/actuator/info"
        ));
        return ResponseEntity.ok(response);
    }
    
    // Weather data endpoint
    @GetMapping("/weather-api/weather/{city}")
    public ResponseEntity<?> getCurrentWeather(@PathVariable String city) {
        try {
            WeatherData weatherData = weatherService.getCurrentWeather(city);
            return ResponseEntity.ok(weatherData);
        } catch (HttpClientErrorException.NotFound e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "City not found: " + city);
            error.put("message", "Please check the city name and try again");
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Error fetching weather data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    // Simple health check
    @GetMapping("/weather-api/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("application", "Weather API");
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
} 