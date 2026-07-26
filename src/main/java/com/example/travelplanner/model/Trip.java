package com.example.travelplanner.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    private String id;

    private String name;
    private String destination;
    private String startDate;
    private String endDate;
    private double budget;
    private String notes;
    private String destinationNotes;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryItem> itinerary = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @Embedded
    private DestinationInfo destinationInfo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String createdAt;

    public Trip() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDestinationNotes() {
        return destinationNotes;
    }

    public void setDestinationNotes(String destinationNotes) {
        this.destinationNotes = destinationNotes;
    }

    public List<ItineraryItem> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryItem> itinerary) {
        this.itinerary = itinerary;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public DestinationInfo getDestinationInfo() {
        return destinationInfo;
    }

    public void setDestinationInfo(DestinationInfo destinationInfo) {
        this.destinationInfo = destinationInfo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void addItineraryItem(ItineraryItem item) {
        itinerary.add(item);
        item.setTrip(this);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        expense.setTrip(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
