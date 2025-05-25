package com.example.weatherapi;

import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class SimpleTest {

    @Test
    public void testBasicMath() {
        assertEquals(2 + 2, 4);
        assertTrue(true);
    }

    @Test
    public void testStringOperations() {
        String hello = "Hello";
        String world = "World";
        String combined = hello + " " + world;
        assertEquals(combined, "Hello World");
    }

    @Test
    public void testApplicationExists() {
        // Simple test to verify the application class exists
        assertTrue(WeatherApiApplication.class != null);
    }
} 