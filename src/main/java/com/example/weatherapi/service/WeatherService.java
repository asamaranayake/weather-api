package com.example.weatherapi.service;

import com.example.weatherapi.model.WeatherData;

public interface WeatherService {
    WeatherData getCurrentWeather(String city);
} 