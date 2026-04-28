package com.travelnote.repository;

import com.travelnote.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    List<Photo> findByJournalEntryIdOrderByDisplayOrderAsc(Long journalEntryId);
    
    List<Photo> findByTripIdOrderByUploadedAtDesc(Long tripId);
    
    List<Photo> findByUserIdOrderByUploadedAtDesc(Long userId);
    
    Optional<Photo> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT MAX(p.displayOrder) FROM Photo p WHERE p.journalEntry.id = :journalEntryId")
    Optional<Integer> findMaxDisplayOrderByJournalEntryId(@Param("journalEntryId") Long journalEntryId);
    
    @Query("SELECT p FROM Photo p WHERE p.journalEntry.id IN (SELECT je.id FROM JournalEntry je WHERE je.trip.id = :tripId) ORDER BY p.takenAt ASC")
    List<Photo> findAllPhotosInTripOrderByTakenAt(@Param("tripId") Long tripId);
}
