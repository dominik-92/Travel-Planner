package com.example.travelplanner.controller;

import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import com.example.travelplanner.service.TripService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<Trip> getTrips() {
        return tripService.findAll();
    }

    @GetMapping("/{tripId}")
    public Trip getTrip(@PathVariable String tripId) {
        return tripService.findById(tripId);
    }

    @PostMapping
    public Trip createTrip(@RequestBody Trip trip) {
        return tripService.createTrip(trip);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable String tripId) {
        tripService.deleteTrip(tripId);
    }

    @PostMapping("/{tripId}/itinerary")
    public Trip addItineraryItem(@PathVariable String tripId, @RequestBody ItineraryItem item) {
        return tripService.addItineraryItem(tripId, item);
    }

    @DeleteMapping("/{tripId}/itinerary/{itemId}")
    public Trip removeItineraryItem(@PathVariable String tripId, @PathVariable String itemId) {
        return tripService.removeItineraryItem(tripId, itemId);
    }

    @PostMapping("/{tripId}/expenses")
    public Trip addExpense(@PathVariable String tripId, @RequestBody Expense expense) {
        return tripService.addExpense(tripId, expense);
    }

    @DeleteMapping("/{tripId}/expenses/{expenseId}")
    public Trip removeExpense(@PathVariable String tripId, @PathVariable String expenseId) {
        return tripService.removeExpense(tripId, expenseId);
    }

    @PostMapping("/{tripId}/destination-info")
    public Trip loadDestinationInfo(@PathVariable String tripId) {
        return tripService.loadDestinationInfo(tripId);
    }

    @PostMapping("/{tripId}/notes")
    public Trip saveNotes(@PathVariable String tripId, @RequestBody Map<String, String> payload) {
        return tripService.updateDestinationNotes(tripId, payload.getOrDefault("destinationNotes", ""));
    }
}