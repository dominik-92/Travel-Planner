package com.example.travelplanner.repository;

import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TripRepositoryTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        userRepository.deleteAll();

        user = new User("u1", "john", "john@test.com", "pass", "2026-01-01");
        userRepository.save(user);

        Trip trip1 = new Trip();
        trip1.setId("t1");
        trip1.setName("Paris Trip");
        trip1.setDestination("Paris");
        trip1.setStartDate("2026-08-01");
        trip1.setEndDate("2026-08-10");
        trip1.setBudget(2000.0);
        trip1.setUser(user);
        tripRepository.save(trip1);

        Trip trip2 = new Trip();
        trip2.setId("t2");
        trip2.setName("London Trip");
        trip2.setDestination("London");
        trip2.setStartDate("2026-09-01");
        trip2.setEndDate("2026-09-10");
        trip2.setBudget(1500.0);
        trip2.setUser(user);
        tripRepository.save(trip2);
    }

    @Test
    void findByUserIdReturnsCorrectTrips() {
        List<Trip> trips = tripRepository.findByUserId("u1");

        assertEquals(2, trips.size());
    }

    @Test
    void findByUserIdReturnsEmptyForUnknownUser() {
        List<Trip> trips = tripRepository.findByUserId("unknown");

        assertTrue(trips.isEmpty());
    }

    @Test
    void findByIdReturnsCorrectTrip() {
        Trip trip = tripRepository.findById("t1").orElse(null);

        assertNotNull(trip);
        assertEquals("Paris Trip", trip.getName());
    }

    @Test
    void deleteByIdRemovesTrip() {
        tripRepository.deleteById("t1");

        assertTrue(tripRepository.findById("t1").isEmpty());
        assertEquals(1, tripRepository.findByUserId("u1").size());
    }
}
