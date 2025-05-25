package com.example.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MainData {
    private double temp;
    
    @JsonProperty("feels_like")
    private double feelsLike;
    
    @JsonProperty("temp_min")
    private double tempMin;
    
    @JsonProperty("temp_max")
    private double tempMax;
    
    private int humidity;

    // Getters and Setters
    public double getTemp() { 
        return temp; 
    }
    
    public void setTemp(double temp) { 
        this.temp = temp; 
    }
    
    public double getFeelsLike() { 
        return feelsLike; 
    }
    
    public void setFeelsLike(double feelsLike) { 
        this.feelsLike = feelsLike; 
    }
    
    public double getTempMin() { 
        return tempMin; 
    }
    
    public void setTempMin(double tempMin) { 
        this.tempMin = tempMin; 
    }
    
    public double getTempMax() { 
        return tempMax; 
    }
    
    public void setTempMax(double tempMax) { 
        this.tempMax = tempMax; 
    }
    
    public int getHumidity() { 
        return humidity; 
    }
    
    public void setHumidity(int humidity) { 
        this.humidity = humidity; 
    }
} 