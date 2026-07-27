package com.example.travelplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    @Test
    void parameterizedConstructorSetsAllFields() {
        Expense expense = new Expense("e1", "Food", 45.50, "Dinner", "2026-08-01T19:00:00Z");

        assertEquals("e1", expense.getId());
        assertEquals("Food", expense.getCategory());
        assertEquals(45.50, expense.getAmount());
        assertEquals("Dinner", expense.getDescription());
        assertEquals("2026-08-01T19:00:00Z", expense.getAddedAt());
    }

    @Test
    void gettersAndSetters() {
        Expense expense = new Expense();
        expense.setId("e2");
        expense.setCategory("Transport");
        expense.setAmount(15.0);
        expense.setDescription("Taxi");
        expense.setAddedAt("2026-08-02T08:00:00Z");
        expense.setRateToPln(4.3257);

        assertEquals("e2", expense.getId());
        assertEquals("Transport", expense.getCategory());
        assertEquals(15.0, expense.getAmount());
        assertEquals("Taxi", expense.getDescription());
        assertEquals("2026-08-02T08:00:00Z", expense.getAddedAt());
        assertEquals(4.3257, expense.getRateToPln());
    }

    @Test
    void tripAssociation() {
        Expense expense = new Expense();
        Trip trip = new Trip();
        trip.setId("t1");

        expense.setTrip(trip);

        assertSame(trip, expense.getTrip());
    }
}
