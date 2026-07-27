package com.example.travelplanner.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class DestinationInfo {

    private String weather;
    private String currency;
    private String currencyCode;
    private String tips;

    public DestinationInfo() {
    }

    public DestinationInfo(String weather, String currency, String currencyCode, String tips) {
        this.weather = weather;
        this.currency = currency;
        this.currencyCode = currencyCode;
        this.tips = tips;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
