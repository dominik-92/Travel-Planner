package com.example.travelplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItineraryItemTest {

    @Test
    void parameterizedConstructorSetsAllFields() {
        ItineraryItem item = new ItineraryItem("i1", 2, "14:30", "Eiffel Tower", "Visit the top floor");

        assertEquals("i1", item.getId());
        assertEquals(2, item.getDay());
        assertEquals("14:30", item.getTime());
        assertEquals("Eiffel Tower", item.getTitle());
        assertEquals("Visit the top floor", item.getDescription());
    }

    @Test
    void gettersAndSetters() {
        ItineraryItem item = new ItineraryItem();
        item.setId("i2");
        item.setDay(3);
        item.setTime("09:00");
        item.setTitle("Museum visit");
        item.setDescription("Louvre");

        assertEquals("i2", item.getId());
        assertEquals(3, item.getDay());
        assertEquals("09:00", item.getTime());
        assertEquals("Museum visit", item.getTitle());
        assertEquals("Louvre", item.getDescription());
    }

    @Test
    void tripAssociation() {
        ItineraryItem item = new ItineraryItem();
        Trip trip = new Trip();
        trip.setId("t1");

        item.setTrip(trip);

        assertSame(trip, item.getTrip());
    }
}
