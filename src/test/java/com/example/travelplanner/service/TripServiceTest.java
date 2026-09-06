package com.example.travelplanner.service;

import com.example.travelplanner.model.DestinationInfo;
import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private TripService tripService;

    private User user;
    private User otherUser;
    private Trip trip;

    @BeforeEach
    void setUp() {
        user = new User("u1", "john", "john@test.com", "pass", "2026-01-01");
        otherUser = new User("u2", "jane", "jane@test.com", "pass", "2026-01-01");

        trip = new Trip();
        trip.setId("t1");
        trip.setName("Paris Trip");
        trip.setDestination("Paris");
        trip.setStartDate("2026-08-01");
        trip.setEndDate("2026-08-10");
        trip.setBudget(2000.0);
        trip.setUser(user);
    }

    @Test
    void findAllByUserReturnsTrips() {
        when(tripRepository.findByUserId("u1")).thenReturn(List.of(trip));

        List<Trip> result = tripService.findAllByUser(user);

        assertEquals(1, result.size());
        assertEquals("Paris Trip", result.get(0).getName());
    }

    @Test
    void findByIdAndUserReturnsTripWhenOwned() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip result = tripService.findByIdAndUser("t1", user);

        assertEquals("Paris Trip", result.getName());
    }

    @Test
    void findByIdAndUserThrowsWhenNotFound() {
        when(tripRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> tripService.findByIdAndUser("missing", user));
    }

    @Test
    void findByIdAndUserThrowsWhenOwnedByOtherUser() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        assertThrows(SecurityException.class, () -> tripService.findByIdAndUser("t1", otherUser));
    }

    @Test
    void findByIdAndUserThrowsWhenTripHasNoUser() {
        trip.setUser(null);
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        assertThrows(SecurityException.class, () -> tripService.findByIdAndUser("t1", user));
    }

    @Test
    void createTripGeneratesIdAndTimestamp() {
        Trip newTrip = new Trip();
        newTrip.setName("New Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");
        newTrip.setBudget(1000.0);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.createTrip(newTrip, user);

        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertSame(user, result.getUser());
    }

    @Test
    void createTripPreservesExistingId() {
        Trip existingTrip = new Trip();
        existingTrip.setId("existing-id");
        existingTrip.setCreatedAt("2026-01-01T00:00:00Z");
        existingTrip.setName("Existing Trip");
        existingTrip.setDestination("Paris");
        existingTrip.setCountry("France");
        existingTrip.setStartDate("2026-08-01");
        existingTrip.setEndDate("2026-08-10");
        existingTrip.setBudget(1000.0);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.createTrip(existingTrip, user);

        assertEquals("existing-id", result.getId());
        assertEquals("2026-01-01T00:00:00Z", result.getCreatedAt());
    }

    @Test
    void createTripSetsDestinationNotesFromNotesWhenNull() {
        Trip newTrip = new Trip();
        newTrip.setName("New Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");
        newTrip.setBudget(1000.0);
        newTrip.setNotes("General notes");
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.createTrip(newTrip, user);

        assertEquals("General notes", result.getDestinationNotes());
    }

    @Test
    void createTripRejectsBlankName() {
        Trip newTrip = new Trip();
        newTrip.setName(" ");
        newTrip.setDestination("Paris");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsBlankDestination() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsBlankCountry() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsEndDateBeforeStartDate() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-10");
        newTrip.setEndDate("2026-08-01");

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsNegativeBudget() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");
        newTrip.setBudget(-1.0);

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsZeroBudget() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("2026-08-01");
        newTrip.setEndDate("2026-08-10");
        newTrip.setBudget(0.0);

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void createTripRejectsBlankDates() {
        Trip newTrip = new Trip();
        newTrip.setName("Paris Trip");
        newTrip.setDestination("Paris");
        newTrip.setCountry("France");
        newTrip.setStartDate("");
        newTrip.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(newTrip, user));
    }

    @Test
    void updateTripUpdatesMutableFields() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip updates = new Trip();
        updates.setName("Updated Trip");
        updates.setDestination("London");
        updates.setCountry("United Kingdom");
        updates.setStartDate("2026-09-01");
        updates.setEndDate("2026-09-15");
        updates.setBudget(3000.0);
        updates.setCurrency("GBP");
        updates.setNotes("New notes");

        Trip result = tripService.updateTrip("t1", updates, user);

        assertEquals("Updated Trip", result.getName());
        assertEquals("London", result.getDestination());
        assertEquals("United Kingdom", result.getCountry());
        assertEquals("2026-09-01", result.getStartDate());
        assertEquals("2026-09-15", result.getEndDate());
        assertEquals(3000.0, result.getBudget());
        assertEquals("GBP", result.getCurrency());
        assertEquals("New notes", result.getNotes());
    }

    @Test
    void updateTripPreservesIdentityItineraryAndExpenses() {
        ItineraryItem item = new ItineraryItem("i1", 1, "10:00", "Louvre", "Museum");
        Expense expense = new Expense("e1", "Food", 50.0, "Lunch", "2026-08-01T12:00:00Z");
        trip.addItineraryItem(item);
        trip.addExpense(expense);
        trip.setCreatedAt("2026-01-01T00:00:00Z");

        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip updates = new Trip();
        updates.setName("Renamed");
        updates.setDestination("Paris");
        updates.setCountry("France");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");
        updates.setBudget(2500.0);

        Trip result = tripService.updateTrip("t1", updates, user);

        assertEquals("t1", result.getId());
        assertEquals("2026-01-01T00:00:00Z", result.getCreatedAt());
        assertSame(user, result.getUser());
        assertEquals(1, result.getItinerary().size());
        assertEquals(1, result.getExpenses().size());
    }

    @Test
    void updateTripThrowsWhenNotFound() {
        when(tripRepository.findById("missing")).thenReturn(Optional.empty());

        Trip updates = new Trip();
        updates.setName("N");
        updates.setDestination("D");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");

        assertThrows(NoSuchElementException.class, () -> tripService.updateTrip("missing", updates, user));
    }

    @Test
    void updateTripThrowsWhenOwnedByOtherUser() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("N");
        updates.setDestination("D");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");

        assertThrows(SecurityException.class, () -> tripService.updateTrip("t1", updates, otherUser));
    }

    @Test
    void updateTripRejectsBlankName() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("  ");
        updates.setDestination("Paris");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsBlankDestination() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("");
        updates.setCountry("France");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsBlankCountry() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("Paris");
        updates.setCountry("  ");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsEndDateBeforeStartDate() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("Paris");
        updates.setCountry("France");
        updates.setStartDate("2026-08-10");
        updates.setEndDate("2026-08-01");

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsNegativeBudget() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("Paris");
        updates.setCountry("France");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");
        updates.setBudget(-1.0);

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsZeroBudget() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("Paris");
        updates.setCountry("France");
        updates.setStartDate("2026-08-01");
        updates.setEndDate("2026-08-10");
        updates.setBudget(0.0);

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void updateTripRejectsBlankDates() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        Trip updates = new Trip();
        updates.setName("Paris Trip");
        updates.setDestination("Paris");
        updates.setCountry("France");
        updates.setStartDate("");
        updates.setEndDate("2026-08-10");

        assertThrows(IllegalArgumentException.class, () -> tripService.updateTrip("t1", updates, user));
    }

    @Test
    void deleteTripCallsRepositoryDelete() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));

        tripService.deleteTrip("t1", user);

        verify(tripRepository).deleteById("t1");
    }

    @Test
    void addItineraryItemAddsItemToTrip() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        ItineraryItem item = new ItineraryItem(null, 1, "10:00", "Louvre", "Museum");
        Trip result = tripService.addItineraryItem("t1", item, user);

        assertEquals(1, result.getItinerary().size());
        assertNotNull(result.getItinerary().get(0).getId());
    }

    @Test
    void addItineraryItemPreservesExistingId() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        ItineraryItem item = new ItineraryItem("existing-item-id", 1, "10:00", "Louvre", "Museum");
        Trip result = tripService.addItineraryItem("t1", item, user);

        assertEquals("existing-item-id", result.getItinerary().get(0).getId());
    }

    @Test
    void removeItineraryItemRemovesFromTrip() {
        ItineraryItem item = new ItineraryItem("i1", 1, "10:00", "Louvre", "Museum");
        trip.addItineraryItem(item);
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.removeItineraryItem("t1", "i1", user);

        assertTrue(result.getItinerary().isEmpty());
    }

    @Test
    void addExpenseAddsExpenseToTrip() {
        trip.setCurrency("EUR");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));
        when(currencyService.getHistoricalRate(eq("EUR"), anyString())).thenReturn(4.3257);

        Expense expense = new Expense(null, "Food", 50.0, "Lunch", null);
        Trip result = tripService.addExpense("t1", expense, user);

        assertEquals(1, result.getExpenses().size());
        assertNotNull(result.getExpenses().get(0).getId());
        assertNotNull(result.getExpenses().get(0).getAddedAt());
        assertEquals(4.3257, result.getExpenses().get(0).getRateToPln());
    }

    @Test
    void addExpensePreservesExistingIdAndTimestamp() {
        trip.setCurrency("PLN");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));
        when(currencyService.getHistoricalRate(eq("PLN"), anyString())).thenReturn(1.0);

        Expense expense = new Expense("e1", "Food", 50.0, "Lunch", "2026-08-01T12:00:00Z");
        Trip result = tripService.addExpense("t1", expense, user);

        assertEquals("e1", result.getExpenses().get(0).getId());
        assertEquals("2026-08-01T12:00:00Z", result.getExpenses().get(0).getAddedAt());
        assertEquals(1.0, result.getExpenses().get(0).getRateToPln());
    }

    @Test
    void removeExpenseRemovesFromTrip() {
        Expense expense = new Expense("e1", "Food", 50.0, "Lunch", "2026-08-01T12:00:00Z");
        trip.addExpense(expense);
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.removeExpense("t1", "e1", user);

        assertTrue(result.getExpenses().isEmpty());
    }

    @Test
    void loadDestinationInfoForParis() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        DestinationInfo info = result.getDestinationInfo();
        assertNotNull(info);
        assertEquals("Euro (€)", info.getCurrency());
        assertEquals("EUR", info.getCurrencyCode());
        assertEquals("EUR", result.getCurrency());
        assertTrue(info.getWeather().contains("Mild"));
    }

    @Test
    void loadDestinationInfoForLondon() {
        trip.setDestination("London");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        assertEquals("Pound Sterling (£)", result.getDestinationInfo().getCurrency());
        assertEquals("GBP", result.getDestinationInfo().getCurrencyCode());
        assertEquals("GBP", result.getCurrency());
    }

    @Test
    void loadDestinationInfoForNewYork() {
        trip.setDestination("New York");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        assertEquals("US Dollar ($)", result.getDestinationInfo().getCurrency());
        assertEquals("USD", result.getDestinationInfo().getCurrencyCode());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void loadDestinationInfoForTokyo() {
        trip.setDestination("Tokyo");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        assertEquals("Japanese Yen (¥)", result.getDestinationInfo().getCurrency());
        assertEquals("JPY", result.getDestinationInfo().getCurrencyCode());
        assertEquals("JPY", result.getCurrency());
    }

    @Test
    void loadDestinationInfoForSydney() {
        trip.setDestination("Sydney");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        assertEquals("Australian Dollar (A$)", result.getDestinationInfo().getCurrency());
        assertEquals("AUD", result.getDestinationInfo().getCurrencyCode());
        assertEquals("AUD", result.getCurrency());
    }

    @Test
    void loadDestinationInfoForUnknownDestinationReturnsDefaults() {
        trip.setDestination("Berlin");
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.loadDestinationInfo("t1", user);

        assertNotNull(result.getDestinationInfo());
        assertTrue(result.getDestinationInfo().getCurrency().contains("Local currency"));
        assertEquals("PLN", result.getDestinationInfo().getCurrencyCode());
        assertEquals("PLN", result.getCurrency());
    }

    @Test
    void updateDestinationNotesSavesNotes() {
        when(tripRepository.findById("t1")).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> inv.getArgument(0));

        Trip result = tripService.updateDestinationNotes("t1", "My custom notes", user);

        assertEquals("My custom notes", result.getDestinationNotes());
    }
}
