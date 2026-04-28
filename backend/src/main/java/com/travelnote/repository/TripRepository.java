package com.travelnote.repository;

import com.travelnote.entity.Trip;
import com.travelnote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    List<Trip> findByUserOrderByStartDateDesc(User user);
    
    List<Trip> findByUserIdOrderByStartDateDesc(Long userId);
    
    Optional<Trip> findByIdAndUserId(Long id, Long userId);
    
    Optional<Trip> findByShareToken(String shareToken);
    
    @Query("SELECT DISTINCT t FROM Trip t JOIN t.tags tag WHERE t.user.id = :userId AND tag.name IN :tagNames")
    List<Trip> findByUserIdAndTagNames(@Param("userId") Long userId, @Param("tagNames") List<String> tagNames);
    
    @Query("SELECT t FROM Trip t WHERE t.user.id = :userId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Trip> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    @Query("SELECT t FROM Trip t WHERE t.isPublic = true ORDER BY t.createdAt DESC")
    List<Trip> findPublicTrips();
}
