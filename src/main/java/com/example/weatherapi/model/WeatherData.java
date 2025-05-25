package com.example.weatherapi.model;

import java.util.List;

public class WeatherData {
    private MainData main;
    private List<Weather> weather;
    private Wind wind;
    private String name;

    // Getters and Setters
    public MainData getMain() { 
        return main; 
    }
    
    public void setMain(MainData main) { 
        this.main = main; 
    }
    
    public List<Weather> getWeather() { 
        return weather; 
    }
    
    public void setWeather(List<Weather> weather) { 
        this.weather = weather; 
    }
    
    public Wind getWind() { 
        return wind; 
    }
    
    public void setWind(Wind wind) { 
        this.wind = wind; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
} 