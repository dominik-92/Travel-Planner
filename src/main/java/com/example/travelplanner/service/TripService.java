package com.example.travelplanner.service;

import com.example.travelplanner.model.DestinationInfo;
import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TripService {

    private final Map<String, Trip> trips = new LinkedHashMap<>();

    public List<Trip> findAll() {
        return new ArrayList<>(trips.values());
    }

    public Trip findById(String id) {
        var trip = trips.get(id);
        if (trip == null) {
            throw new NoSuchElementException("Trip not found");
        }
        return trip;
    }

    public Trip createTrip(Trip trip) {
        if (trip.getId() == null || trip.getId().isBlank()) {
            trip.setId(generateId());
        }
        if (trip.getCreatedAt() == null || trip.getCreatedAt().isBlank()) {
            trip.setCreatedAt(Instant.now().toString());
        }
        if (trip.getDestinationNotes() == null) {
            trip.setDestinationNotes(trip.getNotes());
        }
        if (trip.getItinerary() == null) {
            trip.setItinerary(new ArrayList<>());
        }
        if (trip.getExpenses() == null) {
            trip.setExpenses(new ArrayList<>());
        }
        trips.put(trip.getId(), trip);
        return trip;
    }

    public void deleteTrip(String id) {
        trips.remove(id);
    }

    public Trip addItineraryItem(String tripId, ItineraryItem item) {
        var trip = findById(tripId);
        var finalItem = (item.id() == null || item.id().isBlank())
                ? new ItineraryItem(generateId(), item.day(), item.time(), item.title(), item.description())
                : item;
        trip.getItinerary().add(finalItem);
        return trip;
    }

    public Trip removeItineraryItem(String tripId, String itemId) {
        var trip = findById(tripId);
        trip.setItinerary(new ArrayList<>(trip.getItinerary().stream()
                .filter(item -> !item.id().equals(itemId))
                .toList()));
        return trip;
    }

    public Trip addExpense(String tripId, Expense expense) {
        var trip = findById(tripId);
        var finalExpense = (expense.id() == null || expense.id().isBlank())
                ? new Expense(generateId(), expense.category(), expense.amount(), expense.description(), expense.addedAt())
                : expense;
        trip.getExpenses().add(finalExpense);
        return trip;
    }

    public Trip removeExpense(String tripId, String expenseId) {
        var trip = findById(tripId);
        trip.setExpenses(new ArrayList<>(trip.getExpenses().stream()
                .filter(expense -> !expense.id().equals(expenseId))
                .toList()));
        return trip;
    }

    public Trip loadDestinationInfo(String tripId) {
        var trip = findById(tripId);
        trip.setDestinationInfo(buildDestinationInfo(trip.getDestination()));
        return trip;
    }

    public Trip updateDestinationNotes(String tripId, String destinationNotes) {
        var trip = findById(tripId);
        trip.setDestinationNotes(destinationNotes);
        return trip;
    }

    private DestinationInfo buildDestinationInfo(String destination) {
        var normalized = destination == null ? "" : destination.trim().toLowerCase();

        var defaults = new DestinationInfo(
                "Check local reports for the latest forecast.",
                "Local currency may vary by country.",
                "Bring comfortable shoes, stay hydrated, and verify transport options in advance."
        );

        return switch (normalized) {
            case String s when s.matches(".*(paris|france|europe).*") ->
                    new DestinationInfo(
                            "Mild and changeable – pack a light layer.",
                            "Euro (€)",
                            "Book museums early and use metro passes for savings."
                    );
            case String s when s.matches(".*(london|uk|england|britain).*") ->
                    new DestinationInfo(
                            "Unpredictable weather – carry a compact umbrella.",
                            "Pound Sterling (£)",
                            "Plan around tube hours and enjoy pub meals in the evening."
                    );
            case String s when s.matches(".*(new york|usa|united states|america).*") ->
                    new DestinationInfo(
                            "Seasonal: check forecast before packing.",
                            "US Dollar ($)",
                            "Buy transit cards ahead and reserve popular attractions early."
                    );
            case String s when s.matches(".*(tokyo|japan).*") ->
                    new DestinationInfo(
                            "Often humid in summer; cool in autumn.",
                            "Japanese Yen (¥)",
                            "Carry cash for small shops and follow local etiquette."
                    );
            case String s when s.matches(".*(sydney|australia).*") ->
                    new DestinationInfo(
                            "Sunny days are common; sunscreen is essential.",
                            "Australian Dollar (A$)",
                            "Respect wildlife and plan for longer travel distances."
                    );
            default -> defaults;
        };
    }

    private String generateId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}