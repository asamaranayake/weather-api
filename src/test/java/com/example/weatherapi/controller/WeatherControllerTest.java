package com.example.weatherapi.controller;

import com.example.weatherapi.model.MainData;
import com.example.weatherapi.model.Weather;
import com.example.weatherapi.model.WeatherData;
import com.example.weatherapi.service.WeatherService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private AutoCloseable closeable;

    @BeforeMethod
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        // Reset the mock after each test
        reset(weatherService);
    }

    @Test
    public void testGetCurrentWeather_Success() {
        // Arrange
        String city = "London";
        WeatherData mockData = createMockWeatherData(city);
        
        // Use exact matching with 'eq' for the city parameter
        when(weatherService.getCurrentWeather(eq(city))).thenReturn(mockData);
        
        // Act
        ResponseEntity<?> response = weatherController.getCurrentWeather(city);
        
        // Assert
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        WeatherData result = (WeatherData) response.getBody();
        assertEquals(result.getName(), city);
    }

    @Test
    public void testGetCurrentWeather_NotFound() {
        // Arrange
        String city = "NonExistentCity";
        
        // Use exact matching for the city parameter
        when(weatherService.getCurrentWeather(eq(city)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        // Act
        ResponseEntity<?> response = weatherController.getCurrentWeather(city);
        
        // Assert
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }
    
    private WeatherData createMockWeatherData(String cityName) {
        WeatherData mockData = new WeatherData();
        mockData.setName(cityName);
        
        MainData mainData = new MainData();
        mainData.setTemp(20.5);
        mockData.setMain(mainData);
        
        Weather weather = new Weather();
        weather.setDescription("Cloudy");
        mockData.setWeather(Collections.singletonList(weather));
        
        return mockData;
    }
} 