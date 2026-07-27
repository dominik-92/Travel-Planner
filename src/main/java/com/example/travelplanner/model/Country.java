package com.example.travelplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "countries")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String currencyCode;
    private String currencyNameEn;
    private String currencyNamePl;

    public Country() {
    }

    public Country(String country, String currencyCode, String currencyNameEn, String currencyNamePl) {
        this.country = country;
        this.currencyCode = currencyCode;
        this.currencyNameEn = currencyNameEn;
        this.currencyNamePl = currencyNamePl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyNameEn() {
        return currencyNameEn;
    }

    public void setCurrencyNameEn(String currencyNameEn) {
        this.currencyNameEn = currencyNameEn;
    }

    public String getCurrencyNamePl() {
        return currencyNamePl;
    }

    public void setCurrencyNamePl(String currencyNamePl) {
        this.currencyNamePl = currencyNamePl;
    }
}
