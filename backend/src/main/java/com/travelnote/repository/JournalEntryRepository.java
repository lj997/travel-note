package com.travelnote.repository;

import com.travelnote.entity.JournalEntry;
import com.travelnote.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    
    List<JournalEntry> findByTripOrderByDateAsc(Trip trip);
    
    List<JournalEntry> findByTripIdOrderByDateAsc(Long tripId);
    
    Optional<JournalEntry> findByIdAndTripId(Long id, Long tripId);
    
    Optional<JournalEntry> findByTripIdAndDate(Long tripId, LocalDate date);
    
    @Query("SELECT je FROM JournalEntry je WHERE je.trip.id = :tripId AND je.date BETWEEN :startDate AND :endDate ORDER BY je.date ASC")
    List<JournalEntry> findByTripIdAndDateRange(
        @Param("tripId") Long tripId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT je FROM JournalEntry je WHERE je.trip.id = :tripId AND (LOWER(je.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(je.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY je.date ASC")
    List<JournalEntry> searchByTripIdAndKeyword(@Param("tripId") Long tripId, @Param("keyword") String keyword);
}
