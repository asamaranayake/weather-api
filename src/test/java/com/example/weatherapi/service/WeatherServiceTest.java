package com.example.weatherapi.service;

import com.example.weatherapi.model.MainData;
import com.example.weatherapi.model.Weather;
import com.example.weatherapi.model.WeatherData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(weatherService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(weatherService, "apiUrl", "http://test.api.url");
    }

    @Test
    public void testGetCurrentWeather() {
        // Arrange
        String city = "London";
        WeatherData mockData = new WeatherData();
        mockData.setName(city);
        
        MainData mainData = new MainData();
        mainData.setTemp(20.5);
        mainData.setHumidity(70);
        mockData.setMain(mainData);
        
        Weather weather = new Weather();
        weather.setMain("Clouds");
        weather.setDescription("scattered clouds");
        mockData.setWeather(Collections.singletonList(weather));
        
        when(restTemplate.getForObject(anyString(), eq(WeatherData.class))).thenReturn(mockData);
        
        // Act
        WeatherData result = weatherService.getCurrentWeather(city);
        
        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), city);
        assertEquals(result.getMain().getTemp(), 20.5);
        assertEquals(result.getWeather().get(0).getMain(), "Clouds");
    }
} 