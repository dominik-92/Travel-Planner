package com.example.travelplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DestinationInfoTest {

    @Test
    void parameterizedConstructorSetsAllFields() {
        DestinationInfo info = new DestinationInfo("Sunny", "Euro (EUR)", "Bring sunscreen");

        assertEquals("Sunny", info.getWeather());
        assertEquals("Euro (EUR)", info.getCurrency());
        assertEquals("Bring sunscreen", info.getTips());
    }

    @Test
    void gettersAndSetters() {
        DestinationInfo info = new DestinationInfo();
        info.setWeather("Rainy");
        info.setCurrency("Pound (GBP)");
        info.setTips("Carry umbrella");

        assertEquals("Rainy", info.getWeather());
        assertEquals("Pound (GBP)", info.getCurrency());
        assertEquals("Carry umbrella", info.getTips());
    }
}
