package com.example.travelplanner.repository;

import com.example.travelplanner.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    List<Trip> findByUserId(String userId);
}
