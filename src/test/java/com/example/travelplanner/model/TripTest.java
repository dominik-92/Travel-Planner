package com.example.travelplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {

    @Test
    void defaultConstructorCreatesEmptyLists() {
        Trip trip = new Trip();
        assertNotNull(trip.getItinerary());
        assertNotNull(trip.getExpenses());
        assertTrue(trip.getItinerary().isEmpty());
        assertTrue(trip.getExpenses().isEmpty());
    }

    @Test
    void gettersAndSetters() {
        Trip trip = new Trip();
        trip.setId("t1");
        trip.setName("Paris Trip");
        trip.setDestination("Paris");
        trip.setStartDate("2026-08-01");
        trip.setEndDate("2026-08-10");
        trip.setBudget(2000.0);
        trip.setNotes("Pack light");
        trip.setDestinationNotes("Paris notes");
        trip.setCreatedAt("2026-01-01T00:00:00Z");

        assertEquals("t1", trip.getId());
        assertEquals("Paris Trip", trip.getName());
        assertEquals("Paris", trip.getDestination());
        assertEquals("2026-08-01", trip.getStartDate());
        assertEquals("2026-08-10", trip.getEndDate());
        assertEquals(2000.0, trip.getBudget());
        assertEquals("Pack light", trip.getNotes());
        assertEquals("Paris notes", trip.getDestinationNotes());
        assertEquals("2026-01-01T00:00:00Z", trip.getCreatedAt());
    }

    @Test
    void addItineraryItemSetsBidirectionalLink() {
        Trip trip = new Trip();
        ItineraryItem item = new ItineraryItem("i1", 1, "10:00", "Visit Louvre", "Museum");

        trip.addItineraryItem(item);

        assertEquals(1, trip.getItinerary().size());
        assertSame(trip, item.getTrip());
    }

    @Test
    void addExpenseSetsBidirectionalLink() {
        Trip trip = new Trip();
        Expense expense = new Expense("e1", "Food", 50.0, "Lunch", "2026-08-01T12:00:00Z");

        trip.addExpense(expense);

        assertEquals(1, trip.getExpenses().size());
        assertSame(trip, expense.getTrip());
    }

    @Test
    void setAndGetUser() {
        Trip trip = new Trip();
        User user = new User("u1", "john", "john@test.com", "pass", "2026-01-01");

        trip.setUser(user);

        assertSame(user, trip.getUser());
    }

    @Test
    void setAndGetDestinationInfo() {
        Trip trip = new Trip();
        DestinationInfo info = new DestinationInfo("Sunny", "Euro", "Bring sunscreen");

        trip.setDestinationInfo(info);

        assertNotNull(trip.getDestinationInfo());
        assertEquals("Sunny", trip.getDestinationInfo().getWeather());
        assertEquals("Euro", trip.getDestinationInfo().getCurrency());
        assertEquals("Bring sunscreen", trip.getDestinationInfo().getTips());
    }
}
