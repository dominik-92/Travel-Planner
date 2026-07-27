package com.example.travelplanner.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    private String id;

    private String category;
    private double amount;
    private String description;
    private String addedAt;
    @Column(columnDefinition = "double precision default 1.0")
    private double rateToPln = 1.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    private Trip trip;

    public Expense() {
    }

    public Expense(String id, String category, double amount, String description, String addedAt) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.addedAt = addedAt;
    }

    public Expense(String id, String category, double amount, String description, String addedAt, double rateToPln) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.addedAt = addedAt;
        this.rateToPln = rateToPln;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public double getRateToPln() {
        return rateToPln;
    }

    public void setRateToPln(double rateToPln) {
        this.rateToPln = rateToPln;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
}
