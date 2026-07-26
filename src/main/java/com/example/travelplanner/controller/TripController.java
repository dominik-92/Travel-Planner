package com.example.travelplanner.controller;

import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import com.example.travelplanner.service.AuthService;
import com.example.travelplanner.service.TripService;
import org.springframework.security.core.Authentication;
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
public class TripController {

    private final TripService tripService;
    private final AuthService authService;

    public TripController(TripService tripService, AuthService authService) {
        this.tripService = tripService;
        this.authService = authService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return authService.findByUsername(authentication.getName());
    }

    @GetMapping
    public List<Trip> getTrips(Authentication authentication) {
        return tripService.findAllByUser(getAuthenticatedUser(authentication));
    }

    @GetMapping("/{tripId}")
    public Trip getTrip(@PathVariable String tripId, Authentication authentication) {
        return tripService.findByIdAndUser(tripId, getAuthenticatedUser(authentication));
    }

    @PostMapping
    public Trip createTrip(@RequestBody Trip trip, Authentication authentication) {
        return tripService.createTrip(trip, getAuthenticatedUser(authentication));
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable String tripId, Authentication authentication) {
        tripService.deleteTrip(tripId, getAuthenticatedUser(authentication));
    }

    @PostMapping("/{tripId}/itinerary")
    public Trip addItineraryItem(@PathVariable String tripId, @RequestBody ItineraryItem item, Authentication authentication) {
        return tripService.addItineraryItem(tripId, item, getAuthenticatedUser(authentication));
    }

    @DeleteMapping("/{tripId}/itinerary/{itemId}")
    public Trip removeItineraryItem(@PathVariable String tripId, @PathVariable String itemId, Authentication authentication) {
        return tripService.removeItineraryItem(tripId, itemId, getAuthenticatedUser(authentication));
    }

    @PostMapping("/{tripId}/expenses")
    public Trip addExpense(@PathVariable String tripId, @RequestBody Expense expense, Authentication authentication) {
        return tripService.addExpense(tripId, expense, getAuthenticatedUser(authentication));
    }

    @DeleteMapping("/{tripId}/expenses/{expenseId}")
    public Trip removeExpense(@PathVariable String tripId, @PathVariable String expenseId, Authentication authentication) {
        return tripService.removeExpense(tripId, expenseId, getAuthenticatedUser(authentication));
    }

    @PostMapping("/{tripId}/destination-info")
    public Trip loadDestinationInfo(@PathVariable String tripId, Authentication authentication) {
        return tripService.loadDestinationInfo(tripId, getAuthenticatedUser(authentication));
    }

    @PostMapping("/{tripId}/notes")
    public Trip saveNotes(@PathVariable String tripId, @RequestBody Map<String, String> payload, Authentication authentication) {
        return tripService.updateDestinationNotes(tripId, payload.getOrDefault("destinationNotes", ""), getAuthenticatedUser(authentication));
    }
}