package com.example.travelplanner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    private String code;

    private String name;
    private String namePl;
    private String nameEs;
    private double rate;
    private String effectiveDate;

    public Currency() {
    }

    public Currency(String code, String name, String namePl, String nameEs, double rate, String effectiveDate) {
        this.code = code;
        this.name = name;
        this.namePl = namePl;
        this.nameEs = nameEs;
        this.rate = rate;
        this.effectiveDate = effectiveDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamePl() {
        return namePl;
    }

    public void setNamePl(String namePl) {
        this.namePl = namePl;
    }

    public String getNameEs() {
        return nameEs;
    }

    public void setNameEs(String nameEs) {
        this.nameEs = nameEs;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
