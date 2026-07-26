package com.example.travelplanner.service;

import com.example.travelplanner.model.DestinationInfo;
import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<Trip> findAllByUser(User user) {
        return tripRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public Trip findByIdAndUser(String id, User user) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found"));
        if (trip.getUser() == null || !trip.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied");
        }
        return trip;
    }

    public Trip createTrip(Trip trip, User user) {
        if (trip.getId() == null || trip.getId().isBlank()) {
            trip.setId(generateId());
        }
        if (trip.getCreatedAt() == null || trip.getCreatedAt().isBlank()) {
            trip.setCreatedAt(Instant.now().toString());
        }
        if (trip.getDestinationNotes() == null) {
            trip.setDestinationNotes(trip.getNotes());
        }
        trip.setUser(user);
        return tripRepository.save(trip);
    }

    public void deleteTrip(String id, User user) {
        Trip trip = findByIdAndUser(id, user);
        tripRepository.deleteById(trip.getId());
    }

    public Trip addItineraryItem(String tripId, ItineraryItem item, User user) {
        var trip = findByIdAndUser(tripId, user);
        var finalItem = (item.getId() == null || item.getId().isBlank())
                ? new ItineraryItem(generateId(), item.getDay(), item.getTime(), item.getTitle(), item.getDescription())
                : item;
        trip.addItineraryItem(finalItem);
        tripRepository.save(trip);
        return trip;
    }

    public Trip removeItineraryItem(String tripId, String itemId, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.getItinerary().removeIf(item -> item.getId().equals(itemId));
        tripRepository.save(trip);
        return trip;
    }

    public Trip addExpense(String tripId, Expense expense, User user) {
        var trip = findByIdAndUser(tripId, user);
        if (expense.getId() == null || expense.getId().isBlank()) {
            expense = new Expense(generateId(), expense.getCategory(), expense.getAmount(), expense.getDescription(), expense.getAddedAt());
        }
        if (expense.getAddedAt() == null || expense.getAddedAt().isBlank()) {
            expense.setAddedAt(Instant.now().toString());
        }
        trip.addExpense(expense);
        tripRepository.save(trip);
        return trip;
    }

    public Trip removeExpense(String tripId, String expenseId, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.getExpenses().removeIf(expense -> expense.getId().equals(expenseId));
        tripRepository.save(trip);
        return trip;
    }

    public Trip loadDestinationInfo(String tripId, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.setDestinationInfo(buildDestinationInfo(trip.getDestination()));
        tripRepository.save(trip);
        return trip;
    }

    public Trip updateDestinationNotes(String tripId, String destinationNotes, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.setDestinationNotes(destinationNotes);
        tripRepository.save(trip);
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
