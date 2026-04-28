package com.travelnote.repository;

import com.travelnote.entity.Location;
import com.travelnote.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    List<Location> findByTripOrderByDisplayOrderAsc(Trip trip);
    
    List<Location> findByTripIdOrderByDisplayOrderAsc(Long tripId);
    
    Optional<Location> findByIdAndTripId(Long id, Long tripId);
    
    @Query("SELECT l FROM Location l WHERE l.trip.id = :tripId ORDER BY l.visitDate ASC, l.displayOrder ASC")
    List<Location> findByTripIdOrderByVisitDateAndDisplayOrder(@Param("tripId") Long tripId);
    
    @Query("SELECT MAX(l.displayOrder) FROM Location l WHERE l.trip.id = :tripId")
    Optional<Integer> findMaxDisplayOrderByTripId(@Param("tripId") Long tripId);
}
